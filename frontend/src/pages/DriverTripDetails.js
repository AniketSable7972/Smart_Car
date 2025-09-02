// DriverTripDetails.js
import React, { useEffect, useMemo, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../api/client";

// Icons for better visual representation
const Icons = {
  time: "🕒",
  speed: "🚗",
  fuel: "⛽",
  temperature: "🌡️",
  alert: "⚠️",
  type: "📋",
  severity: "🔴",
  penalty: "💰",
  history: "📊",
  alerts: "🚨"
};

const TabButton = ({ active, onClick, children, icon }) => (
  <button
    onClick={onClick}
    className={`flex items-center gap-2 px-4 py-3 border-b-2 transition-all duration-200 ${active
      ? "border-blue-600 text-blue-600 bg-blue-50"
      : "border-transparent text-gray-600 hover:text-blue-600 hover:bg-gray-50"
      } rounded-t-lg font-medium`}
  >
    <span>{icon}</span>
    {children}
  </button>
);

const SeverityBadge = ({ severity }) => {
  const severityLevel = String(severity || "").toUpperCase();
  const getBadgeStyle = (level) => {
    switch (level) {
      case "LOW":
        return "bg-green-100 text-green-800 border-green-200";
      case "MEDIUM":
        return "bg-yellow-100 text-yellow-800 border-yellow-200";
      case "HIGH":
        return "bg-orange-100 text-orange-800 border-orange-200";
      case "CRITICAL":
        return "bg-red-100 text-red-800 border-red-200";
      default:
        return "bg-gray-100 text-gray-800 border-gray-200";
    }
  };

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${getBadgeStyle(
        severityLevel
      )}`}
    >
      {severityLevel}
    </span>
  );
};

const PAGE_SIZE = 10;

const DriverTripDetails = () => {
  const { tripId } = useParams();
  const navigate = useNavigate();
  const [tab, setTab] = useState("history");
  const [trip, setTrip] = useState(null);
  const [telemetry, setTelemetry] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tPage, setTPage] = useState(1);
  const [aPage, setAPage] = useState(1);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const [tripRes, tRes, aRes] = await Promise.all([
          api.get(`/trips/${tripId}`),
          api.get(`/telemetry/trip/${tripId}`),
          api.get(`/alerts/trip/${tripId}`)
        ]);
        setTrip(tripRes?.data?.data || null);
        setTelemetry(tRes?.data?.data || []);
        setAlerts(aRes?.data?.data || []);
        setTPage(1);
        setAPage(1);
      } catch (_) {
        setTrip(null);
        setTelemetry([]);
        setAlerts([]);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [tripId]);

  const pagedTelemetry = useMemo(() => {
    const sorted = [...telemetry].sort(
      (a, b) => new Date(b.timestamp) - new Date(a.timestamp)
    );
    const start = (tPage - 1) * PAGE_SIZE;
    return sorted.slice(start, start + PAGE_SIZE);
  }, [telemetry, tPage]);
  const tTotal = Math.max(1, Math.ceil(telemetry.length / PAGE_SIZE));

  const pagedAlerts = useMemo(() => {
    const sorted = [...alerts].sort(
      (a, b) => new Date(b.timestamp) - new Date(a.timestamp)
    );
    const start = (aPage - 1) * PAGE_SIZE;
    return sorted.slice(start, start + PAGE_SIZE);
  }, [alerts, aPage]);
  const aTotal = Math.max(1, Math.ceil(alerts.length / PAGE_SIZE));

  return (
    <div className="pt-16">
      <div className="p-6 bg-gradient-to-br from-gray-50 to-gray-100 min-h-screen">
        <div className="max-w-7xl mx-auto">
          {/* Back Button */}
          <div className="mb-6">
            <button
              onClick={() => navigate('/trips')}
              className="flex items-center gap-3 px-6 py-3 bg-gradient-to-r from-gray-100 to-gray-200 text-gray-700 hover:from-gray-200 hover:to-gray-300 hover:text-gray-900 rounded-xl transition-all duration-300 font-semibold shadow-sm hover:shadow-md transform hover:-translate-x-1"
            >
              <span className="text-lg">←</span>
              Back to Trip History
            </button>
          </div>

          <h1 className="text-3xl font-bold mb-6 text-gray-800 flex items-center gap-3">
            <span className="text-blue-600">🚗</span>
            Trip #{tripId}
          </h1>

          {/* Trip Info Cards */}
          {trip && (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
              {[
                { label: "Car Model", value: trip.carModel, icon: "🚗", bg: "bg-blue-100" },
                { label: "Car Number", value: trip.carNumber, icon: "🔢", bg: "bg-green-100" },
                { label: "Start Point", value: trip.startPoint, icon: "📍", bg: "bg-purple-100" },
                { label: "End Point", value: trip.endPoint, icon: "🎯", bg: "bg-orange-100" }
              ].map((card, idx) => (
                <div
                  key={idx}
                  className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200"
                >
                  <div className="flex items-center gap-3">
                    <div className={`p-2 ${card.bg} rounded-lg`}>
                      <span className="text-2xl">{card.icon}</span>
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-600">{card.label}</p>
                      <p className="text-lg font-semibold text-gray-900">{card.value}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Tabs */}
          <div className="flex gap-1 border-b border-gray-200 mb-6 bg-white rounded-t-lg p-1">
            <TabButton active={tab === "history"} onClick={() => setTab("history")} icon={Icons.history}>
              History
            </TabButton>
            <TabButton active={tab === "alerts"} onClick={() => setTab("alerts")} icon={Icons.alerts}>
              Alerts
            </TabButton>
          </div>

          {/* Loading */}
          {loading ? (
            <div className="flex items-center justify-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              <span className="ml-3 text-gray-600">Loading...</span>
            </div>
          ) : tab === "history" ? (
            /* Telemetry Table */
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
              <div className="px-6 py-4 border-b border-gray-200 bg-gray-50">
                <h3 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
                  <span>{Icons.history}</span> Telemetry History
                </h3>
              </div>

              {telemetry.length === 0 ? (
                <div className="text-center py-12 text-gray-500">
                  <span className="text-4xl mb-4 block">📊</span>
                  <p>No telemetry records found for this trip.</p>
                </div>
              ) : (
                <>
                  <div className="overflow-x-auto">
                    <table className="w-full">
                      <thead>
                        <tr className="bg-gray-50">
                          {[
                            { label: "Time", icon: Icons.time },
                            { label: "Speed", icon: Icons.speed },
                            { label: "Fuel", icon: Icons.fuel },
                            { label: "Temperature", icon: Icons.temperature }
                          ].map((col, idx) => (
                            <th
                              key={idx}
                              className="px-6 py-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                            >
                              <div className="flex items-center gap-2">{col.icon} {col.label}</div>
                            </th>
                          ))}
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-200">
                        {pagedTelemetry.map((t, idx) => (
                          <tr key={idx} className="hover:bg-gray-50 transition-colors duration-150">
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{new Date(t.timestamp).toLocaleString()}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900"><span className="font-medium">{t.speed}</span> km/h</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900"><span className="font-medium">{t.fuelLevel}%</span></td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900"><span className="font-medium">{t.temperature}°C</span></td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {/* Pagination */}
                  <div className="px-6 py-4 border-t border-gray-200 bg-gray-50">
                    <div className="flex items-center justify-center gap-3">
                      <button
                        className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 hover:border-gray-400 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
                        onClick={() => setTPage((p) => Math.max(1, p - 1))}
                        disabled={tPage === 1}
                      >
                        ← Previous
                      </button>
                      <span className="text-sm text-gray-600 font-medium">
                        Page {tPage} of {tTotal}
                      </span>
                      <button
                        className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 hover:border-gray-400 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
                        onClick={() => setTPage((p) => Math.min(tTotal, p + 1))}
                        disabled={tPage === tTotal}
                      >
                        Next →
                      </button>
                    </div>
                  </div>
                </>
              )}
            </div>
          ) : (
            /* Alerts Table */
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
              <div className="px-6 py-4 border-b border-gray-200 bg-gray-50">
                <h3 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
                  <span>{Icons.alerts}</span> Trip Alerts
                </h3>
              </div>

              {alerts.length === 0 ? (
                <div className="text-center py-12 text-gray-500">
                  <span className="text-4xl mb-4 block">✅</span>
                  <p>No alerts found for this trip.</p>
                </div>
              ) : (
                <>
                  <div className="overflow-x-auto">
                    <table className="w-full">
                      <thead>
                        <tr className="bg-gray-50">
                          {[
                            { label: "Time", icon: Icons.time },
                            { label: "Type", icon: Icons.type },
                            { label: "Severity", icon: Icons.severity },
                            { label: "Penalty", icon: Icons.penalty }
                          ].map((col, idx) => (
                            <th
                              key={idx}
                              className="px-6 py-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                            >
                              <div className="flex items-center gap-2">{col.icon} {col.label}</div>
                            </th>
                          ))}
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-200">
                        {pagedAlerts.map((a) => {
                          const t = telemetry.find(
                            (r) =>
                              Math.abs(
                                new Date(r.timestamp).getTime() - new Date(a.timestamp).getTime()
                              ) < 5 * 60 * 1000
                          ) || null;

                          const sev = String(a.severity || "").toUpperCase();
                          const penalty = sev === "HIGH" ? 10 : sev === "CRITICAL" ? 20 : 0;

                          return (
                            <tr key={a.id} className="hover:bg-gray-50 transition-colors duration-150">
                              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{new Date(a.timestamp).toLocaleString()}</td>
                              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900"><span className="font-medium">{a.type}</span></td>
                              <td className="px-6 py-4 whitespace-nowrap"><SeverityBadge severity={a.severity} /></td>
                              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900"><span className="font-medium text-red-600">₹{penalty}</span></td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>

                  {/* Pagination */}
                  <div className="px-6 py-4 border-t border-gray-200 bg-gray-50">
                    <div className="flex items-center justify-center gap-3">
                      <button
                        className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 hover:border-gray-400 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
                        onClick={() => setAPage((p) => Math.max(1, p - 1))}
                        disabled={aPage === 1}
                      >
                        ← Previous
                      </button>
                      <span className="text-sm text-gray-600 font-medium">
                        Page {aPage} of {aTotal}
                      </span>
                      <button
                        className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 hover:border-gray-400 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
                        onClick={() => setAPage((p) => Math.min(aTotal, p + 1))}
                        disabled={aPage === aTotal}
                      >
                        Next →
                      </button>
                    </div>
                  </div>
                </>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default DriverTripDetails;


