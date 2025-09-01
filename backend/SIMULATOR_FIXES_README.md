# Simulator and Telemetry System Fixes

## Overview
This document outlines the fixes implemented to resolve issues with the telemetry simulator and trip data tracking.

## Issues Fixed

### 1. Simulator Logic Fixed
- **Problem**: Simulator was using `is_active` column from cars table instead of `status` column
- **Solution**: Updated `TelemetrySimulator.java` to only generate telemetry for cars with `ACTIVE` status
- **Result**: Telemetry is now only generated when cars are assigned to active trips

### 2. Trip ID Properly Set
- **Problem**: Trip ID was showing as null in telemetry data
- **Solution**: 
  - Added `getActiveTripForCar()` method to `TripService`
  - Added `findFirstByCarIdAndStatus()` method to `TripRepository`
  - Updated simulator to fetch active trip for each car and set trip_id
- **Result**: All telemetry data now has proper trip_id linking

### 3. Alert System Enhanced
- **Problem**: Alerts were not linked to trips, causing history tracking issues
- **Solution**:
  - Added `trip_id` field to `Alert` model
  - Updated `AlertService.createAlert()` to accept trip parameter
  - Modified `MqttService.checkAndCreateAlerts()` to pass trip information
- **Result**: Alerts are now properly linked to trips for better history tracking

### 4. Simulator Notification
- **Problem**: Simulator wasn't notified when trips started
- **Solution**: Added `notifySimulatorTripStarted()` method in `TripService` to send notifications when trips are approved
- **Result**: Simulator is now properly notified to start generating telemetry for new trips

## Database Changes

### New Fields Added
- `Alert.trip_id` - Links alerts to specific trips

### Database Cleanup
A cleanup script `db-cleanup.sql` has been provided to remove any existing `car_model` and `car_number` columns from the telemetry table if they exist.

## How It Works Now

1. **Trip Approval**: When admin approves a trip request:
   - Car status is set to `ACTIVE`
   - Simulator is notified via WebSocket
   - Trip status is set to `ACTIVE`

2. **Telemetry Generation**: Simulator now:
   - Only processes cars with `ACTIVE` status
   - Fetches the active trip for each car
   - Sets proper `trip_id` in telemetry data
   - Generates realistic sensor data

3. **Alert Creation**: When alerts are created:
   - Trip information is automatically linked
   - History can be properly tracked by trip
   - Critical alerts trigger fines for the specific trip

## Files Modified

### Core Models
- `Telemetry.java` - Already correct (no car_model/car_number fields)
- `Alert.java` - Added trip_id field
- `Car.java` - No changes needed
- `Trip.java` - No changes needed

### Services
- `TelemetrySimulator.java` - Fixed logic to use status column
- `TripService.java` - Added trip notification and helper methods
- `AlertService.java` - Enhanced to support trip linking
- `MqttService.java` - Updated alert creation with trip info

### Repositories
- `TripRepository.java` - Added findFirstByCarIdAndStatus method

## Testing

To test the fixes:

1. **Start the application**
2. **Create a car** (status will be `IDLE`)
3. **Request a trip** (status will be `REQUESTED`)
4. **Approve the trip** (car status becomes `ACTIVE`, trip status becomes `ACTIVE`)
5. **Verify telemetry generation** - should only start after trip approval
6. **Check trip_id** - should be properly set in all telemetry records
7. **Verify alerts** - should be linked to the specific trip

## Database Cleanup

If you have existing data with car_model/car_number in telemetry table:

```bash
# Connect to your MySQL database
mysql -u root -p smart_car_monitoring

# Run the cleanup script
source /path/to/backend/src/main/resources/db-cleanup.sql
```

## Configuration

The simulator can be configured via `application.properties`:

```properties
simulator.enabled=true
simulator.interval=5000
```

## Monitoring

Check the logs for:
- `"Generating telemetry for car X with trip Y"`
- `"SIMULATOR NOTIFICATION: Trip X started for car Y"`
- `"Telemetry processed for car X with trip Y"`

## Benefits

1. **Accurate Data**: Telemetry only generated for active trips
2. **Proper Tracking**: All data linked to specific trips
3. **Better History**: Complete audit trail for trips
4. **Efficient Simulation**: No unnecessary data generation
5. **Proper Alerts**: Alerts linked to specific trips for better management