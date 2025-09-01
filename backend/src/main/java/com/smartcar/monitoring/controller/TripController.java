package com.smartcar.monitoring.controller;

import com.smartcar.monitoring.dto.ApiResponseDto;
import com.smartcar.monitoring.dto.TripDto;
import com.smartcar.monitoring.dto.TripRequestDto;
import com.smartcar.monitoring.model.TripCost;
import com.smartcar.monitoring.repository.TripCostRepository;
import com.smartcar.monitoring.service.TripService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "*")
public class TripController {

    @Autowired
    private TripService tripService;

    @Autowired
    private TripCostRepository tripCostRepository;

    @PostMapping("/request")
    public ResponseEntity<ApiResponseDto<TripDto>> requestTrip(@Valid @RequestBody TripRequestDto req) {
        try {
            TripDto dto = tripService.requestTrip(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success("Trip requested", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to request trip: " + e.getMessage()));
        }
    }

    @PostMapping("/{tripId}/approve-start")
    public ResponseEntity<ApiResponseDto<TripDto>> approveAndStart(@PathVariable Long tripId,
            @RequestParam(required = false) Long carId) {
        try {
            TripDto dto = tripService.approveAndStart(tripId, carId);
            return ResponseEntity.ok(ApiResponseDto.success("Trip approved and started", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to approve/start trip: " + e.getMessage()));
        }
    }

    @PostMapping("/{tripId}/reject")
    public ResponseEntity<ApiResponseDto<TripDto>> reject(@PathVariable Long tripId,
            @RequestParam(required = false) String reason) {
        try {
            TripDto dto = tripService.reject(tripId, reason);
            return ResponseEntity.ok(ApiResponseDto.success("Trip rejected", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to reject trip: " + e.getMessage()));
        }
    }

    @PostMapping("/{tripId}/stop")
    public ResponseEntity<ApiResponseDto<TripDto>> stop(@PathVariable Long tripId) {
        try {
            TripDto dto = tripService.stop(tripId);
            return ResponseEntity.ok(ApiResponseDto.success("Trip stopped", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to stop trip: " + e.getMessage()));
        }
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<ApiResponseDto<List<TripDto>>> driverTrips(@PathVariable Long driverId) {
        try {
            List<TripDto> list = tripService.listDriverTrips(driverId);
            return ResponseEntity.ok(ApiResponseDto.success("Trips retrieved", list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve trips: " + e.getMessage()));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponseDto<List<TripDto>>> pendingTrips() {
        try {
            List<TripDto> list = tripService.listPendingTrips();
            return ResponseEntity.ok(ApiResponseDto.success("Pending trips retrieved", list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve pending trips: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponseDto<List<TripDto>>> activeTrips() {
        try {
            List<TripDto> list = tripService.listActiveTrips();
            return ResponseEntity.ok(ApiResponseDto.success("Active trips retrieved", list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve active trips: " + e.getMessage()));
        }
    }

    @GetMapping("/active/driver/{driverId}")
    public ResponseEntity<ApiResponseDto<TripDto>> activeTripForDriver(@PathVariable Long driverId) {
        try {
            return tripService.getActiveTripForDriver(driverId)
                    .map(t -> ResponseEntity.ok(ApiResponseDto.success("Active trip retrieved", new TripDto(t))))
                    .orElse(ResponseEntity.ok(ApiResponseDto.success("No active trip", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve active trip: " + e.getMessage()));
        }
    }

    @GetMapping("/completed")
    public ResponseEntity<ApiResponseDto<List<TripDto>>> completedTrips() {
        try {
            List<TripDto> list = tripService.listCompletedTrips();
            return ResponseEntity.ok(ApiResponseDto.success("Completed trips retrieved", list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve completed trips: " + e.getMessage()));
        }
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<ApiResponseDto<TripDto>> getTripById(@PathVariable Long tripId) {
        try {
            TripDto tripDto = tripService.getTripById(tripId);
            return ResponseEntity.ok(ApiResponseDto.success("Trip details retrieved", tripDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("Trip not found: " + e.getMessage()));
        }
    }

    @GetMapping("/costs")
    public ResponseEntity<ApiResponseDto<List<Map<String, Object>>>> listCosts() {
        try {
            List<Map<String, Object>> costs = tripCostRepository.findByIsActiveTrue().stream().map(tc -> {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", tc.getId());
                m.put("startPoint", tc.getStartPoint());
                m.put("endPoint", tc.getEndPoint());
                m.put("baseCost", tc.getBaseCost());
                return m;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Trip costs retrieved", costs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve costs: " + e.getMessage()));
        }
    }
}