// App.js
import React, { useEffect, useState } from "react";
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import AdminDashboard from "./pages/AdminDashboard";
import DriverDashboard from "./pages/DriverDashboard";
import AlertsPage from "./pages/AlertsPage";
import AnalyticsPage from "./pages/AnalyticsPage";
import HistoryPage from "./pages/HistoryPage";
import SettingsPage from "./pages/SettingsPage";
import Navigation from "./components/Navigation";
import RegisterPage from "./pages/RegisterPage";
import CarAlertsPage from "./pages/CarAlertsPage";
import TripsPage from "./pages/TripsPage";
import TripManagementPage from "./pages/TripManagementPage";
import DriverTripDetails from "./pages/DriverTripDetails";

function AppWrapper() {
    const [user, setUser] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem('token');
        const role = localStorage.getItem('role');
        const username = localStorage.getItem('username');
        const id = localStorage.getItem('userId');
        if (token && role && id) {
            setUser({ id: Number(id), username, role });
        }
    }, []);

    const handleLogin = (userData) => {
        setUser(userData);
        navigate("/dashboard");
    };

    const handleLogout = () => {
        setUser(null);
        localStorage.clear();
        navigate("/");
    };

    const handlePageChange = (pageId) => {
        switch (pageId) {
            case "admin-dashboard":
            case "driver-dashboard":
                navigate("/dashboard");
                break;
            case "fleet-map":
            case "map-view":
                navigate("/dashboard");
                break;
            case "trips":
                navigate("/trips");
                break;
            case "trip-management":
                navigate("/trip-management");
                break;
            case "analytics":
                navigate("/analytics");
                break;
            case "alerts":
                navigate("/alerts");
                break;
            case "history":
                navigate("/history");
                break;
            case "settings":
                navigate("/settings");
                break;
            default:
                navigate("/dashboard");
        }
    };

    return (
        <>
            {!user ? (
                <Routes>
                    <Route path="/" element={<LoginPage onLogin={handleLogin} />} />
                    <Route path="/register" element={<RegisterPage />} />
                    <Route path="*" element={<Navigate to="/" />} />
                </Routes>
            ) : (
                <div className="main-layout">
                    <Navigation
                        user={user}
                        onLogout={handleLogout}
                        onPageChange={handlePageChange}
                        currentPage=""
                    />
                    <div className="page-content">
                        <Routes>
                            {user.role === "ADMIN" ? (
                                <>
                                    <Route path="/dashboard" element={<AdminDashboard />} />
                                    {/* Map removed */}
                                    <Route path="/trip-management" element={<TripManagementPage />} />
                                    <Route path="/analytics" element={<AnalyticsPage />} />
                                    <Route path="/alerts" element={<AlertsPage user={user} />} />
                                    <Route path="/alerts/car/:carId" element={<CarAlertsPage />} />
                                    <Route path="/history" element={<HistoryPage user={user} />} />
                                    <Route path="/settings" element={<SettingsPage />} />
                                    <Route path="/trips/:tripId" element={<DriverTripDetails />} />
                                    <Route path="*" element={<Navigate to="/dashboard" />} />
                                </>
                            ) : (
                                <>
                                    <Route path="/dashboard" element={<DriverDashboard user={user} />} />
                                    <Route path="/trips" element={<TripsPage user={user} />} />
                                    <Route path="/trips/:tripId" element={<DriverTripDetails />} />
                                    <Route path="*" element={<Navigate to="/dashboard" />} />
                                </>
                            )}
                        </Routes>
                    </div>
                </div>
            )}
        </>
    );
}

export default function App() {
    return (
        <Router>
            <AppWrapper />
        </Router>
    );
}
