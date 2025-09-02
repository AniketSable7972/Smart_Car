// CarAlertsPage.js
import React, { useEffect, useState, useMemo } from "react";
import { useParams } from "react-router-dom";
import { AlertTriangle, Fuel, Thermometer, Wrench, Zap } from "lucide-react";
import api from "../api/client";

const THRESHOLDS = { speed: 100, fuel: 20, temperature: 100 };
const compactCause = (type, valueStr) => {
  const t = (type || "").toLowerCase();
  const num = parseFloat((valueStr || "").replace(/[^0-9.\-]/g, ""));
  if (t.includes("speed")) return `Speed ${isNaN(num) ? valueStr : num} > ${THRESHOLDS.speed} (overspeed)`;
  if (t.includes("fuel")) return `Fuel ${isNaN(num) ? valueStr : num}% < ${THRESHOLDS.fuel}% (low fuel)`;
  if (t.includes("temp")) return `Temp ${isNaN(num) ? valueStr : num}°C > ${THRESHOLDS.temperature}°C (overheat)`;
  return "Threshold exceeded";
};

const CarAlertsPage = () => {
  const { carId } = useParams();
  const [alerts, setAlerts] = useState([]);
  const [page, setPage] = useState(1);
  const pageSize = 10;

  const [telemetryWindow, setTelemetryWindow] = useState([]);

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

  const loadAlerts = async () => {
    try {
      const res = await api.get(`/alerts/car/${carId}`);
      const arr = (res?.data?.data || []).sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
      setAlerts(arr);
      setPage(1);
    } catch (_) { setAlerts([]); }
  };

  const acknowledgeOne = async (id) => {
    try {
      await api.put(`/alerts/${id}/acknowledge`);
      setAlerts((prev) => prev.map((a) => a.id === id ? { ...a, acknowledged: true } : a));
    } catch (_) { }
  };

  const acknowledgeAll = async () => {
    try {
      const list = [...alerts];
      await Promise.all(list.filter(a => !a.acknowledged).map((a) => api.put(`/alerts/${a.id}/acknowledge`)));
      setAlerts((prev) => prev.map((a) => ({ ...a, acknowledged: true })));
    } catch (_) { }
  };

  const loadTelemetryWindow = async () => {
    try {
      const end = new Date();
      const start = new Date(end.getTime() - 24 * 60 * 60 * 1000);
      const tRes = await api.get(`/telemetry/car/${carId}/range`, { params: { startTime: formatLocalDateTime(start), endTime: formatLocalDateTime(end) } });
      setTelemetryWindow(tRes?.data?.data || []);
    } catch (_) { setTelemetryWindow([]); }
  };

  useEffect(() => { loadAlerts(); loadTelemetryWindow(); }, [carId]);

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

  const pagedAlerts = useMemo(() => {
    const start = (page - 1) * pageSize;
    return alerts.slice(start, start + pageSize);
  }, [alerts, page]);

  const totalPages = Math.max(1, Math.ceil(alerts.length / pageSize));

  return (
    <div className="pt-16">
      <div className="p-4 bg-gray-100 min-h-screen">
        <div className="flex items-center justify-between mb-3">
          <h1 className="text-2xl font-bold">Car {carId} Alerts</h1>
          <button onClick={acknowledgeAll} className="px-3 py-2 bg-blue-600 text-white rounded disabled:opacity-50" disabled={alerts.every(a => a.acknowledged)}>Acknowledge All</button>
        </div>
        <div className="grid grid-cols-2 md:grid-cols-5 gap-3 mb-4">
          <div className="bg-white p-3 rounded shadow"><div className="text-gray-600">Speed</div><div className="text-2xl font-bold">{alerts.filter(a => (a.type || '').toLowerCase().includes('speed')).length}</div></div>
          <div className="bg-white p-3 rounded shadow"><div className="text-gray-600">Fuel</div><div className="text-2xl font-bold">{alerts.filter(a => (a.type || '').toLowerCase().includes('fuel')).length}</div></div>
          <div className="bg-white p-3 rounded shadow"><div className="text-gray-600">Temperature</div><div className="text-2xl font-bold">{alerts.filter(a => (a.type || '').toLowerCase().includes('temp')).length}</div></div>
          <div className="bg-white p-3 rounded shadow"><div className="text-gray-600">Maintenance</div><div className="text-2xl font-bold">{alerts.filter(a => (a.type || '').toLowerCase().includes('maint')).length}</div></div>
          <div className="bg-white p-3 rounded shadow"><div className="text-gray-600">Total</div><div className="text-2xl font-bold">{alerts.length}</div></div>
        </div>

        <div className="overflow-x-auto bg-white rounded shadow">
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-gray-200 text-left">
                <th className="p-3">Type</th>
                <th className="p-3">Cause</th>
                <th className="p-3">Value at event</th>
                <th className="p-3">Severity</th>
                <th className="p-3">Timestamp</th>
                <th className="p-3">Action</th>
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
                    <td className="p-3"><span className={`px-2 py-1 rounded text-xs ${severityColor[a.severity] || 'bg-gray-100 text-gray-600'}`}>{a.severity}</span></td>
                    <td className="p-3">{a.timestamp}</td>
                    <td className="p-3">
                      <button
                        className={`px-2 py-1 rounded ${a.acknowledged ? 'bg-gray-300 text-gray-600 cursor-not-allowed' : 'bg-green-600 text-white'}`}
                        disabled={!!a.acknowledged}
                        onClick={() => acknowledgeOne(a.id)}
                      >
                        {a.acknowledged ? 'Acknowledged' : 'Acknowledge'}
                      </button>
                    </td>
                  </tr>
                );
              })}
              {pagedAlerts.length === 0 && (
                <tr><td className="p-3 text-gray-500" colSpan="6">No alerts found.</td></tr>
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
};

export default CarAlertsPage;


