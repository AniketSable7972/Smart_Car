import React from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import LoginPage from '../pages/LoginPage';
import AdminDashboard from '../pages/AdminDashboard';
import DriverDashboard from '../pages/DriverDashboard';
import AlertPage from '../pages/AlertPage';
import AnalyticsPage from '../pages/AnalyticsPage';
import HistoryPage from '../pages/HistoryPage';
import MapView from '../pages/MapView';
import SettingsPage from '../pages/SettingsPage';
import Navigation from '../components/Navigation';

const AppRoutes = () => {
    const isAuthenticated = localStorage.getItem('token'); // Simple check

    return (
        <Router>
            {isAuthenticated && <Navigation />}
            <Routes>
                <Route path="/" element={<LoginPage />} />
                <Route path="/admin" element={isAuthenticated ? <AdminDashboard /> : <Navigate to="/" />} />
                <Route path="/driver" element={isAuthenticated ? <DriverDashboard /> : <Navigate to="/" />} />
                <Route path="/alerts" element={<AlertPage />} />
                <Route path="/analytics" element={<AnalyticsPage />} />
                <Route path="/history" element={<HistoryPage />} />
                <Route path="/map" element={<MapView />} />
                <Route path="/settings" element={<SettingsPage />} />
            </Routes>
        </Router>
    );
};

export default AppRoutes;
