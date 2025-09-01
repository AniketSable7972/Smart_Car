// ALertPage.js
import React, { useState, useEffect, useMemo } from "react";
import { AlertTriangle, Fuel, Thermometer, Wrench, Zap, Clock, Car as CarIcon } from "lucide-react";
import api from "../api/client";

const formatLocalDateTime = (date) => {
  const pad = (n) => String(n).padStart(2, "0");
  const y = date.getFullYear();
  const m = pad(date.getMonth() + 1);
  const d = pad(date.getDate());
  const hh = pad(date.getHours());
  const mm = pad(date.getMinutes());
  const ss = pad(date.getSeconds());
  return `${y}-${m}-${d}T${hh}:${mm}:${ss}`;
};

const THRESHOLDS = { speed: 100, fuel: 20, temperature: 100 };
const compactCause = (type, valueStr) => {
  const t = (type || "").toLowerCase();
  const num = parseFloat((valueStr || "").replace(/[^0-9.\-]/g, ""));
  if (t.includes("speed")) return `Speed ${isNaN(num) ? valueStr : num} > ${THRESHOLDS.speed} (overspeed)`;
  if (t.includes("fuel")) return `Fuel ${isNaN(num) ? valueStr : num}% < ${THRESHOLDS.fuel}% (low fuel)`;
  if (t.includes("temp")) return `Temp ${isNaN(num) ? valueStr : num}°C > ${THRESHOLDS.temperature}°C (overheat)`;
  return "Threshold exceeded";
};

const AlertsPage = ({ user }) => {
  const [alerts, setAlerts] = useState([]);
  const [carId, setCarId] = useState(null);
  const [telemetryWindow, setTelemetryWindow] = useState([]);
  const [page, setPage] = useState(1);
  const PAGE_SIZE = 8;

  // Admin-specific state
  const [alertStats, setAlertStats] = useState({ totalAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0 });
  const [severityStats, setSeverityStats] = useState({ lowAlerts: 0, mediumAlerts: 0, highAlerts: 0, criticalAlerts: 0 });
  const [recentDetailed, setRecentDetailed] = useState([]);
  // Per-severity pagination
  const [sevPage, setSevPage] = useState({ LOW: 1, MEDIUM: 1, HIGH: 1, CRITICAL: 1 });
  const sevPageSize = 5;

  const loadDriverCar = async () => {
    if (user.role !== 'DRIVER') return;
    try {
      const dres = await api.get(`/drivers/user/${user.id}`);
      const d = dres?.data?.data;
      setCarId(d?.assignedCarId || null);
    } catch (_) { setCarId(null); }
  };

  const loadAlerts = async () => {
    try {
      if (user.role === 'DRIVER') {
        if (!carId) return;
        const res = await api.get(`/alerts/car/${carId}`);
        const arr = (res?.data?.data || []).sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
        setAlerts(arr);
      } else {
        const [alertsRes, carsRes] = await Promise.all([
          api.get("/alerts"),
          api.get("/cars"),
        ]);
        const arrRaw = (alertsRes?.data?.data || []).sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
        const activeCars = carsRes?.data?.data || [];
        const activeIds = new Set(activeCars.map((c) => c.id));
        const arr = arrRaw.filter((a) => activeIds.has(a.carId ?? a.car?.id));
        setAlerts(arr);
        // group by car for car-wise section
        const grouped = arr.reduce((acc, a) => {
          const cid = a.carId ?? a.car?.id;
          if (!cid) return acc;
          if (!acc[cid]) acc[cid] = [];
          acc[cid].push(a);
          return acc;
        }, {});
        // setAlertsByCar(grouped); // This line is removed
      }
      setPage(1);
    } catch (_) { }
  };

  const loadTelemetryWindow = async () => {
    if (user.role !== 'DRIVER' || !carId) { setTelemetryWindow([]); return; }
    try {
      const end = new Date();
      const start = new Date(end.getTime() - 24 * 60 * 60 * 1000);
      const tRes = await api.get(`/telemetry/car/${carId}/range`, { params: { startTime: formatLocalDateTime(start), endTime: formatLocalDateTime(end) } });
      setTelemetryWindow(tRes?.data?.data || []);
    } catch (_) { setTelemetryWindow([]); }
  };

  // Admin: fetch stats
  const loadAdminStats = async () => {
    if (user.role === 'DRIVER') return;
    try {
      const [countRes, sevRes] = await Promise.all([
        api.get('/alerts/stats/count'),
        api.get('/alerts/stats/severity'),
      ]);
      setAlertStats(countRes?.data?.data || { totalAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0 });
      setSeverityStats(sevRes?.data?.data || { lowAlerts: 0, mediumAlerts: 0, highAlerts: 0, criticalAlerts: 0 });
    } catch (_) { }
  };

  // Removed car-wise drivers load

  useEffect(() => { loadDriverCar(); }, [user.id, user.role]);
  useEffect(() => { loadAlerts(); }, [carId, user.role]);
  useEffect(() => { loadTelemetryWindow(); }, [carId]);
  useEffect(() => { loadAdminStats(); }, [user.role]);
  // Removed car-wise drivers load

  const getAlertIcon = (type) => {
    switch ((type || '').toLowerCase()) {
      case "low_fuel":
      case "fuel":
        return <Fuel className="text-blue-500" size={18} />;
      case "high_temperature":
      case "temperature":
        return <Thermometer className="text-red-500" size={18} />;
      case "maintenance":
        return <Wrench className="text-yellow-500" size={18} />;
      case "high_speed":
      case "speed":
        return <Zap className="text-green-500" size={18} />;
      default:
        return <AlertTriangle className="text-gray-500" size={18} />;
    }
  };

  const severityColor = {
    LOW: "bg-green-200 text-green-800",
    MEDIUM: "bg-yellow-200 text-yellow-800",
    HIGH: "bg-orange-200 text-orange-800",
    CRITICAL: "bg-red-200 text-red-800",
  };

  const countsByType = useMemo(() => {
    const c = { speed: 0, fuel: 0, temperature: 0, maintenance: 0, other: 0 };
    for (const a of alerts) {
      const t = (a.type || '').toLowerCase();
      if (t.includes('speed')) c.speed++;
      else if (t.includes('fuel')) c.fuel++;
      else if (t.includes('temp')) c.temperature++;
      else if (t.includes('maint')) c.maintenance++;
      else c.other++;
    }
    return c;
  }, [alerts]);

  const alertsBySeverity = useMemo(() => {
    const groups = { LOW: [], MEDIUM: [], HIGH: [], CRITICAL: [] };
    for (const a of alerts) {
      const sev = String(a.severity || '').toUpperCase();
      if (groups[sev]) groups[sev].push(a);
    }
    return groups;
  }, [alerts]);

  const deriveValueForAlert = (a) => {
    if (!telemetryWindow.length) return null;
    const t = new Date(a.timestamp).getTime();
    let best = null, diff = Number.MAX_SAFE_INTEGER;
    for (const rec of telemetryWindow) {
      const d = Math.abs(new Date(rec.timestamp).getTime() - t);
      if (d < diff) { best = rec; diff = d; }
    }
    if (!best) return null;
    const type = (a.type || '').toLowerCase();
    if (type.includes('fuel')) return `${best.fuelLevel}%`;
    if (type.includes('temp')) return `${best.temperature}°C`;
    if (type.includes('speed')) return `${best.speed} km/h`;
    return null;
  };

  // Admin: for recent 5 alerts, fetch nearby telemetry to compute cause strings
  useEffect(() => {
    const run = async () => {
      if (user.role === 'DRIVER') { setRecentDetailed([]); return; }
      const top5 = (alerts || []).slice(0, 5);
      const results = [];
      for (const a of top5) {
        try {
          const ts = new Date(a.timestamp);
          const start = new Date(ts.getTime() - 10 * 60 * 1000);
          const end = new Date(ts.getTime() + 10 * 60 * 1000);
          const tRes = await api.get(`/telemetry/car/${a.carId}/range`, { params: { startTime: formatLocalDateTime(start), endTime: formatLocalDateTime(end) } });
          const tList = tRes?.data?.data || [];
          let best = null, bestDiff = Number.MAX_SAFE_INTEGER;
          for (const rec of tList) {
            const diff = Math.abs(new Date(rec.timestamp).getTime() - ts.getTime());
            if (diff < bestDiff) { best = rec; bestDiff = diff; }
          }
          let valueStr = null;
          const type = (a.type || '').toLowerCase();
          if (best) {
            if (type.includes('fuel')) valueStr = `${best.fuelLevel}%`;
            else if (type.includes('temp')) valueStr = `${best.temperature}°C`;
            else if (type.includes('speed')) valueStr = `${best.speed} km/h`;
          }
          results.push({ ...a, derivedValue: valueStr, cause: compactCause(a.type, valueStr) });
        } catch (_) {
          results.push({ ...a, derivedValue: null, cause: compactCause(a.type, null) });
        }
      }
      setRecentDetailed(results);
    };
    run();
  }, [alerts, user.role]);

  const pagedAlerts = useMemo(() => {
    const start = (page - 1) * PAGE_SIZE;
    return alerts.slice(start, start + PAGE_SIZE);
  }, [alerts, page]);

  const totalPages = Math.max(1, Math.ceil((alerts || []).length / PAGE_SIZE));

  // Per-severity paged mapping
  const pagedBySeverity = useMemo(() => {
    const out = {};
    for (const sev of ["LOW", "MEDIUM", "HIGH", "CRITICAL"]) {
      const list = alertsBySeverity[sev] || [];
      const start = ((sevPage[sev] || 1) - 1) * sevPageSize;
      out[sev] = list.slice(start, start + sevPageSize);
    }
    return out;
  }, [alertsBySeverity, sevPage]);

  if (user.role === 'DRIVER' && !carId) {
    return (
      <div className="pt-16">
        <div className="p-6 bg-gray-100 min-h-screen flex items-center justify-center">
          <div className="bg-white p-6 rounded shadow text-center max-w-md">
            <h2 className="text-lg font-semibold mb-2">No vehicle assigned</h2>
            <p className="text-gray-600">You will see your alerts once a vehicle is assigned to you.</p>
          </div>
        </div>
      </div>
    );
  }

  if (user.role === 'DRIVER') {
    return (
      <div className="pt-16">
        <div className="p-4 bg-gray-100 min-h-screen">
          <h1 className="text-2xl font-bold tracking-tight mb-4">Alerts</h1>
          <div className="grid grid-cols-2 md:grid-cols-5 gap-3 mb-4">
            <div className="bg-white p-3 rounded shadow"><div className="text-gray-600">Speed</div><div className="text-2xl font-bold">{countsByType.speed}</div></div>
            <div className="bg-white p-3 rounded shadow"><div className="text-gray-600">Fuel</div><div className="text-2xl font-bold">{countsByType.fuel}</div></div>
            <div className="bg-white p-3 rounded shadow"><div className="text-gray-600">Temperature</div><div className="text-2xl font-bold">{countsByType.temperature}</div></div>
            <div className="bg-white p-3 rounded shadow"><div className="text-gray-600">Maintenance</div><div className="text-2xl font-bold">{countsByType.maintenance}</div></div>
            <div className="bg-white p-3 rounded shadow"><div className="text-gray-600">Other</div><div className="text-2xl font-bold">{countsByType.other}</div></div>
          </div>

          <div className="overflow-x-auto bg-white rounded shadow">
            <table className="w-full border-collapse">
              <thead>
                <tr className="bg-gray-200 text-left">
                  <th className="p-3 font-semibold text-gray-700">Type</th>
                  <th className="p-3 font-semibold text-gray-700">Cause</th>
                  <th className="p-3 font-semibold text-gray-700">Value at event</th>
                  <th className="p-3 font-semibold text-gray-700">Severity</th>
                  <th className="p-3 font-semibold text-gray-700">Timestamp</th>
                </tr>
              </thead>
              <tbody>
                {pagedAlerts.map((a) => {
                  const v = deriveValueForAlert(a);
                  const cause = compactCause(a.type, v);
                  return (
                    <tr key={a.id} className="border-b">
                      <td className="p-3 flex items-center gap-2">{getAlertIcon(a.type)} {a.type}</td>
                      <td className="p-3">{cause}</td>
                      <td className="p-3">{v || '-'}</td>
                      <td className="p-3"><span className={`px-2 py-1 rounded-full text-xs font-medium tracking-wide ${severityColor[a.severity] || 'bg-gray-100 text-gray-600'}`}>{a.severity}</span></td>
                      <td className="p-3">{a.timestamp}</td>
                    </tr>
                  );
                })}
                {pagedAlerts.length === 0 && (
                  <tr><td className="p-3 text-gray-500" colSpan="5">No alerts found.</td></tr>
                )}
              </tbody>
            </table>
          </div>

          <div className="flex items-center justify-center gap-2 mt-3">
            <button className="px-3 py-1 border rounded disabled:opacity-50" onClick={() => setPage(p => Math.max(1, p - 1))} disabled={page === 1}>Prev</button>
            <span className="text-sm">Page {page} of {totalPages}</span>
            <button className="px-3 py-1 border rounded disabled:opacity-50" onClick={() => setPage(p => Math.min(totalPages, p + 1))} disabled={page === totalPages}>Next</button>
          </div>
        </div>
      </div>
    );
  }

  // Admin redesigned view
  return (
    <div className="pt-16">
      <div className="p-6 bg-gray-100 min-h-screen">
        <h1 className="text-2xl font-bold tracking-tight mb-4">Alerts Overview</h1>

        {/* Summary cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
          <div className="bg-white rounded-2xl shadow p-5 flex flex-col items-center">
            <AlertTriangle className="text-blue-500 mb-2" size={28} />
            <p class Name="text-xs uppercase tracking-wide text-gray-500 mb-1">Total Alerts</p>
            <p className="text-4xl font-extrabold text-gray-900">{alertStats.totalAlerts}</p>
          </div>
          <div className="bg-white rounded-2xl shadow p-5 flex flex-col items-center">
            <Clock className="text-yellow-500 mb-2" size={28} />
            <p className="text-xs uppercase tracking-wide text-gray-500 mb-1">Unacknowledged</p>
            <p className="text-4xl font-extrabold text-gray-900">{alertStats.unacknowledgedAlerts}</p>
          </div>
          <div className="bg-white rounded-2xl shadow p-5 flex flex-col items-center">
            <Zap className="text-red-500 mb-2" size={28} />
            <p className="text-xs uppercase tracking-wide text-gray-500 mb-1">Critical</p>
            <p className="text-4xl font-extrabold text-gray-900">{alertStats.criticalAlerts}</p>
          </div>
          <div className="bg-white rounded-2xl shadow p-5">
            <p className="text-base font-semibold text-gray-700 mb-3">By Severity</p>
            <div className="grid grid-cols-2 gap-y-2 text-sm">
              <span className="flex items-center gap-2 font-medium"><span className="w-3 h-3 bg-green-500 rounded-full"></span>Low: <span className="font-bold text-gray-900">{severityStats.lowAlerts}</span></span>
              <span className="flex items-center gap-2 font-medium"><span className="w-3 h-3 bg-yellow-500 rounded-full"></span>Medium: <span className="font-bold text-gray-900">{severityStats.mediumAlerts}</span></span>
              <span className="flex items-center gap-2 font-medium"><span className="w-3 h-3 bg-orange-500 rounded-full"></span>High: <span className="font-bold text-gray-900">{severityStats.highAlerts}</span></span>
              <span className="flex items-center gap-2 font-medium"><span className="w-3 h-3 bg-red-500 rounded-full"></span>Critical: <span className="font-bold text-gray-900">{severityStats.criticalAlerts}</span></span>
            </div>
          </div>
        </div>

        {/* Alerts by Severity */}
        <div className="bg-white rounded-2xl shadow p-5">
          <div className="flex justify-between items-center mb-6">
            <h3 className="text-lg font-semibold">Alerts by Severity</h3>
            <button
              className={`px-4 py-2 rounded-full text-sm font-semibold shadow transition 
        ${alerts.every(a => a.acknowledged)
                  ? "bg-green-500 hover:bg-green-600 text-white"
                  : "bg-red-500 hover:bg-red-600 text-white"
                }`}
              onClick={async () => {
                try {
                  await api.put("/alerts/acknowledge-all");
                  await loadAlerts();
                } catch (_) { }
              }}
            >
              {alerts.every(a => a.acknowledged) ? "All Acknowledged ✅" : "Acknowledge All"}
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {Object.entries(alertsBySeverity).map(([sev, list]) => (
              <div key={sev} className="border rounded-xl shadow-sm p-4">
                <div className={`inline-block px-3 py-1 rounded-full text-xs font-medium tracking-wide mb-3
          ${severityColor[sev] || 'bg-gray-100 text-gray-600'}`}>
                  {sev}
                </div>
                <div className="overflow-x-auto">
                  <table className="w-full text-sm border-collapse">
                    <thead>
                      <tr className="bg-gray-50 text-gray-700">
                        <th className="p-2 font-semibold">Time</th>
                        <th className="p-2 font-semibold">Car</th>
                        <th className="p-2 font-semibold">Trip</th>
                        <th className="p-2 font-semibold">Type</th>
                        <th className="p-2 font-semibold">Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {(pagedBySeverity[sev] || []).map(a => (
                        <tr key={a.id} className="border-b hover:bg-gray-50">
                          <td className="p-2 whitespace-nowrap">{new Date(a.timestamp).toLocaleString()}</td>
                          <td className="p-2">{a.carNumber || a.carId}</td>
                          <td className="p-2">{a.tripId || '-'}</td>
                          <td className="p-2">{a.type}</td>
                          <td className="p-2">
                            <button
                              className={`px-3 py-1 rounded-full text-xs font-semibold shadow transition 
                        ${a.acknowledged
                                  ? "bg-green-500 hover:bg-green-600 text-white"
                                  : "bg-red-500 hover:bg-red-600 text-white"
                                }`}
                              onClick={async () => {
                                try {
                                  await api.put(`/alerts/${a.id}/acknowledge`);
                                  await loadAlerts();
                                } catch (_) { }
                              }}
                            >
                              {a.acknowledged ? "Acknowledged" : "Acknowledge"}
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {/* Pagination */}
                <div className="flex items-center justify-center gap-3 mt-3">
                  <button className="px-3 py-1 border rounded-lg text-sm disabled:opacity-50"
                    onClick={() => setSevPage(p => ({ ...p, [sev]: Math.max(1, (p[sev] || 1) - 1) }))}
                    disabled={(sevPage[sev] || 1) === 1}>
                    Prev
                  </button>
                  <span className="text-xs text-gray-500">
                    Page {sevPage[sev] || 1} of {Math.max(1, Math.ceil((list || []).length / sevPageSize))}
                  </span>
                  <button className="px-3 py-1 border rounded-lg text-sm disabled:opacity-50"
                    onClick={() => setSevPage(p => ({ ...p, [sev]: Math.min(Math.max(1, Math.ceil((list || []).length / sevPageSize)), (p[sev] || 1) + 1) }))}
                    disabled={(sevPage[sev] || 1) >= Math.max(1, Math.ceil((list || []).length / sevPageSize))}>
                    Next
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AlertsPage;
