import React, { useState, useEffect, useMemo, useRef } from "react";
import { FaMapMarkerAlt, FaGasPump, FaThermometerHalf, FaCar, FaTools, FaClock } from "react-icons/fa";
import { Bell, AlertCircle } from "lucide-react";
import api from "../api/client";

const AdminDashboard = () => {
    const [vehicles, setVehicles] = useState([]);
    const [driversByCarId, setDriversByCarId] = useState({});
    const [searchTerm, setSearchTerm] = useState("");
    const [autoRefresh, setAutoRefresh] = useState(true);
    const [alertCounts, setAlertCounts] = useState({ totalAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0 });
    const [notifOpen, setNotifOpen] = useState(false);
    const [criticalUnack, setCriticalUnack] = useState([]);
    const [carsById, setCarsById] = useState({});

    // 👇 ref for notification panel
    const notifRef = useRef(null);

    // 👇 close on outside click
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (notifRef.current && !notifRef.current.contains(event.target)) {
                setNotifOpen(false);
            }
        };
        if (notifOpen) {
            document.addEventListener("mousedown", handleClickOutside);
        }
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [notifOpen]);

    const fetchData = async () => {
        try {
            const [telemetryRes, driversRes, alertsStatsRes, carsRes] = await Promise.all([
                api.get("/telemetry/latest/all"),
                api.get("/drivers/assigned"),
                api.get("/alerts/stats/count"),
                api.get("/cars"),
            ]);

            const telemetryList = telemetryRes?.data?.data || [];
            const activeCars = carsRes?.data?.data || [];
            const activeCarIdSet = new Set((activeCars || []).map((c) => c.id));
            const activeTelemetry = telemetryList.filter((t) => activeCarIdSet.has(t.carId));

            const drivers = (driversRes?.data?.data || []).reduce((acc, d) => {
                if (d.assignedCarId) acc[d.assignedCarId] = d.name || d.username;
                return acc;
            }, {});
            const counts = alertsStatsRes?.data?.data || { totalAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0 };

            const carStatusById = (activeCars || []).reduce((acc, c) => {
                acc[c.id] = c.status || "";
                return acc;
            }, {});
            const carMap = (activeCars || []).reduce((acc, c) => { acc[c.id] = c; return acc; }, {});

            const rows = activeTelemetry.map(t => {
                let formattedTime = "-";
                if (t.timestamp) {
                    try {
                        formattedTime = new Date(t.timestamp).toLocaleString("en-IN", {
                            day: "2-digit",
                            month: "2-digit",
                            year: "numeric",
                            hour: "2-digit",
                            minute: "2-digit",
                            second: "2-digit",
                            hour12: true,
                        });
                    } catch {
                        formattedTime = t.timestamp;
                    }
                }

                return {
                    id: t.carId,
                    carNumber: carMap[t.carId]?.carNumber || '-',
                    carModel: carMap[t.carId]?.carModel || '-',
                    driver: drivers[t.carId] || "-",
                    location: t.location || "-",
                    speed: t.speed ?? 0,
                    fuel: t.fuelLevel ?? 0,
                    temp: t.temperature ?? 0,
                    status: carStatusById[t.carId] || "-",
                    lastUpdate: formattedTime,
                };
            });

            setVehicles(rows);
            setDriversByCarId(drivers);
            setCarsById(carMap);
            setAlertCounts(counts);
        } catch (e) { }
    };

    useEffect(() => { fetchData(); }, []);

    useEffect(() => {
        const loadCritical = async () => {
            try {
                const res = await api.get('/alerts/critical');
                const list = (res?.data?.data || [])
                    .filter(a => !a.acknowledged)
                    .filter(a => String(a.severity || '').toUpperCase() === 'CRITICAL');
                setCriticalUnack(list);
            } catch (_) { setCriticalUnack([]); }
        };
        loadCritical();
        const id = setInterval(loadCritical, 30000);
        return () => clearInterval(id);
    }, []);

    const acknowledgeAlert = async (id) => {
        try {
            await api.put(`/alerts/${id}/acknowledge`);
            setCriticalUnack((prev) => prev.filter((a) => a.id !== id));
        } catch (_) { }
    };

    const acknowledgeAll = async () => {
        try {
            await Promise.all(criticalUnack.map(a => api.put(`/alerts/${a.id}/acknowledge`)));
            setCriticalUnack([]);
        } catch (_) { }
    };

    useEffect(() => {
        if (!autoRefresh) return;
        const interval = setInterval(fetchData, 30000);
        return () => clearInterval(interval);
    }, [autoRefresh]);

    const filteredVehicles = useMemo(() => {
        const q = (searchTerm || "").toLowerCase().trim();
        if (!q) return vehicles;
        return vehicles.filter((v) => {
            const idStr = String(v.id || "").toLowerCase();
            const driverStr = String(v.driver || "").toLowerCase();
            const locationStr = String(v.location || "").toLowerCase();
            return idStr.includes(q) || driverStr.includes(q) || locationStr.includes(q);
        });
    }, [vehicles, searchTerm]);

    const activeCount = vehicles.filter((v) => String(v.status || "").toLowerCase() === "active").length;
    const idleCount = vehicles.filter((v) => String(v.status || "").toLowerCase() === "idle").length;
    const maintenanceCount = vehicles.filter((v) => String(v.status || "").toLowerCase() === "under maintainance").length;

    const getFuelColor = (fuel) => {
        if (fuel > 50) return "green";
        if (fuel > 25) return "orange";
        return "red";
    };

    const getTempColor = (temp) => {
        if (temp <= 95) return "green";
        if (temp <= 100) return "orange";
        return "red";
    };

    return (
        <div className="pt-16 bg-gray-100 min-h-screen">
            <div className="p-6">
                <h1 className="text-3xl font-bold mb-6">Admin Dashboard</h1>

                {/* Dashboard Cards */}
                <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                    <div className="flex items-center p-5 bg-green-100 rounded-2xl shadow hover:shadow-lg transition">
                        <FaCar className="text-4xl text-green-600 mr-4" />
                        <div>
                            <h2 className="text-lg font-semibold">Active Vehicles</h2>
                            <p className="text-3xl font-bold">{activeCount}</p>
                        </div>
                    </div>
                    <div className="flex items-center p-5 bg-yellow-100 rounded-2xl shadow hover:shadow-lg transition">
                        <FaClock className="text-4xl text-yellow-600 mr-4" />
                        <div>
                            <h2 className="text-lg font-semibold">Idle Vehicles</h2>
                            <p className="text-3xl font-bold">{idleCount}</p>
                        </div>
                    </div>
                    <div className="flex items-center p-5 bg-orange-100 rounded-2xl shadow hover:shadow-lg transition">
                        <FaTools className="text-4xl text-orange-600 mr-4" />
                        <div>
                            <h2 className="text-lg font-semibold">Maintenance</h2>
                            <p className="text-3xl font-bold">{maintenanceCount}</p>
                        </div>
                    </div>
                    <div className="flex items-center p-5 bg-red-100 rounded-2xl shadow hover:shadow-lg transition">
                        <AlertCircle className="text-4xl text-red-600 mr-4" />
                        <div>
                            <h2 className="text-lg font-semibold">Alerts</h2>
                            <p className="text-3xl font-bold text-red-600">{alertCounts.totalAlerts}</p>
                        </div>
                    </div>
                </div>

                {/* Search + Notifications */}
                <div className="flex justify-between items-center mb-6 relative">
                    <input
                        type="text"
                        placeholder="Search by ID, driver or location"
                        className="border px-4 py-2 rounded-lg shadow w-1/2 focus:outline-none focus:ring-2 focus:ring-blue-400"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                    <div className="flex items-center gap-4" ref={notifRef}>
                        <div className="text-sm text-gray-600">Auto refresh every 30s</div>
                        <button
                            className="relative bg-white p-2 rounded-full shadow hover:shadow-md"
                            onClick={() => setNotifOpen(o => !o)}
                        >
                            <Bell size={20} />
                            {criticalUnack.length > 0 && (
                                <span className="absolute -top-1 -right-1 bg-red-600 text-white text-[10px] rounded-full px-1">
                                    {criticalUnack.length}
                                </span>
                            )}
                        </button>

                        {/* Notifications Panel */}
                        {notifOpen && (
                            <div className="absolute right-0 top-12 w-96 bg-white rounded-2xl shadow-xl border z-10">
                                <div className="flex justify-between items-center px-4 py-3 border-b font-semibold bg-gray-50 rounded-t-2xl">
                                    <span>Critical Alerts</span>
                                    {criticalUnack.length > 0 && (
                                        <button
                                            className="text-xs px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600"
                                            onClick={acknowledgeAll}
                                        >
                                            Acknowledge All
                                        </button>
                                    )}
                                </div>
                                <div className="max-h-80 overflow-y-auto p-3">
                                    {criticalUnack.length === 0 && (
                                        <p className="text-sm text-gray-500 text-center">No critical alerts 🎉</p>
                                    )}
                                    {criticalUnack.map((a) => (
                                        <div key={a.id} className="p-3 border rounded-xl mb-2 bg-white shadow-sm hover:shadow-md transition">
                                            <div className="flex items-center justify-between">
                                                <div>
                                                    <div className="text-sm font-medium">{a.type}</div>
                                                    <div className="text-xs text-gray-500">{a.timestamp}</div>
                                                </div>
                                                <button
                                                    className="text-xs px-2 py-1 border rounded hover:bg-gray-100"
                                                    onClick={() => acknowledgeAlert(a.id)}
                                                >Acknowledge</button>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                {/* Modern Styled Table */}
                <div className="overflow-x-auto rounded-2xl shadow-lg border border-gray-200">
                    <table className="w-full border-collapse text-sm text-gray-700">
                        <thead className="sticky top-0 bg-gray-100 shadow-sm text-gray-600 text-[13px] uppercase tracking-wide">
                            <tr>
                                <th className="px-4 py-3 text-left">Car ID</th>
                                <th className="px-4 py-3 text-left">Car Number</th>
                                <th className="px-4 py-3 text-left">Car Model</th>
                                <th className="px-4 py-3 text-left">Driver</th>
                                <th className="px-4fuel py-3 text-left">Location</th>
                                <th className="px-4 py-3 text-center">Speed</th>
                                <th className="px-4 py-3 text-center">Fuel</th>
                                <th className="px-4 py-3 text-center">Engine Temp</th>
                                <th className="px-4 py-3 text-center">Status</th>
                                <th className="px-4 py-3 text-left">Last Update</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredVehicles.map((v, i) => (
                                <tr
                                    key={i}
                                    className={`transition-colors ${i % 2 === 0 ? "bg-white" : "bg-gray-50"
                                        } hover:bg-blue-50`}
                                >
                                    <td className="px-4 py-3">{v.id}</td>
                                    <td className="px-4 py-3 font-medium text-gray-800">{v.carNumber}</td>
                                    <td className="px-4 py-3">{v.carModel}</td>
                                    <td className="px-4 py-3">{v.driver}</td>
                                    <td className="px-4 py-3 flex items-center gap-2">
                                        <FaMapMarkerAlt className="text-blue-500" size={14} />
                                        <span>{v.location}</span>
                                    </td>
                                    <td className="px-4 py-3 text-center">{v.speed}</td>
                                    <td className="px-4 py-3 text-center">
                                        <div className="flex items-center justify-center gap-1">
                                            <FaGasPump size={14} className="text-gray-500" />
                                            <span style={{ color: getFuelColor(v.fuel) }}>{v.fuel}%</span>
                                        </div>
                                    </td>

                                    <td className="px-4 py-3 text-center">
                                        <div className="flex items-center justify-center gap-1">
                                            <FaThermometerHalf size={14} className="text-gray-500" />
                                            <span style={{ color: getTempColor(v.temp) }}>{v.temp}°C</span>
                                        </div>
                                    </td>

                                    <td className="px-4 py-3 text-center">
                                        {(() => {
                                            const s = String(v.status || "").toLowerCase();
                                            return (
                                                <span
                                                    className={`px-2 py-1 text-xs rounded-full font-medium ${s === "active"
                                                        ? "bg-green-100 text-green-700"
                                                        : s === "idle"
                                                            ? "bg-yellow-100 text-yellow-700"
                                                            : s === "maintenance"
                                                                ? "bg-orange-100 text-orange-700"
                                                                : "bg-red-100 text-red-700"
                                                        }`}
                                                >
                                                    {v.status}
                                                </span>
                                            );
                                        })()}
                                    </td>
                                    <td className="px-4 py-3 text-gray-500 text-sm">{v.lastUpdate}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

            </div>
        </div>
    );
};

export default AdminDashboard;
