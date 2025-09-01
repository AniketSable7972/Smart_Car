-- Database cleanup script to remove car_model and car_number from telemetry table
-- This script should be run manually if the database has these columns

-- Check if car_model column exists in telemetry table and remove it
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'smart_car_monitoring' 
     AND TABLE_NAME = 'telemetry' 
     AND COLUMN_NAME = 'car_model') > 0,
    'ALTER TABLE telemetry DROP COLUMN car_model',
    'SELECT "car_model column does not exist in telemetry table" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check if car_number column exists in telemetry table and remove it
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'smart_car_monitoring' 
     AND TABLE_NAME = 'telemetry' 
     AND COLUMN_NAME = 'car_number') > 0,
    'ALTER TABLE telemetry DROP COLUMN car_number',
    'SELECT "car_number column does not exist in telemetry table" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verify the current structure of telemetry table
DESCRIBE telemetry;

-- Show sample data to verify trip_id is being set correctly
SELECT 
    t.id,
    t.car_id,
    t.trip_id,
    t.timestamp,
    t.speed,
    t.fuel,
    t.temperature,
    t.location,
    c.status as car_status,
    tr.status as trip_status
FROM telemetry t
LEFT JOIN cars c ON t.car_id = c.id
LEFT JOIN trips tr ON t.trip_id = tr.id
ORDER BY t.timestamp DESC
LIMIT 10;