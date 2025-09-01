FRONTEND

AdminDashboard:

🔍 Search & Refresh Features

A search bar to filter vehicles by ID, driver name, or location.
A Refresh button that simulates data updates (changing speed, fuel level, engine temperature) and supports auto-refresh every 30 seconds.
📊 Dashboard Overview Cards

4 visual summary cards showing:
Count of active vehicles on the road.
Count of idle vehicles parked or stationary.
Count of vehicles under maintenance.
Number of alerts, triggered when fuel is below 25% or engine temperature exceeds 100°F.
📋 Vehicle Data Table

A table showing:
Vehicle ID, driver, location, speed (mph), fuel level (%), engine temperature (°F), status, and last update time.
Use icons (e.g. map pin, fuel, thermometer) next to location, fuel, and temperature values.
Use badges to style status values (active/idle/maintenance).
Apply conditional color styles:
Fuel: green (>50%), yellow (25–50%), red (<25%)
Temperature: green (≤95°F), yellow (>95°F), red (>100°F)

AlertsPage:

👤 Role-Based Behavior

Accept a user prop with a role (either 'DRIVER' or 'ADMIN').
If the role is 'DRIVER', show only alerts related to the user's assigned vehicle.
If the role is 'ADMIN', display all alerts and enable administrative actions like acknowledging and resolving alerts.
🚦 Alert Data Properties

Each alert includes:

id: Unique alert ID
vehicleId: Associated vehicle
driver: Driver name
type: One of 'fuel', 'temperature', 'maintenance', 'speed', 'system'
severity: 'low', 'medium', 'high', 'critical'
status: 'active', 'acknowledged', 'resolved'
message: Alert description
timestamp: Date and time of alert
location: Optional vehicle location
🎯 Filter & Search Functionality

Provide dropdowns to filter alerts by status and severity
Include a search bar to match message, vehicleId, or driver
🧠 Computed Metrics

Display counts for active, acknowledged, resolved, and critical alerts
Show badges or visual indicators for severity and status
🖼️ UI Elements

Use Tailwind CSS for layout and styling, and custom components:

Card for summary panels
Badge for status and severity indicators
Select, Input, and Table for filters and data view
Use Lucide icons (e.g. AlertTriangle, Fuel, Zap) for alert types and visual emphasis
📋 Table with Actions

Tabulate all alerts with columns: Type (with icon), Vehicle, Message, Severity, Status, Timestamp
If user.role === 'ADMIN', add "Actions" column with buttons:
Acknowledge: Marks alert as acknowledged
Resolve: Marks alert as resolved
Only show Acknowledge button for active alerts
Only show Resolve button for non-resolved alerts
📱 Responsive Layout

Layout should adapt across screen sizes: use grid for summary cards and wrap filters

AnalyticsPage:

Objective: Design and implement a responsive fleet analytics dashboard using React, Recharts, and Lucide icons, showcasing key performance metrics, interactive filters, and visual insights for vehicle data.

📋 Requirements

1. Framework & Libraries

Use React functional components with hooks (useState)
Integrate Recharts for data visualization (LineChart, BarChart, PieChart)
Use Lucide-react icons for visual cues
UI components like Card, Select, etc., should be imported from a custom UI library (./ui/...) 2. Dashboard Layout

Responsive grid layout using Tailwind CSS classes (grid-cols, space-y, p-6, etc.)
Top section includes:
Title: "Fleet Analytics"
Description: "Comprehensive insights and performance metrics for your fleet"
Filters:
Vehicle selector (All Vehicles, CAR001, CAR002, etc.)
Time range selector (Last 7 days, Last 30 days, etc.) 3. Key Metrics Cards

Display four summary cards with icons and trend indicators:

Fleet Efficiency (MPG) – with upward trend
Total Distance (mi) – with upward trend
Active Hours (hrs) – with downward trend
Active Alerts – with upward trend and red highlight
Each card includes:

Title and icon
Bold metric value
Small trend indicator with percentage change 4. Charts Section

Two-column grid layout with four charts:

Fuel Efficiency Trend (LineChart)
Monthly efficiency vs target
Blue line for actual, red dashed line for target
Speed Analysis by Vehicle (BarChart)
Avg and max speed per vehicle
Blue and red bars respectively
Engine Temperature Patterns (LineChart)
Temperature readings across time of day
Orange line
Alert Distribution (PieChart)
Breakdown of alert types with custom colors
Tooltip and percentage labels 5. Fleet Performance Summary

Two-column summary card:

✅ Performing Well
Highlights vehicles and metrics performing above expectations
⚠️ Needs Attention
Flags vehicles and metrics needing improvement
Each section uses:

Colored background and border
Bullet points for insights

DriverDashboard:

Objective: Build a responsive Driver Dashboard using React, Lucide icons, and custom UI components to display real-time telemetry data, active alerts, and daily driving statistics for a vehicle assigned to a user.

📦 Tech Stack & Libraries

React Functional Components with Hooks (useState, useEffect)
Lucide-react icons for visual indicators
Custom UI components: Card, Badge, Progress from ./ui/...
Tailwind CSS for layout and styling
🧑‍💼 Component: DriverDashboard

Props:

user: User – includes assignedCarId to identify the vehicle
State:

telemetry: TelemetryData – includes:
speed (mph)
fuelLevel (%)
engineTemp (°F)
location (lat, lng, address)
lastUpdate (Date)
alerts: Alert[] – array of active alerts with type (warning, info), message, and timestamp
Behavior:

Simulates real-time updates every 10 seconds:
Speed fluctuates between 0–80 mph
Fuel level gradually decreases
Engine temperature varies between 80–120°F
lastUpdate timestamp refreshes
🖥️ Dashboard Layout

1. Header Section

Title: "Driver Dashboard"
Subtitle: Vehicle ID from user.assignedCarId
Last updated time (formatted) 2. Telemetry Cards (4 Cards in Grid)

Each card includes:

Title and Lucide icon
Bold metric value
Contextual info or thresholds
Cards:

🚀 Current Speed
Value in mph
Speed limit note
⛽ Fuel Level
Percentage with color-coded text (green, yellow, red)
Progress bar visualization
🌡️ Engine Temp
Temperature in °F with color-coded status
Normal range note
📍 Location
Address (truncated if long)
Latitude and longitude 3. Active Alerts Section

Title: "Active Alerts" with warning icon
Description: "Important notifications for your vehicle"
List of alerts:
Icon color based on type
Message and timestamp
Badge indicating severity (destructive or secondary)
If no alerts: display fallback message 4. Quick Stats Section (3 Cards in Grid)

Each card includes:

Title and icon
Bold metric value
Contextual note or trend
Cards:

🚗 Today's Distance
Miles driven
Trend indicator (e.g., +12% from yesterday)
⏱️ Drive Time
Total time driven today
Start time
📊 Avg Speed
Average speed including stops

HistoryPage:

Objective: Create a responsive Trip History Dashboard using React, Lucide icons, and custom UI components to display historical trip data, performance metrics, and export capabilities tailored to both drivers and fleet managers.

🧰 Tech Stack & Libraries

React Functional Components with Hooks (useState)
Lucide-react icons for visual indicators
Custom UI components: Card, Badge, Button, Select, Table from ./ui/...
Tailwind CSS for layout and styling
👤 Component: HistoryPage

Props:

user: User – includes role (DRIVER or MANAGER) and optionally assignedCarId
State:

timeFilter: string – selected time range (7days, 30days, 90days, year)
Data:

Static array of Trip objects with:
id, date, startLocation, endLocation
distance (mi), duration (e.g., '45m'), avgSpeed (mph), fuelUsed (gal)
status: 'completed' | 'ongoing' | 'cancelled'
🖥️ Dashboard Layout

1. Header Section

Title: "Trip History" for drivers, "Fleet History" for managers
Subtitle:
Drivers: "View your driving history and performance metrics"
Managers: "Analyze historical trip data across the fleet"
Controls:
Time Filter Dropdown – select time range
Export Button – triggers data export (icon: Download) 2. Summary Cards (3 Cards in Grid)

Each card includes:

Title and Lucide icon
Bold metric value
Contextual note or trend
Cards:

📍 Total Distance
Sum of all trip distances
Trend indicator (e.g., +8% from last period)
⛽ Fuel Consumed
Sum of all fuel used
Efficiency calculation: mi/gal
⏱️ Average Speed
Average across all trips
Note: includes traffic stops 3. Trip History Table

Title: "Trip History" with calendar icon
Description:
Drivers: "Detailed history of all trips for vehicle [assignedCarId]"
Managers: "Detailed history of all trips"
Columns:
Trip ID
Date & Time (formatted)
Route (start → end)
Distance (mi)
Duration
Avg Speed (mph)
Fuel Used (gal)
Status (with color-coded badges) 4. Status Badge Logic

✅ Completed: green badge
🔵 Ongoing: blue badge
❌ Cancelled: red destructive badge
🟡 Unknown: outlined badge

LoginPage:

Objective: Design and implement a responsive Login Page using React, Lucide icons, and custom UI components to authenticate users based on role (DRIVER or ADMIN) and provide access to their respective dashboards in a fleet management system.

🧰 Tech Stack & Libraries

React Functional Components with Hooks (useState)
Lucide-react icons for visual branding and alerts
Custom UI components: Card, Input, Label, Select, Button, Alert from ./ui/...
Tailwind CSS for layout and styling
👤 Component: LoginPage

Props:

onLogin: (user: User) => void – callback to pass authenticated user data to parent component
State:

username: string – input field for username
password: string – input field for password
selectedRole: UserRole – dropdown selection (DRIVER or ADMIN)
error: string – error message for invalid login
isLoading: boolean – loading state during simulated API call
Mock Data:

Array of hardcoded users with:
id, username, password, role, assignedCarId (for drivers only)
🖥️ UI Layout & Behavior

1. Page Container

Full-screen centered layout with gradient background
Responsive padding and spacing 2. Login Card

Header Section:
Icon: Car inside a rounded primary-colored circle
Title: "Fleet Management System"
Subtitle: "Sign in to access your dashboard"
Form Section:
Username Input – labeled, required
Password Input – labeled, required
Role Selector – dropdown with Driver and Administrator options
Error Alert – shown if login fails, with AlertCircle icon and descriptive message
Submit Button – disabled during loading, shows "Signing in..." when active
Demo Credentials Box:
Muted background with sample login info
Text examples for driver and admin accounts 3. Login Logic

On form submission:
Simulates 1-second delay to mimic API call
Validates credentials against mock user array
If match found: calls onLogin with user object
If no match: displays error message "Invalid credentials or role mismatch"

MapView:

Objective: Develop a responsive Map View Dashboard using React, Lucide icons, and custom UI components to visualize real-time vehicle locations, telemetry data, and status indicators. The interface should support role-based filtering for drivers and administrators, allowing users to center the map on specific vehicles and view detailed metrics.

🧰 Tech Stack & Libraries

React Functional Components with Hooks (useState, useEffect)
Lucide-react icons for visual indicators
Custom UI components: Card, Badge, Button from ./ui/...
Tailwind CSS for layout and styling
Simulated map interface (SVG grid background) with placeholder for future integration (e.g., Google Maps or Leaflet.js)
👤 Component: MapView

Props:

user: User – includes role (DRIVER or ADMIN) and assignedCarId for drivers
State:

vehicles: VehicleLocation[] – array of vehicle objects with:
id, driver, lat, lng, address
speed, fuelLevel, engineTemp
status: 'active' | 'idle' | 'maintenance'
selectedVehicle: VehicleLocation | null – currently focused vehicle
centerLat, centerLng – coordinates for map centering
Behavior:

Filters vehicles based on user role:
Drivers see only their assigned vehicle
Admins see all vehicles
Automatically centers map on driver’s vehicle on load
Clicking a vehicle marker or list item updates map center and details panel
🖥️ UI Layout & Features

1. Header Section

Title: "Vehicle Location" (Driver) or "Fleet Map" (Admin)
Subtitle: Role-specific description
Refresh Button: Reloads page to simulate data refresh 2. Map Panel (2/3 Width)

Card Title: "Live Map View" with MapPin icon
Simulated Map Interface:
SVG grid background with gradient overlay
Vehicle markers positioned using percentage-based layout
Each marker:
Colored dot based on status (green, yellow, red)
Label with vehicle ID
Clickable to center map and show details
Center crosshair icon (Crosshair)
Zoom controls (+, - buttons)
Map type toggle badge (Satellite View)
Footer Note: Placeholder for future map integration and interaction tips 3. Sidebar Panel (1/3 Width)

a. Fleet Vehicle List (Admin only)

Card with title "Fleet Vehicles" and Car icon
List of vehicles:
Clickable items with:
Vehicle ID and driver name
Address (truncated)
Status badge (Active, Idle, Maintenance)
Highlighted if selected
b. Selected Vehicle Details

Card with title "CARXXX Details" and Car icon
Info includes:
Driver name and address
Speed (Gauge icon)
Fuel level (Fuel icon) with color-coded percentage
Engine temperature (Thermometer icon) with thresholds:

Button to re-center map on vehicle (Crosshair icon)

Navigation:

Objective: Design and implement a responsive top navigation bar using React, Lucide icons, and custom UI components to support role-based page access, seamless navigation, and user session management for a fleet management system.

🧰 Tech Stack & Libraries

React Functional Components with Props
Lucide-react icons for visual clarity
Custom UI components: Button from ./ui/button
Tailwind CSS for layout, spacing, and responsiveness
👤 Component: Navigation

Props:

user: User – includes username and role (DRIVER or ADMIN)
currentPage: Page – currently active page identifier
onPageChange: (page: Page) => void – callback to switch pages
onLogout: () => void – callback to log out the user
🖥️ UI Layout & Features

1. Top Navigation Bar

Fixed at the top (fixed top-0) with border and background styling
Contains:
Branding Section:
Car icon
App name: "Fleet Manager"
Page Navigation Buttons:
Rendered conditionally based on user.role
Highlighted (variant="default") if active
Uses Lucide icons for each page
Pages:
Driver Role:
Dashboard (LayoutDashboard)
Map View (Map)
Trip History (History)
Alerts (AlertTriangle)
Admin Role:
Fleet Dashboard (LayoutDashboard)
Fleet Map (Map)
Analytics (BarChart3)
Alerts (AlertTriangle)
Settings (Settings)
User Info Section:
Welcome message with username
Role badge (e.g., DRIVER, ADMIN)
Logout button with LogOut icon 2. Mobile Navigation

Visible only on smaller screens (md:hidden)
Horizontal scrollable button group
Compact layout with smaller icons and labels
Same role-based page filtering logic
Ensures usability on mobile devices
🔄 Behavior

Clicking a page button triggers onPageChange(page.id)
Active page is visually highlighted
Logout button triggers onLogout()
Responsive design adapts layout for desktop and mobile

SettingPage:

Objective: Build a multi-tabbed settings dashboard using React, Lucide icons, and custom UI components to manage users, vehicles, notifications, system behavior, and security policies for a fleet management system. The interface should support real-time configuration, form validation, and role-based assignments.

🧰 Tech Stack & Libraries

React Functional Components with Hooks (useState)
Lucide-react icons for visual indicators
Custom UI components: Card, Tabs, Table, Dialog, Input, Switch, Select, Badge, Button, Alert, Label from ./ui/...
Tailwind CSS for layout and styling
🧩 Component: SettingsPage

State Management:

users: UserData[] – user accounts with roles, vehicle assignments, and login history
vehicles: Vehicle[] – fleet vehicles with status, model, year, and driver assignment
notifications – alert preferences (email, SMS, critical-only, maintenance reminders)
systemSettings – refresh interval, data retention, speed limits
alertThresholds – fuel, temperature, and speed thresholds
securitySettings – password policy, 2FA, session timeout, login attempts
Dialog and form states for adding users and vehicles
Form error arrays for validation feedback
🖥️ UI Layout & Features

1. Header

Title: "System Settings"
Subtitle: "Manage users, vehicles, and configure system settings" 2. Tabs Navigation

Tabs: User Management, Fleet Management, Notifications, System, Security
Each tab displays a distinct configuration panel
👥 Tab: User Management

Add User Dialog:
Form with username, password, confirm password, role selector
If role is DRIVER, vehicle assignment dropdown (filtered to active, unassigned vehicles)
Validation includes:
Unique username
Password length and match
Vehicle availability
User Table:
Columns: Username, Role, Assigned Vehicle, Status, Last Login, Actions
Actions: Edit, Toggle Status (Active/Inactive), Delete
Role and status badges for visual clarity
🚚 Tab: Fleet Management

Add Vehicle Dialog:
Form with vehicle ID, model, year
Validation includes:
Unique ID
Alphanumeric format
Year range (2000 to next year)
Fleet Overview Cards:
Total Vehicles, Active, Assigned, Available
Vehicle Table:
Columns: ID, Model, Year, Status, Assigned Driver, Added Date, Actions
Actions: Toggle Status (Active/Maintenance), Delete (only if unassigned)
Status badges for quick visual status
🔔 Tab: Notifications

Alert Thresholds:

Inputs for:
Low Fuel (%)
High Temperature (°F)
Speed Violation (%)
Validated numeric ranges
🛠️ Tab: System

Real-Time Settings:
Auto Refresh toggle
Refresh Interval (10–300 seconds)
Data Management:
Data Retention (30–365 days)
Max Speed Limit (25–100 mph)
Save button for applying changes
🛡️ Tab: Security

Access Control Settings:
Toggles for:
Strong Passwords
Two-Factor Authentication
Session Timeout
Input for Max Login Attempts (1–10)
Audit Log:
Recent events like login, access, failed attempts, and settings updates
🔄 Behavior & Logic

Form Validation:
Real-time error feedback
Prevents submission on invalid input
Role-Based Logic:
Drivers must be assigned to available vehicles
Admins can manage all settings
State Synchronization:
Updates vehicle assignment when user is added
Prevents deletion of assigned vehicles
