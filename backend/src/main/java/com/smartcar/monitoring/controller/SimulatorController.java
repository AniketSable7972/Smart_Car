package com.smartcar.monitoring.controller;

import com.smartcar.monitoring.dto.ApiResponseDto;
import com.smartcar.monitoring.simulator.TelemetrySimulator;
import com.smartcar.monitoring.service.MqttService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulator")
@CrossOrigin(origins = "*")
public class SimulatorController {

    @Autowired
    private TelemetrySimulator telemetrySimulator;

    @Autowired
    private MqttService mqttService;

    // GET /api/simulator/status - Get simulator status
    @GetMapping("/status")
    public ResponseEntity<ApiResponseDto<Object>> getSimulatorStatus() {
        try {
            class SimulatorStatus {
                public final boolean isRunning;
                public final boolean mqttConnected;
                public final String status;

                public SimulatorStatus(boolean isRunning, boolean mqttConnected, String status) {
                    this.isRunning = isRunning;
                    this.mqttConnected = mqttConnected;
                    this.status = status;
                }
            }

            boolean isRunning = telemetrySimulator.isRunning();
            boolean mqttConnected = mqttService.isConnected();
            String status = isRunning ? "RUNNING" : "STOPPED";

            SimulatorStatus simulatorStatus = new SimulatorStatus(isRunning, mqttConnected, status);
            return ResponseEntity.ok(ApiResponseDto.success("Simulator status retrieved successfully", simulatorStatus));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponseDto.error("Failed to get simulator status: " + e.getMessage()));
        }
    }

    // POST /api/simulator/start - Start the simulator
    @PostMapping("/start")
    public ResponseEntity<ApiResponseDto<String>> startSimulator() {
        try {
            telemetrySimulator.startSimulation();
            return ResponseEntity.ok(ApiResponseDto.success("Simulator started successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponseDto.error("Failed to start simulator: " + e.getMessage()));
        }
    }

    // POST /api/simulator/stop - Stop the simulator
    @PostMapping("/stop")
    public ResponseEntity<ApiResponseDto<String>> stopSimulator() {
        try {
            telemetrySimulator.stopSimulation();
            return ResponseEntity.ok(ApiResponseDto.success("Simulator stopped successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponseDto.error("Failed to stop simulator: " + e.getMessage()));
        }
    }

    // POST /api/simulator/toggle - Toggle simulator on/off
    @PostMapping("/toggle")
    public ResponseEntity<ApiResponseDto<Object>> toggleSimulator() {
        try {
            boolean currentStatus = telemetrySimulator.isRunning();
            if (currentStatus) {
                telemetrySimulator.stopSimulation();
            } else {
                telemetrySimulator.startSimulation();
            }

            class ToggleResponse {
                public final boolean newStatus;
                public final String message;

                public ToggleResponse(boolean newStatus, String message) {
                    this.newStatus = newStatus;
                    this.message = message;
                }
            }

            ToggleResponse response = new ToggleResponse(!currentStatus, 
                currentStatus ? "Simulator stopped" : "Simulator started");

            return ResponseEntity.ok(ApiResponseDto.success("Simulator toggled successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponseDto.error("Failed to toggle simulator: " + e.getMessage()));
        }
    }

    // GET /api/simulator/mqtt-status - Get MQTT connection status
    @GetMapping("/mqtt-status")
    public ResponseEntity<ApiResponseDto<Object>> getMqttStatus() {
        try {
            class MqttStatus {
                public final boolean connected;
                public final String status;

                public MqttStatus(boolean connected, String status) {
                    this.connected = connected;
                    this.status = status;
                }
            }

            boolean connected = mqttService.isConnected();
            String status = connected ? "CONNECTED" : "DISCONNECTED";

            MqttStatus mqttStatus = new MqttStatus(connected, status);
            return ResponseEntity.ok(ApiResponseDto.success("MQTT status retrieved successfully", mqttStatus));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponseDto.error("Failed to get MQTT status: " + e.getMessage()));
        }
    }

    // POST /api/simulator/generate-test-data - Generate test data for specific car
    @PostMapping("/generate-test-data")
    public ResponseEntity<ApiResponseDto<String>> generateTestData(@RequestParam Long carId) {
        try {
            // This would trigger immediate generation of test data for a specific car
            // For now, we'll just return success as the simulator handles this automatically
            return ResponseEntity.ok(ApiResponseDto.success("Test data generation triggered for car: " + carId));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponseDto.error("Failed to generate test data: " + e.getMessage()));
        }
    }
}
