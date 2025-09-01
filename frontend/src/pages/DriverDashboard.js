// DriverDashboard.js
import React, { useEffect, useMemo, useState } from "react";
import { Gauge, Fuel, Thermometer, MapPin, AlertTriangle } from "lucide-react";
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

const DriverDashboard = ({ user }) => {
  const [carId, setCarId] = useState(null);
  const [latest, setLatest] = useState(null);
  const [stats, setStats] = useState(null);
  const [alerts, setAlerts] = useState([]);
  const [recentAlerts, setRecentAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTripId, setActiveTripId] = useState(null);

  const fetchDriverCar = async () => {
    try {
      const dres = await api.get(`/drivers/user/${user.id}`);
      const d = dres?.data?.data;
      const assigned = d?.assignedCarId || null;
      setCarId(assigned);
      if (!assigned) {
        setLoading(false);
      }
    } catch (_) {
      setCarId(null);
      setLoading(false);
    }
  };

  const fetchStats = async (carIdArg) => {
    try {
      // fetch active trip for current driver
      const dres = await api.get(`/drivers/user/${user.id}`);
      const driverId = dres?.data?.data?.id;
      let tripId = null;
      if (driverId) {
        const tRes = await api.get(`/trips/active/driver/${driverId}`);
        tripId = tRes?.data?.data?.id || null;
        setActiveTripId(tripId);
      }
      if (!tripId) { setStats(null); return; }
      const sRes = await api.get(`/telemetry/stats/trip/${tripId}`);
      setStats(sRes?.data?.data || null);
    } catch (_) {
      setStats(null);
    }
  };

  const fetchLatest = async (carIdArg) => {
    try {
      const tRes = await api.get(`/telemetry/car/${carIdArg}/latest`);
      const list = tRes?.data?.data || [];
      setLatest(list[0] || null);
    } catch (_) {
      setLatest(null);
    }
  };

  const fetchAlerts = async (carIdArg) => {
    try {
      const aRes = await api.get(`/alerts/car/${carIdArg}`);
      const arr = (aRes?.data?.data || []).sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
      setAlerts(arr);
      return arr;
    } catch (_) {
      setAlerts([]);
      return [];
    }
  };

  const fetchRecentAlertValues = async (carIdArg, baseAlerts) => {
    try {
      const candidate = (baseAlerts || []).slice(0, 3);
      if (candidate.length === 0) { setRecentAlerts([]); return; }
      const minTs = new Date(Math.min(...candidate.map(a => new Date(a.timestamp).getTime())));
      const maxTs = new Date(Math.max(...candidate.map(a => new Date(a.timestamp).getTime())));
      const start = new Date(minTs.getTime() - 10 * 60 * 1000);
      const end = new Date(maxTs.getTime() + 10 * 60 * 1000);
      const tRes = await api.get(`/telemetry/car/${carIdArg}/range`, {
        params: { startTime: formatLocalDateTime(start), endTime: formatLocalDateTime(end) }
      });
      const tList = tRes?.data?.data || [];
      const nearest = (ts) => {
        if (tList.length === 0) return null;
        const t = new Date(ts).getTime();
        let best = null, bestDiff = Number.MAX_SAFE_INTEGER;
        for (const rec of tList) {
          const diff = Math.abs(new Date(rec.timestamp).getTime() - t);
          if (diff < bestDiff) { best = rec; bestDiff = diff; }
        }
        return best;
      };
      const withValues = candidate.map(a => {
        const rec = nearest(a.timestamp);
        let value = null;
        const type = (a.type || "").toLowerCase();
        if (rec) {
          if (type.includes("fuel")) value = `${rec.fuelLevel}%`;
          else if (type.includes("temp")) value = `${rec.temperature}°C`;
          else if (type.includes("speed")) value = `${rec.speed} km/h`;
        }
        const cause = compactCause(a.type, value);
        return { ...a, derivedValue: value, cause };
      });
      setRecentAlerts(withValues);
    } catch (_) {
      setRecentAlerts((baseAlerts || []).slice(0, 3));
    }
  };

  const fetchAll = async () => {
    if (!carId) return;
    try {
      await Promise.all([
        fetchLatest(carId),
        fetchStats(carId),
        (async () => {
          const arr = await fetchAlerts(carId);
          await fetchRecentAlertValues(carId, arr);
        })(),
      ]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchDriverCar(); }, [user.id]);
  useEffect(() => { fetchAll(); }, [carId]);
  useEffect(() => { const id = setInterval(fetchAll, 10000); return () => clearInterval(id); }, [carId]);

  if (loading && carId === null) {
    return (
      <div className="pt-16"><div className="p-6 bg-gray-100 min-h-screen" /></div>
    );
  }

  if (!carId) {
    return (
      <div className="pt-16">
        <div className="p-6 bg-gray-100 min-h-screen">
          <div className="mb-6">
            <h1 className="text-3xl font-bold text-gray-800">Driver Dashboard</h1>
            <p className="text-gray-600">Not in a trip, data will appear once when the trip is confirmed.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="pt-16 bg-gray-50 min-h-screen p-6">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-800 mb-1">Driver Dashboard</h1>
        <p className="text-gray-600">
          Assigned Vehicle: <span className="font-semibold text-gray-800">{carId || "-"}</span>
        </p>
        {latest && (
          <p className="text-xs text-gray-500 mt-1">
            Last updated: {new Date(latest.timestamp).toLocaleString()}
          </p>
        )}
      </div>

      {/* Top Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
        {[
          { label: "Current Speed", value: latest?.speed ?? 0, icon: <Gauge size={28} />, color: "blue" },
          { label: "Fuel Level", value: latest?.fuelLevel ?? 0, icon: <Fuel size={28} />, color: "green" },
          { label: "Engine Temp", value: latest?.temperature ?? 0, icon: <Thermometer size={28} />, color: "orange" },
        ].map((card, idx) => (
          <div
            key={idx}
            className={`bg-white rounded-2xl shadow-lg hover:shadow-xl transition transform hover:-translate-y-1 flex items-center gap-4 p-5`}
          >
            <div className={`w-12 h-12 flex items-center justify-center rounded-full bg-${card.color}-100`}>
              {React.cloneElement(card.icon, { className: `text-${card.color}-500` })}
            </div>
            <div>
              <h3 className="text-sm text-gray-500">{card.label}</h3>
              <p className={`text-3xl font-bold text-${card.color}-600`}>
                {card.value} {card.label === "Current Speed" ? "km/h" : card.label === "Engine Temp" ? "°C" : "%"}
              </p>
            </div>
          </div>
        ))}
      </div>

      {/* Recent Alerts */}
      <div className="bg-white p-6 rounded-2xl shadow-lg mb-10">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-semibold flex items-center gap-2 text-red-600">
            <AlertTriangle size={20} /> Recent Alerts
          </h3>
          <span className="text-xs px-3 py-1 bg-gray-100 rounded-full text-gray-600">
            Last 3 events
          </span>
        </div>

        {recentAlerts.length === 0 ? (
          <p className="text-gray-500 text-sm text-center">No recent alerts 🎉</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {recentAlerts.map((a) => {
              const typeLower = (a.type || "").toLowerCase();
              const color = typeLower.includes("fuel") ? "green" : typeLower.includes("temp") ? "orange" : "blue";
              const Icon = typeLower.includes("fuel") ? Fuel : typeLower.includes("temp") ? Thermometer : Gauge;

              return (
                <div
                  key={a.id}
                  className={`p-4 rounded-xl border-l-4 border-${color}-500 bg-gray-50 shadow-sm hover:shadow-md transition`}
                >
                  <div className="flex items-center gap-2 mb-1">
                    <Icon size={16} className={`text-${color}-500`} />
                    <span className="text-xs text-gray-500">{new Date(a.timestamp).toLocaleString()}</span>
                  </div>
                  <div className="font-semibold text-gray-800">{a.type}</div>
                  <div className="text-sm text-gray-600 mt-1">{a.cause || a.message || "-"}</div>
                  <div className="text-sm mt-2">
                    <span className="text-gray-500">Value:</span>{" "}
                    <span className="font-medium">{a.derivedValue || "-"}</span>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Bottom Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {[
          { label: "Avg Speed (24h)", value: stats?.averageSpeed ?? 0, min: stats?.minSpeed, max: stats?.maxSpeed, icon: <Gauge />, color: "blue", unit: "km/h" },
          { label: "Avg Fuel (24h)", value: stats?.averageFuel ?? 0, min: stats?.minFuel, max: stats?.maxFuel, icon: <Fuel />, color: "green", unit: "%" },
          { label: "Avg Temp (24h)", value: stats?.averageTemperature ?? 0, min: stats?.minTemperature, max: stats?.maxTemperature, icon: <Thermometer />, color: "orange", unit: "°C" },
          { label: "Alerts (24h)", value: alerts.length, icon: <AlertTriangle />, color: "red", unit: "", extra: "Fuel / Temp / Speed issues" },
        ].map((stat, idx) => (
          <div
            key={idx}
            className="bg-white p-5 rounded-2xl shadow-lg flex flex-col hover:shadow-xl transition transform hover:-translate-y-1"
          >
            <div className="flex justify-between items-center mb-2">
              <h2 className="text-sm text-gray-500">{stat.label}</h2>
              {React.cloneElement(stat.icon, { className: `text-${stat.color}-500`, size: 20 })}
            </div>
            <p className={`text-2xl font-bold text-${stat.color}-600`}>
              {stat.value} {stat.unit}
            </p>
            {stat.min !== undefined && stat.max !== undefined && (
              <p className="text-xs text-gray-500">Min {stat.min} • Max {stat.max}</p>
            )}
            {stat.extra && <p className="text-xs text-gray-500">{stat.extra}</p>}
          </div>
        ))}
      </div>
    </div>
  );
};

export default DriverDashboard;
