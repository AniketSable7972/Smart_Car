package com.smartcar.monitoring.simulator;

import com.smartcar.monitoring.dto.TelemetryDto;
import com.smartcar.monitoring.model.Car;
import com.smartcar.monitoring.model.Trip;
import com.smartcar.monitoring.service.CarService;
import com.smartcar.monitoring.service.MqttService;
import com.smartcar.monitoring.service.TripService;
import com.smartcar.monitoring.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class TelemetrySimulator {

    private static final Logger logger = LoggerFactory.getLogger(TelemetrySimulator.class);

    @Autowired
    private MqttService mqttService;

    @Autowired
    private CarService carService;

    @Autowired
    private TripService tripService;

    @Autowired
    private WebSocketService webSocketService;

    @Value("${simulator.enabled:true}")
    private boolean simulatorEnabled;

    @Value("${simulator.interval:5000}")
    private long simulatorInterval;

    private final Random random = new Random();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private final String[] locations = {
        "Shivajinagar, Pune", "Kothrud, Pune", "Hinjewadi, Pune", "Viman Nagar, Pune", "Kalyani Nagar, Pune"
    };

    private final Map<Long, TelemetryDto> lastByCarId = new HashMap<>();
    private final Map<Long, ProfileState> profileByCarId = new HashMap<>();

    private static class ProfileState {
        int speedDir = +1;
        int tempDir = +1;
        boolean fuelResetPending = false;
    }

    @PostConstruct
    public void init() {
        if (simulatorEnabled) {
            logger.info("Telemetry Simulator initialized. Interval: {}ms", simulatorInterval);
            startSimulation();
        } else {
            logger.info("Telemetry Simulator is disabled");
        }
    }

    @Scheduled(fixedDelayString = "${simulator.interval:5000}")
    public void simulateTelemetry() {
        if (!simulatorEnabled || !isRunning.get()) return;

        try {
            List<Car> activeCars = carService.getCarsByStatus("ACTIVE");

            if (activeCars.isEmpty()) {
                logger.debug("No cars with ACTIVE status; skipping telemetry generation");
                return;
            }

            for (Car car : activeCars) {
                Optional<Trip> activeTripOpt = tripService.getActiveTripForCar(car.getId());
                if (activeTripOpt.isEmpty()) {
                    lastByCarId.remove(car.getId());
                    profileByCarId.remove(car.getId());
                    continue;
                }

                Trip activeTrip = activeTripOpt.get();

                TelemetryDto last = lastByCarId.get(car.getId());

                // Reset telemetry if trip ID has changed
                if (last != null && !Objects.equals(last.getTripId(), activeTrip.getId())) {
                    lastByCarId.remove(car.getId());
                    profileByCarId.remove(car.getId());
                    last = null;
                }

                TelemetryDto base = last != null ? last : initialFromCar(car);
                TelemetryDto next = smoothAdvance(base, car.getId());
                next.setCarId(car.getId());
                next.setTripId(activeTrip.getId());
                lastByCarId.put(car.getId(), next);

                mqttService.publishTelemetry(car.getId(), next);
            }

        } catch (Exception e) {
            logger.error("Error in telemetry simulation", e);
        }
    }

    private TelemetryDto initialFromCar(Car car) {
        TelemetryDto t = new TelemetryDto();
        t.setSpeed(0); // Always start from 0
        t.setFuelLevel(100); // Full tank
        t.setTemperature(30); // Default temp
        t.setLocation(locations[random.nextInt(locations.length)]);
        t.setTimestamp(LocalDateTime.now());
        return t;
    }

    private TelemetryDto smoothAdvance(TelemetryDto prev, Long carId) {
        ProfileState ps = profileByCarId.computeIfAbsent(carId, k -> new ProfileState());

        int targetMaxSpeed = 120;
        int targetMinSpeed = 60;
        int speedStep = 3;
        int nextSpeed = prev.getSpeed() + (ps.speedDir > 0 ? speedStep : -speedStep);
        if (ps.speedDir > 0 && nextSpeed >= targetMaxSpeed) { ps.speedDir = -1; nextSpeed = targetMaxSpeed; }
        if (ps.speedDir < 0 && nextSpeed <= targetMinSpeed) { ps.speedDir = +1; nextSpeed = targetMinSpeed; }
        nextSpeed = clamp(nextSpeed, 0, 170);

        int targetMaxTemp = 120;
        int targetMinTemp = 80;
        int tempStep = 2;
        int nextTemp = prev.getTemperature() + (ps.tempDir > 0 ? tempStep : -tempStep);
        if (ps.tempDir > 0 && nextTemp >= targetMaxTemp) { ps.tempDir = -1; nextTemp = targetMaxTemp; }
        if (ps.tempDir < 0 && nextTemp <= targetMinTemp) { ps.tempDir = +1; nextTemp = targetMinTemp; }
        nextTemp = clamp(nextTemp, -5, 130);

        int nextFuel;
        if (ps.fuelResetPending) {
            nextFuel = 100;
            ps.fuelResetPending = false;
        } else {
            nextFuel = clamp(prev.getFuelLevel() - randBetween(1, 2), 0, 100);
            if (nextFuel == 0) { ps.fuelResetPending = true; }
        }

        String location = nextLocation(prev.getLocation());
        TelemetryDto t = new TelemetryDto();
        t.setSpeed(nextSpeed);
        t.setFuelLevel(nextFuel);
        t.setTemperature(nextTemp);
        t.setLocation(location);
        t.setTimestamp(LocalDateTime.now());
        return t;
    }

    private String nextLocation(String current) {
        int idx = 0;
        for (int i = 0; i < locations.length; i++) {
            if (locations[i].equals(current)) {
                idx = i;
                break;
            }
        }
        return locations[(idx + 1) % locations.length];
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private int randBetween(int a, int b) {
        return a + random.nextInt(Math.max(1, b - a + 1));
    }

    public void startSimulation() {
        if (isRunning.compareAndSet(false, true)) {
            logger.info("Telemetry simulation started");
            webSocketService.broadcastSimulatorStatus(true);
        }
    }

    public void stopSimulation() {
        if (isRunning.compareAndSet(true, false)) {
            logger.info("Telemetry simulation stopped");
            webSocketService.broadcastSimulatorStatus(false);
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public void setSimulatorEnabled(boolean enabled) {
        this.simulatorEnabled = enabled;
        if (enabled) {
            startSimulation();
        } else {
            stopSimulation();
        }
        logger.info("Telemetry simulator enabled: {}", enabled);
    }

    public void setSimulatorInterval(long interval) {
        this.simulatorInterval = interval;
        logger.info("Telemetry simulator interval updated: {}ms", interval);
    }
}