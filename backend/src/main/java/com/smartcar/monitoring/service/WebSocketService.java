package com.smartcar.monitoring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcar.monitoring.dto.TelemetryDto;
import com.smartcar.monitoring.model.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // Send telemetry updates to all connected clients
    public void broadcastTelemetryUpdate(TelemetryDto telemetryDto) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "TELEMETRY_UPDATE");
            message.put("data", telemetryDto);
            message.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSend("/topic/telemetry", message);
            logger.debug("Broadcasted telemetry update for car: {}", telemetryDto.getCarId());
        } catch (Exception e) {
            logger.error("Error broadcasting telemetry update", e);
        }
    }

    // Send telemetry updates to specific car subscribers
    public void sendTelemetryToCar(Long carId, TelemetryDto telemetryDto) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "CAR_TELEMETRY");
            message.put("carId", carId);
            message.put("data", telemetryDto);
            message.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSend("/topic/car/" + carId + "/telemetry", message);
            logger.debug("Sent telemetry update to car {} subscribers", carId);
        } catch (Exception e) {
            logger.error("Error sending telemetry to car {}", carId, e);
        }
    }

    // Send alert updates to all connected clients
    public void broadcastAlertUpdate(Alert alert) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "ALERT_UPDATE");
            message.put("data", alert);
            message.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSend("/topic/alerts", message);
            logger.debug("Broadcasted alert update: {}", alert.getType());
        } catch (Exception e) {
            logger.error("Error broadcasting alert update", e);
        }
    }

    // Send critical alerts to admin dashboard
    public void sendCriticalAlertToAdmins(Alert alert) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "CRITICAL_ALERT");
            message.put("data", alert);
            message.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSend("/topic/admin/critical-alerts", message);
            logger.debug("Sent critical alert to admin dashboard: {}", alert.getType());
        } catch (Exception e) {
            logger.error("Error sending critical alert to admins", e);
        }
    }

    // Send system status updates
    public void broadcastSystemStatus(Map<String, Object> status) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "SYSTEM_STATUS");
            message.put("data", status);
            message.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSend("/topic/system/status", message);
            logger.debug("Broadcasted system status update");
        } catch (Exception e) {
            logger.error("Error broadcasting system status", e);
        }
    }

    // Send MQTT connection status
    public void broadcastMqttStatus(boolean connected) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "MQTT_STATUS");
            message.put("connected", connected);
            message.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSend("/topic/system/mqtt-status", message);
            logger.debug("Broadcasted MQTT status: {}", connected);
        } catch (Exception e) {
            logger.error("Error broadcasting MQTT status", e);
        }
    }

    // Send simulator status updates
    public void broadcastSimulatorStatus(boolean running) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "SIMULATOR_STATUS");
            message.put("running", running);
            message.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSend("/topic/system/simulator-status", message);
            logger.debug("Broadcasted simulator status: {}", running);
        } catch (Exception e) {
            logger.error("Error broadcasting simulator status", e);
        }
    }

    // Send dashboard statistics updates
    public void broadcastDashboardStats(Map<String, Object> stats) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "DASHBOARD_STATS");
            message.put("data", stats);
            message.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSend("/topic/dashboard/stats", message);
            logger.debug("Broadcasted dashboard statistics");
        } catch (Exception e) {
            logger.error("Error broadcasting dashboard stats", e);
        }
    }

    // Send user-specific notifications
    public void sendNotificationToUser(String username, String message) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "NOTIFICATION");
            notification.put("message", message);
            notification.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSendToUser(username, "/queue/notifications", notification);
            logger.debug("Sent notification to user: {}", username);
        } catch (Exception e) {
            logger.error("Error sending notification to user {}", username, e);
        }
    }

    // Send real-time car location updates
    public void broadcastCarLocation(Long carId, String location) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "CAR_LOCATION");
            message.put("carId", carId);
            message.put("location", location);
            message.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSend("/topic/car/" + carId + "/location", message);
            messagingTemplate.convertAndSend("/topic/map/locations", message);
            logger.debug("Broadcasted car location update for car: {}", carId);
        } catch (Exception e) {
            logger.error("Error broadcasting car location for car {}", carId, e);
        }
    }

    // Send heartbeat/ping to keep connections alive
    public void sendHeartbeat() {
        try {
            Map<String, Object> heartbeat = new HashMap<>();
            heartbeat.put("type", "HEARTBEAT");
            heartbeat.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSend("/topic/heartbeat", heartbeat);
        } catch (Exception e) {
            logger.error("Error sending heartbeat", e);
        }
    }
}
