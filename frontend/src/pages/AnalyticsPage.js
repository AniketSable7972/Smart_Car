// AnalyticsPage.js
import React, { useEffect, useMemo, useState } from "react";
import {
    LineChart, Line, BarChart, Bar, PieChart, Pie, Cell, Tooltip,
    XAxis, YAxis, CartesianGrid, Legend, ResponsiveContainer
} from "recharts";
import { AlertTriangle, Gauge, Thermometer, Activity } from "lucide-react";
import api from "../api/client";

const COLORS = ["#6366F1", "#F59E0B", "#10B981", "#EF4444", "#3B82F6", "#FF8042", "#00C49F"];

const formatLocalDateTime = (date) => {
    const pad = (n) => String(n).padStart(2, "0");
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
};

const formatDisplayTime = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInHours = (now - date) / (1000 * 60 * 60);

    if (diffInHours < 24) {
        // Within 24 hours, show time
        return date.toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
        });
    } else if (diffInHours < 168) { // 7 days
        // Within a week, show day and time
        return date.toLocaleDateString('en-US', {
            weekday: 'short',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
        });
    } else {
        // Older than a week, show date
        return date.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });
    }
};

const formatChartTime = (dateString, timeRange) => {
    const date = new Date(dateString);

    if (timeRange === "24h") {
        // For 24h view, show hour:minute
        return date.toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
        });
    } else if (timeRange === "7d") {
        // For 7d view, show day and hour
        return date.toLocaleDateString('en-US', {
            weekday: 'short',
            hour: '2-digit',
            hour12: true
        });
    } else {
        // For 30d view, show month and day
        return date.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric'
        });
    }
};

const getRange = (rangeKey) => {
    const end = new Date();
    const start = new Date(end.getTime());
    if (rangeKey === "24h") start.setHours(end.getHours() - 24);
    else if (rangeKey === "7d") start.setDate(end.getDate() - 7);
    else if (rangeKey === "30d") start.setDate(end.getDate() - 30);
    else start.setDate(end.getDate() - 7);
    return { start, end };
};

const AnalyticsPage = () => {
    const [timeRange, setTimeRange] = useState("7d");
    const [cars, setCars] = useState([]);
    const [selectedCarId, setSelectedCarId] = useState("ALL");

    const [alertsCounts, setAlertsCounts] = useState({ totalAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0 });
    const [alertsAll, setAlertsAll] = useState([]);
    const [latestAll, setLatestAll] = useState([]);
    const [carTelemetry, setCarTelemetry] = useState([]);
    const [carStats, setCarStats] = useState(null);

    // Load base data
    useEffect(() => {
        const loadBase = async () => {
            try {
                const [carsRes, countRes, alertsRes, teleRes] = await Promise.all([
                    api.get("/cars"),
                    api.get("/alerts/stats/count"),
                    api.get("/alerts"),
                    api.get("/telemetry/latest/all"),
                ]);
                const carList = carsRes?.data?.data || [];
                setCars(carList);
                setAlertsCounts(countRes?.data?.data || { totalAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0 });
                setAlertsAll((alertsRes?.data?.data || []).sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp)));
                const activeIds = new Set(carList.map((c) => c.id));
                setLatestAll((teleRes?.data?.data || []).filter((t) => activeIds.has(t.carId)));
            } catch (_) { }
        };
        loadBase();
    }, []);

    // Load car-specific alert counts when car selection changes
    useEffect(() => {
        const loadCarAlerts = async () => {
            if (selectedCarId === "ALL") {
                // Reset to fleet-wide counts
                try {
                    const countRes = await api.get("/alerts/stats/count");
                    setAlertsCounts(countRes?.data?.data || { totalAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0 });
                } catch (_) { }
                return;
            }

            try {
                const alertsRes = await api.get(`/alerts/car/${selectedCarId}`);
                const carAlerts = alertsRes?.data?.data || [];

                // Calculate counts for the selected car
                const totalAlerts = carAlerts.length;
                const unacknowledgedAlerts = carAlerts.filter(alert => !alert.acknowledged).length;
                const criticalAlerts = carAlerts.filter(alert => alert.severity === 'CRITICAL').length;

                setAlertsCounts({
                    totalAlerts,
                    unacknowledgedAlerts,
                    criticalAlerts
                });
            } catch (_) {
                // If car-specific alerts fail, reset counts
                setAlertsCounts({ totalAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0 });
            }
        };

        loadCarAlerts();
    }, [selectedCarId]);

    // Load telemetry/stats for selected car
    useEffect(() => {
        const run = async () => {
            if (selectedCarId === "ALL") { setCarTelemetry([]); setCarStats(null); return; }
            const { start, end } = getRange(timeRange);
            try {
                const [rangeRes, statsRes] = await Promise.all([
                    api.get(`/telemetry/car/${selectedCarId}/range`, { params: { startTime: formatLocalDateTime(start), endTime: formatLocalDateTime(end) } }),
                    api.get(`/telemetry/stats/car/${selectedCarId}`, { params: { startTime: formatLocalDateTime(start), endTime: formatLocalDateTime(end) } }),
                ]);
                setCarTelemetry(rangeRes?.data?.data || []);
                setCarStats(statsRes?.data?.data || null);
            } catch (_) { setCarTelemetry([]); setCarStats(null); }
        };
        run();
    }, [selectedCarId, timeRange]);

    // Derived: fleet status
    const statusDistribution = useMemo(() => {
        const counts = cars.reduce((acc, c) => { const key = String(c.status || "-").toUpperCase(); acc[key] = (acc[key] || 0) + 1; return acc; }, {});
        return Object.entries(counts).map(([name, value]) => ({ name, value }));
    }, [cars]);

    // Alerts by severity (pie)
    const severityPie = useMemo(() => {
        const counts = { LOW: 0, MEDIUM: 0, HIGH: 0, CRITICAL: 0 };
        for (const a of alertsAll) {
            const s = String(a.severity || "").toUpperCase();
            if (counts[s] !== undefined) counts[s]++;
        }
        return Object.entries(counts).map(([name, value]) => ({ name, value }));
    }, [alertsAll]);

    // Alerts over time
    const alertsOverTime = useMemo(() => {
        const { start, end } = getRange(timeRange);
        const inRange = (alertsAll || []).filter((a) => {
            const ts = new Date(a.timestamp);
            return ts >= start && ts <= end && (selectedCarId === "ALL" || String(a.carId) === String(selectedCarId));
        });
        const byKey = {};
        for (const a of inRange) {
            const ts = new Date(a.timestamp);
            let key;
            if (timeRange === "24h") {
                // Group by hour for 24h view
                key = ts.toLocaleDateString('en-US', {
                    month: 'short',
                    day: 'numeric'
                }) + ' ' + ts.toLocaleTimeString('en-US', {
                    hour: '2-digit',
                    hour12: true
                });
            } else if (timeRange === "7d") {
                // Group by day for 7d view
                key = ts.toLocaleDateString('en-US', {
                    weekday: 'short',
                    month: 'short',
                    day: 'numeric'
                });
            } else {
                // Group by day for 30d view
                key = ts.toLocaleDateString('en-US', {
                    month: 'short',
                    day: 'numeric'
                });
            }
            byKey[key] = (byKey[key] || 0) + 1;
        }
        return Object.entries(byKey).sort((a, b) => {
            // Sort by actual date, not formatted string
            const aDate = new Date(inRange.find(alert => {
                const ts = new Date(alert.timestamp);
                let key;
                if (timeRange === "24h") {
                    key = ts.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) + ' ' + ts.toLocaleTimeString('en-US', { hour: '2-digit', hour12: true });
                } else if (timeRange === "7d") {
                    key = ts.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });
                } else {
                    key = ts.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
                }
                return key === a[0];
            })?.timestamp || 0);
            const bDate = new Date(inRange.find(alert => {
                const ts = new Date(alert.timestamp);
                let key;
                if (timeRange === "24h") {
                    key = ts.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) + ' ' + ts.toLocaleTimeString('en-US', { hour: '2-digit', hour12: true });
                } else if (timeRange === "7d") {
                    key = ts.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });
                } else {
                    key = ts.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
                }
                return key === b[0];
            })?.timestamp || 0);
            return aDate - bDate;
        }).map(([name, value]) => ({ name, value }));
    }, [alertsAll, timeRange, selectedCarId]);

    // Alerts by car
    const alertsByCarNumber = useMemo(() => {
        const idToNumber = (cars || []).reduce((acc, c) => { acc[c.id] = c.carNumber || String(c.id); return acc; }, {});
        const counts = {};
        for (const a of alertsAll) {
            const key = idToNumber[a.carId] || String(a.carId);
            counts[key] = (counts[key] || 0) + 1;
        }
        return Object.entries(counts).map(([name, value]) => ({ name, value }));
    }, [alertsAll, cars]);

    // Telemetry series for charts
    const carSeries = useMemo(() => (carTelemetry || []).map((t) => ({
        time: formatDisplayTime(t.timestamp),
        speed: t.speed,
        fuel: t.fuelLevel,
        temp: t.temperature,
        rawTime: t.timestamp // Keep raw timestamp for sorting
    })), [carTelemetry]);

    const idToNumber = useMemo(() => cars.reduce((acc, c) => { acc[c.id] = c.carNumber; return acc; }, {}), [cars]);

    return (
        <div className="pt-16">
            <div className="p-6 bg-gray-50 min-h-screen">
                <div className="max-w-7xl mx-auto space-y-8">
                    {/* Header */}
                    <header>
                        <h1 className="text-4xl font-bold text-gray-900">📊 Analytics Dashboard</h1>
                        <p className="text-gray-600">Operational insights from telemetry & alerts</p>
                    </header>

                    {/* Filters */}
                    <div className="bg-white rounded-2xl shadow p-4 flex flex-wrap gap-4">
                        <select className="px-4 py-2 rounded-full border shadow-sm text-gray-700 focus:ring focus:ring-indigo-300"
                            value={selectedCarId} onChange={(e) => setSelectedCarId(e.target.value)}>
                            <option value="ALL">All cars</option>
                            {cars.map((c) => (
                                <option key={c.id} value={c.id}>{c.carNumber} ({String(c.status || "").toUpperCase()})</option>
                            ))}
                        </select>
                        <select className="px-4 py-2 rounded-full border shadow-sm text-gray-700 focus:ring focus:ring-indigo-300"
                            value={timeRange} onChange={(e) => setTimeRange(e.target.value)}>
                            <option value="24h">Last 24 hours</option>
                            <option value="7d">Last 7 days</option>
                            <option value="30d">Last 30 days</option>
                        </select>
                    </div>

                    {/* KPI Cards */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div className="bg-gradient-to-br from-red-50 to-red-100 p-5 rounded-2xl shadow">
                            <div className="flex items-center gap-3">
                                <AlertTriangle className="text-red-500 w-6 h-6" />
                                <div>
                                    <p className="text-sm text-gray-500">Total Alerts</p>
                                    <p className="text-3xl font-bold">{alertsCounts.totalAlerts}</p>
                                </div>
                            </div>
                        </div>
                        <div className="bg-gradient-to-br from-orange-50 to-yellow-100 p-5 rounded-2xl shadow">
                            <div className="flex items-center gap-3">
                                <Gauge className="text-orange-500 w-6 h-6" />
                                <div>
                                    <p className="text-sm text-gray-500">Unacknowledged</p>
                                    <p className="text-3xl font-bold">{alertsCounts.unacknowledgedAlerts}</p>
                                </div>
                            </div>
                        </div>
                        <div className="bg-gradient-to-br from-pink-50 to-red-100 p-5 rounded-2xl shadow">
                            <div className="flex items-center gap-3">
                                <Thermometer className="text-pink-500 w-6 h-6" />
                                <div>
                                    <p className="text-sm text-gray-500">Critical Alerts</p>
                                    <p className="text-3xl font-bold">{alertsCounts.criticalAlerts}</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Charts */}
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                        {/* Severity Distribution */}
                        <div className="bg-white rounded-2xl shadow p-5">
                            <h3 className="text-lg font-semibold border-l-4 border-red-500 pl-2 mb-3">Alert Severity Distribution</h3>
                            <ResponsiveContainer width="100%" height={260}>
                                <PieChart>
                                    <Pie data={severityPie} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={90} label>
                                        {severityPie.map((entry, index) => (
                                            <Cell key={`sev-${index}`} fill={COLORS[index % COLORS.length]} />
                                        ))}
                                    </Pie>
                                    <Tooltip />
                                    <Legend />
                                </PieChart>
                            </ResponsiveContainer>
                        </div>

                        {/* Fleet Status */}
                        <div className="bg-white rounded-2xl shadow p-5">
                            <h3 className="text-lg font-semibold border-l-4 border-indigo-500 pl-2 mb-3">Fleet Status Distribution</h3>
                            <ResponsiveContainer width="100%" height={260}>
                                <PieChart>
                                    <Pie data={statusDistribution} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={90} label>
                                        {statusDistribution.map((entry, index) => (
                                            <Cell key={`stat-${index}`} fill={COLORS[(index + 2) % COLORS.length]} />
                                        ))}
                                    </Pie>
                                    <Tooltip />
                                    <Legend />
                                </PieChart>
                            </ResponsiveContainer>
                        </div>

                        {/* Alerts Over Time */}
                        <div className="bg-white rounded-2xl shadow p-5 lg:col-span-2">
                            <h3 className="text-lg font-semibold border-l-4 border-orange-500 pl-2 mb-3">
                                Alerts Over Time ({timeRange === "24h" ? "by hour" : "by day"})
                            </h3>
                            <ResponsiveContainer width="100%" height={280}>
                                <BarChart data={alertsOverTime}>
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis dataKey="name" />
                                    <YAxis allowDecimals={false} />
                                    <Tooltip />
                                    <Bar dataKey="value" fill="#FF6B6B" name="Alerts" />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>

                        {/* Top Alerting Cars */}
                        <div className="bg-white rounded-2xl shadow p-5 lg:col-span-2">
                            <h3 className="text-lg font-semibold border-l-4 border-indigo-500 pl-2 mb-3">Top Alerting Cars</h3>
                            <ResponsiveContainer width="100%" height={260}>
                                <BarChart data={alertsByCarNumber}>
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis dataKey="name" />
                                    <YAxis allowDecimals={false} />
                                    <Tooltip />
                                    <Bar dataKey="value" fill="#6366F1" name="Alerts" />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </div>

                    {/* Car Details */}
                    {selectedCarId !== "ALL" && (
                        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                            <div className="bg-white rounded-2xl shadow p-5">
                                <h3 className="text-lg font-semibold border-l-4 border-indigo-500 pl-2 mb-3">
                                    {idToNumber[selectedCarId]} Telemetry
                                </h3>
                                <ResponsiveContainer width="100%" height={280}>
                                    <LineChart data={[...carSeries].sort((a, b) => new Date(a.rawTime) - new Date(b.rawTime))}>
                                        <CartesianGrid strokeDasharray="3 3" />
                                        <XAxis dataKey="time" />
                                        <YAxis yAxisId="left" />
                                        <Tooltip
                                            labelFormatter={(label) => `Time: ${label}`}
                                            formatter={(value, name) => [
                                                name === 'speed' ? `${value} km/h` :
                                                    name === 'fuel' ? `${value}%` :
                                                        `${value}`,
                                                name.charAt(0).toUpperCase() + name.slice(1)
                                            ]}
                                        />
                                        <Legend />
                                        <Line yAxisId="left" type="monotone" dataKey="speed" stroke="#3B82F6" name="Speed (km/h)" dot={false} />
                                        <Line yAxisId="left" type="monotone" dataKey="fuel" stroke="#10B981" name="Fuel (%)" dot={false} />
                                        <Line yAxisId="left" type="monotone" dataKey="temp" stroke="#F59E0B" name="Temp (°C)" dot={false} />
                                    </LineChart>
                                </ResponsiveContainer>
                            </div>

                            <div className="bg-white rounded-2xl shadow p-5">
                                <h3 className="text-lg font-semibold border-l-4 border-indigo-500 pl-2 mb-3">
                                    {idToNumber[selectedCarId]} Summary
                                </h3>
                                {carStats ? (
                                    <div className="grid grid-cols-2 gap-3 text-sm">
                                        <div className="p-3 bg-gray-50 rounded flex flex-col items-start">
                                            <Activity className="w-4 h-4 text-indigo-500" />
                                            <div className="text-gray-600">Avg Speed</div>
                                            <div className="text-xl font-semibold">{carStats.averageSpeed} km/h</div>
                                        </div>
                                        <div className="p-3 bg-gray-50 rounded flex flex-col items-start">
                                            <Gauge className="w-4 h-4 text-green-500" />
                                            <div className="text-gray-600">Avg Fuel</div>
                                            <div className="text-xl font-semibold">{carStats.averageFuel}%</div>
                                        </div>
                                        <div className="p-3 bg-gray-50 rounded flex flex-col items-start">
                                            <Thermometer className="w-4 h-4 text-red-500" />
                                            <div className="text-gray-600">Avg Temp</div>
                                            <div className="text-xl font-semibold">{carStats.averageTemperature}°C</div>
                                        </div>
                                        <div className="p-3 bg-gray-50 rounded">
                                            <div className="text-gray-600">Records</div>
                                            <div className="text-xl font-semibold">{carStats.totalRecords}</div>
                                        </div>
                                        <div className="p-3 bg-gray-50 rounded">
                                            <div className="text-gray-600">Speed Range</div>
                                            <div className="text-xl font-semibold">{carStats.minSpeed} - {carStats.maxSpeed}</div>
                                        </div>
                                        <div className="p-3 bg-gray-50 rounded">
                                            <div className="text-gray-600">Fuel Range</div>
                                            <div className="text-xl font-semibold">{carStats.minFuel}% - {carStats.maxFuel}%</div>
                                        </div>
                                        <div className="p-3 bg-gray-50 rounded">
                                            <div className="text-gray-600">Temp Range</div>
                                            <div className="text-xl font-semibold">{carStats.minTemperature}° - {carStats.maxTemperature}°</div>
                                        </div>
                                    </div>
                                ) : (
                                    <div className="text-gray-500 text-sm">No data available for selected range.</div>
                                )}
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default AnalyticsPage;
