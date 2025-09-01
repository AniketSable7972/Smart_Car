package com.smartcar.monitoring.service;

import com.smartcar.monitoring.dto.TripDto;
import com.smartcar.monitoring.dto.TripRequestDto;
import com.smartcar.monitoring.exception.DriverNotFoundException;
import com.smartcar.monitoring.exception.UserNotFoundException;
import com.smartcar.monitoring.model.*;
import com.smartcar.monitoring.model.Trip.TripStatus;
import com.smartcar.monitoring.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TripService {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private TripCostRepository tripCostRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private WebSocketService webSocketService;

    public Optional<Trip> getById(Long id) {
        return tripRepository.findById(id);
    }

    public TripDto getTripById(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found with ID: " + tripId));

        return new TripDto(trip);
    }

    public TripDto requestTrip(TripRequestDto req) {
        Driver driver = driverRepository.findById(req.getDriverId())
                .orElseThrow(() -> new DriverNotFoundException("Driver not found: " + req.getDriverId()));

        Trip trip = new Trip();
        trip.setDriver(driver);
        trip.setStartPoint(req.getStartPoint());
        trip.setEndPoint(req.getEndPoint());
        trip.setStatus(TripStatus.REQUESTED);
        trip.setRequestedAt(LocalDateTime.now());

        // Pre-calc base cost if available
        tripCostRepository.findByStartPointAndEndPointAndIsActiveTrue(req.getStartPoint(), req.getEndPoint())
                .ifPresent(tc -> {
                    trip.setBaseCost(tc.getBaseCost());
                    trip.setTotalCost(tc.getBaseCost());
                });

        if (req.getCarId() != null) {
            carRepository.findById(req.getCarId()).ifPresent(trip::setCar);
        }

        Trip saved = tripRepository.save(trip);
        return new TripDto(saved);
    }

    public TripDto approveAndStart(Long tripId, Long carId) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> new RuntimeException("Trip not found"));
        if (carId != null) {
            Car car = carRepository.findById(carId).orElseThrow(() -> new RuntimeException("Car not found"));
            trip.setCar(car);
        }
        trip.setStatus(TripStatus.APPROVED);
        trip.setApprovedAt(LocalDateTime.now());
        trip = tripRepository.save(trip);

        // start
        trip.setStatus(TripStatus.ACTIVE);
        trip.setStartedAt(LocalDateTime.now());
        trip = tripRepository.save(trip);

        // assign driver to car for duration of trip and set car ACTIVE
        if (trip.getCar() != null) {
            Driver driver = trip.getDriver();
            if (driver != null) {
                driver.setAssignedCarId(trip.getCar().getId());
                driverRepository.save(driver);
            }
            Car car = trip.getCar();
            car.setStatus("ACTIVE");
            if (driver != null) {
                car.setDriver(driver);
            }
            carRepository.save(car);
        }

        // Broadcast
        {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("type", "TRIP_STARTED");
            payload.put("tripId", trip.getId());
            payload.put("driverId", trip.getDriver().getId());
            webSocketService.broadcastSystemStatus(payload);
        }

        // Notify simulator to start generating telemetry for this trip
        notifySimulatorTripStarted(trip);

        return new TripDto(trip);
    }

    public TripDto reject(Long tripId, String reason) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> new RuntimeException("Trip not found"));
        trip.setStatus(TripStatus.REJECTED);
        trip.setEndedAt(LocalDateTime.now());
        Trip saved = tripRepository.save(trip);
        webSocketService.sendNotificationToUser(
                trip.getDriver().getUser().getUsername(),
                "Your trip request was rejected" + (reason != null ? (": " + reason) : ""));
        return new TripDto(saved);
    }

    public TripDto stop(Long tripId) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> new RuntimeException("Trip not found"));
        trip.setStatus(TripStatus.COMPLETED);
        trip.setEndedAt(LocalDateTime.now());
        Trip saved = tripRepository.save(trip);

        // unassign driver from car at end of trip and set car IDLE
        Driver driver = saved.getDriver();
        if (driver != null) {
            driver.setAssignedCarId(null);
            driverRepository.save(driver);
        }
        if (saved.getCar() != null) {
            Car car = saved.getCar();
            car.setStatus("IDLE");
            car.setDriver(null);
            carRepository.save(car);
        }

        {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("type", "TRIP_COMPLETED");
            payload.put("tripId", trip.getId());
            payload.put("driverId", trip.getDriver().getId());
            webSocketService.broadcastSystemStatus(payload);
        }
        return new TripDto(saved);
    }

    public List<TripDto> listDriverTrips(Long driverId) {
        return tripRepository.findByDriverIdAndIsActiveTrue(driverId).stream().map(TripDto::new)
                .collect(Collectors.toList());
    }

    public List<TripDto> listPendingTrips() {
        return tripRepository.findByStatus(TripStatus.REQUESTED).stream().map(TripDto::new)
                .collect(Collectors.toList());
    }

    public List<TripDto> listActiveTrips() {
        return tripRepository.findByStatus(TripStatus.ACTIVE).stream().map(TripDto::new).collect(Collectors.toList());
    }

    public List<TripDto> listCompletedTrips() {
        return tripRepository.findByStatus(TripStatus.COMPLETED).stream().map(TripDto::new)
                .collect(Collectors.toList());
    }

    public Optional<Trip> getActiveTripForDriver(Long driverId) {
        return tripRepository.findFirstByDriverIdAndStatus(driverId, TripStatus.ACTIVE);
    }

    public Optional<Trip> getActiveTripForCar(Long carId) {
        return tripRepository.findFirstByCarIdAndStatus(carId, TripStatus.ACTIVE);
    }

    public void addFineForCritical(Long carId, int fineAmountRs) {
        // Add fine to the active trip associated with this car
        List<Trip> active = tripRepository.findByCarIdAndStatus(carId, TripStatus.ACTIVE);
        for (Trip t : active) {
            BigDecimal fine = t.getAdditionalFine() == null ? BigDecimal.ZERO : t.getAdditionalFine();
            BigDecimal add = new BigDecimal(fineAmountRs);
            t.setAdditionalFine(fine.add(add));

            BigDecimal base = t.getBaseCost() == null ? BigDecimal.ZERO : t.getBaseCost();
            t.setTotalCost(base.add(t.getAdditionalFine()));
            tripRepository.save(t);
        }
    }

    /**
     * Notify the simulator that a trip has started and telemetry should be
     * generated
     */
    private void notifySimulatorTripStarted(Trip trip) {
        if (trip.getCar() != null) {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("type", "SIMULATOR_TRIP_STARTED");
            payload.put("tripId", trip.getId());
            payload.put("carId", trip.getCar().getId());
            payload.put("driverId", trip.getDriver().getId());
            payload.put("message",
                    "Start generating telemetry for trip " + trip.getId() + " and car " + trip.getCar().getId());
            webSocketService.broadcastSystemStatus(payload);

            // Log for debugging
            System.out.println(
                    "SIMULATOR NOTIFICATION: Trip " + trip.getId() + " started for car " + trip.getCar().getId());
        }
    }
}