// MqttService.java
package com.smartcar.monitoring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcar.monitoring.dto.TelemetryDto;
import com.smartcar.monitoring.model.Alert;
import com.smartcar.monitoring.model.Car;
import com.smartcar.monitoring.model.Telemetry;
import com.smartcar.monitoring.model.Trip;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class MqttService {

    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);

    @Autowired
    private MqttClient mqttClient;

    @Autowired
    private TelemetryService telemetryService;

    @Autowired
    private CarService carService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private TripService tripService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${mqtt.topic.prefix}")
    private String topicPrefix;

    @Value("${mqtt.client.id}")
    private String clientId;

    private final AtomicBoolean subscribed = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        try {
            setupMessageCallback();
            subscribeToTopicsOnce();
            logger.info("MQTT Service initialized successfully. Client ID: {}", clientId);
            webSocketService.broadcastMqttStatus(true);
        } catch (Exception e) {
            logger.error("Failed to initialize MQTT Service", e);
            webSocketService.broadcastMqttStatus(false);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
                logger.info("MQTT client disconnected and closed");
            }
            webSocketService.broadcastMqttStatus(false);
        } catch (MqttException e) {
            logger.error("Error during MQTT cleanup", e);
        }
    }

    private void setupMessageCallback() throws MqttException {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                logger.error("MQTT connection lost", cause);
                webSocketService.broadcastMqttStatus(false);
                subscribed.set(false);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                try {
                    handleIncomingMessage(topic, message);
                } catch (Exception e) {
                    logger.error("Error handling incoming MQTT message", e);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                logger.debug("MQTT message delivery completed");
            }
        });
    }

    private void subscribeToTopicsOnce() throws MqttException {
        if (!mqttClient.isConnected()) {
            logger.warn("MQTT client not connected yet; deferring subscription");
            return;
        }
        if (subscribed.compareAndSet(false, true)) {
            String telemetryTopic = topicPrefix + "/+/telemetry";
            mqttClient.subscribe(telemetryTopic, 1);
            logger.info("Subscribed to telemetry topic: {}", telemetryTopic);

            String statusTopic = topicPrefix + "/+/status";
            mqttClient.subscribe(statusTopic, 1);
            logger.info("Subscribed to status topic: {}", statusTopic);
        } else {
            logger.debug("Already subscribed; skipping duplicate subscription");
        }
    }

    private void handleIncomingMessage(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            logger.debug("Received MQTT message on topic: {} - Payload: {}", topic, payload);

            String[] topicParts = topic.split("/");
            if (topicParts.length >= 3) {
                String carIdStr = topicParts[1];
                String messageType = topicParts[2];

                Long carId = Long.parseLong(carIdStr);

                switch (messageType) {
                    case "telemetry":
                        handleTelemetryMessage(carId, payload);
                        break;
                    case "status":
                        handleStatusMessage(carId, payload);
                        break;
                    default:
                        logger.warn("Unknown message type: {}", messageType);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing MQTT message", e);
        }
    }

    private void handleTelemetryMessage(Long carId, String payload) {
        try {
            TelemetryDto telemetryDto = objectMapper.readValue(payload, TelemetryDto.class);
            telemetryDto.setCarId(carId);
            telemetryDto.setTimestamp(LocalDateTime.now());

            Car car = carService.getCarById(carId);
            Telemetry telemetry = new Telemetry();
            telemetry.setCar(car);
            telemetry.setSpeed(telemetryDto.getSpeed());
            telemetry.setFuel(telemetryDto.getFuelLevel());
            telemetry.setTemperature(telemetryDto.getTemperature());
            telemetry.setLocation(telemetryDto.getLocation());
            telemetry.setTimestamp(telemetryDto.getTimestamp());

            // Link to active trip by fetching managed entity
            Trip activeTrip = tripService.getActiveTripForCar(carId).orElse(null);
            telemetry.setTrip(activeTrip);

            telemetryService.createTelemetry(telemetry);

            telemetryDto.setTripId(activeTrip != null ? activeTrip.getId() : null);

            Alert createdAlert = checkAndCreateAlerts(car, telemetryDto);

            webSocketService.broadcastTelemetryUpdate(telemetryDto);
            webSocketService.sendTelemetryToCar(carId, telemetryDto);
            webSocketService.broadcastCarLocation(carId, telemetryDto.getLocation());

            if (createdAlert != null && createdAlert.getSeverity() == Alert.AlertSeverity.CRITICAL) {
                webSocketService.sendCriticalAlertToAdmins(createdAlert);
                try {
                    tripService.addFineForCritical(carId, 100);
                } catch (Exception ignore) {
                }
            }

            logger.info("Telemetry processed for car {} with trip {}", carId, telemetryDto.getTripId());

        } catch (Exception e) {
            logger.error("Error handling telemetry message for car {}", carId, e);
        }
    }

    private void handleStatusMessage(Long carId, String payload) {
        try {
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("carId", carId);
            statusUpdate.put("status", payload);
            webSocketService.broadcastSystemStatus(statusUpdate);
        } catch (Exception e) {
            logger.error("Error handling status message for car {}", carId, e);
        }
    }

    private Alert checkAndCreateAlerts(Car car, TelemetryDto telemetryDto) {
        try {
            Alert createdAlert = null;

            // Get the active trip for this car to link alerts
            Trip activeTrip = null;
            try {
                activeTrip = tripService.getActiveTripForCar(car.getId()).orElse(null);
            } catch (Exception e) {
                logger.warn("Could not get active trip for car {}: {}", car.getId(), e.getMessage());
            }

            if (telemetryDto.getFuelLevel() < 20) {
                String msg = "Low fuel level: " + telemetryDto.getFuelLevel() + "%";
                Alert.AlertSeverity sev = telemetryDto.getFuelLevel() < 10 ? Alert.AlertSeverity.CRITICAL
                        : Alert.AlertSeverity.HIGH;
                createdAlert = alertService.createAlert(car, activeTrip, "LOW_FUEL", sev.toString(), msg);
                webSocketService.broadcastAlertUpdate(createdAlert);
                if (sev == Alert.AlertSeverity.HIGH) {
                    try {
                        tripService.addFineForCritical(car.getId(), 10);
                    } catch (Exception ignore) {
                    }
                }
                if (sev == Alert.AlertSeverity.CRITICAL) {
                    try {
                        tripService.addFineForCritical(car.getId(), 20);
                    } catch (Exception ignore) {
                    }
                }
            }
            if (telemetryDto.getTemperature() > 100) {
                String msg = "High temperature: " + telemetryDto.getTemperature() + "°C";
                Alert.AlertSeverity sev = telemetryDto.getTemperature() > 110 ? Alert.AlertSeverity.CRITICAL
                        : Alert.AlertSeverity.HIGH;
                createdAlert = alertService.createAlert(car, activeTrip, "HIGH_TEMPERATURE", sev.toString(), msg);
                webSocketService.broadcastAlertUpdate(createdAlert);
                if (sev == Alert.AlertSeverity.HIGH) {
                    try {
                        tripService.addFineForCritical(car.getId(), 10);
                    } catch (Exception ignore) {
                    }
                }
                if (sev == Alert.AlertSeverity.CRITICAL) {
                    try {
                        tripService.addFineForCritical(car.getId(), 20);
                    } catch (Exception ignore) {
                    }
                }
            }
            if (telemetryDto.getSpeed() > 120) {
                String msg = "High speed: " + telemetryDto.getSpeed() + " km/h";
                Alert.AlertSeverity sev = telemetryDto.getSpeed() > 150 ? Alert.AlertSeverity.CRITICAL
                        : Alert.AlertSeverity.HIGH;
                createdAlert = alertService.createAlert(car, activeTrip, "HIGH_SPEED", sev.toString(), msg);
                webSocketService.broadcastAlertUpdate(createdAlert);
                if (sev == Alert.AlertSeverity.HIGH) {
                    try {
                        tripService.addFineForCritical(car.getId(), 10);
                    } catch (Exception ignore) {
                    }
                }
                if (sev == Alert.AlertSeverity.CRITICAL) {
                    try {
                        tripService.addFineForCritical(car.getId(), 20);
                    } catch (Exception ignore) {
                    }
                }
            }
            return createdAlert;
        } catch (Exception e) {
            logger.error("Error checking alerts for car {}", car.getId(), e);
            return null;
        }
    }

    public CompletableFuture<Void> publishTelemetry(Long carId, TelemetryDto telemetryDto) {
        return CompletableFuture.runAsync(() -> {
            try {
                String topic = topicPrefix + "/" + carId + "/telemetry";
                String payload = objectMapper.writeValueAsString(telemetryDto);
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(1);
                message.setRetained(false);
                mqttClient.publish(topic, message);
            } catch (Exception e) {
                logger.error("Error publishing telemetry for car {}", carId, e);
            }
        });
    }

    public CompletableFuture<Void> publishStatus(Long carId, String status) {
        return CompletableFuture.runAsync(() -> {
            try {
                String topic = topicPrefix + "/" + carId + "/status";
                String payload = "{\"status\":\"" + status + "\",\"timestamp\":\"" + LocalDateTime.now() + "\"}";
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(1);
                message.setRetained(false);
                mqttClient.publish(topic, message);
            } catch (Exception e) {
                logger.error("Error publishing status for car {}", carId, e);
            }
        });
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }
}
