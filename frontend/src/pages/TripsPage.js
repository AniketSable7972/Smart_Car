// TripsPage.js
import React, { useEffect, useMemo, useState, useCallback } from "react";
import api from "../api/client";
import { useNavigate } from "react-router-dom";
import { MapPin, Flag, DollarSign, Clock, CheckCircle, ExternalLink } from "lucide-react";

const PUNE_POINTS = [
  "Shivajinagar, Pune",
  "Kothrud, Pune",
  "Hinjewadi, Pune",
  "Viman Nagar, Pune",
  "Kalyani Nagar, Pune",
];

const formatDate = (ts) => {
  if (!ts) return "-";
  const date = new Date(ts);
  return isNaN(date) ? "-" : date.toLocaleString();
};

const TripsPage = ({ user }) => {
  const navigate = useNavigate();
  const [costs, setCosts] = useState([]);
  const [startPoint, setStartPoint] = useState("");
  const [endPoint, setEndPoint] = useState("");
  const [estCost, setEstCost] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [trips, setTrips] = useState([]);
  const [historyFilter, setHistoryFilter] = useState("ALL");

  const loadCosts = async () => {
    try {
      const res = await api.get("/trips/costs");
      setCosts(res?.data?.data || []);
    } catch (_) { setCosts([]); }
  };

  const loadTrips = useCallback(async () => {
    try {
      const res = await api.get(`/trips/driver/${user.id}`);
      setTrips(res?.data?.data || []);
    } catch (_) { setTrips([]); }
  }, [user.id]);

  useEffect(() => { loadCosts(); loadTrips(); }, [user.id, loadTrips]);

  useEffect(() => {
    if (!startPoint || !endPoint || startPoint === endPoint) { setEstCost(null); return; }
    const match = (costs || []).find(c => c.startPoint === startPoint && c.endPoint === endPoint);
    setEstCost(match ? match.baseCost : null);
  }, [startPoint, endPoint, costs]);

  const submitRequest = async () => {
    if (!startPoint || !endPoint || startPoint === endPoint) return;
    setSubmitting(true);
    try {
      await api.post("/trips/request", { driverId: user.id, startPoint, endPoint });
      setStartPoint(""); setEndPoint("");
      await loadTrips();
      alert("Trip request submitted");
    } catch (e) { alert("Failed to submit request"); }
    finally { setSubmitting(false); }
  };

  const current = useMemo(() => {
    const pending = (trips || []).find(t => t.status === "REQUESTED");
    const active = (trips || []).find(t => t.status === "ACTIVE");
    const approved = (trips || []).find(t => t.status === "APPROVED");
    return active || approved || pending || null;
  }, [trips]);

  const history = useMemo(() => (trips || []).filter(t => ["REJECTED", "COMPLETED"].includes(t.status)), [trips]);

  // Sort descending by Trip ID & apply filter
  const filteredHistory = useMemo(() => {
    const sorted = [...history].sort((a, b) => b.id - a.id);
    return sorted.filter(t => {
      if (historyFilter === "ALL") return true;
      return t.status === historyFilter;
    });
  }, [history, historyFilter]);

  const statusBadge = (status) => {
    let color = "bg-gray-200 text-gray-800";
    if (status === "ACTIVE") color = "bg-green-100 text-green-800";
    if (status === "REQUESTED") color = "bg-yellow-100 text-yellow-800";
    if (status === "COMPLETED") color = "bg-blue-100 text-blue-800";
    if (status === "REJECTED") color = "bg-red-100 text-red-800";
    return <span className={`px-2 py-1 rounded-full text-xs font-semibold ${color}`}>{status}</span>;
  };

  return (
    <div className="pt-16 bg-gray-50 min-h-screen p-6">
      <h1 className="text-3xl font-bold text-gray-800 mb-6">Trips</h1>

      {/* Trip Request */}
      {!current && (
        <div className="bg-white rounded-2xl shadow-lg p-6 mb-8 hover:shadow-xl transition">
          <h2 className="text-xl font-semibold mb-4 flex items-center gap-2"><MapPin size={20} /> Request a Trip</h2>

          {/* Row 1: Start & End Points */}
          <div className="flex flex-col md:flex-row gap-4 mb-4">
            <div className="flex-1">
              <label className="block text-sm text-gray-600 mb-1 flex items-center gap-1"><MapPin size={16} /> Start Point</label>
              <select className="border rounded px-3 py-2 w-full" value={startPoint} onChange={(e) => setStartPoint(e.target.value)}>
                <option value="">Select start</option>
                {PUNE_POINTS.map((p) => (<option key={p} value={p}>{p}</option>))}
              </select>
            </div>
            <div className="flex-1">
              <label className="block text-sm text-gray-600 mb-1 flex items-center gap-1"><Flag size={16} /> End Point</label>
              <select className="border rounded px-3 py-2 w-full" value={endPoint} onChange={(e) => setEndPoint(e.target.value)}>
                <option value="">Select destination</option>
                {PUNE_POINTS.map((p) => (<option key={p} value={p}>{p}</option>))}
              </select>
            </div>
          </div>

          {/* Row 2: Estimated Cost */}
          <div className="mb-4">
            <label className="block text-sm text-gray-600 mb-1 flex items-center gap-1">Estimated Cost</label>
            <div className="text-xl font-semibold">{estCost !== null ? `₹${estCost}` : "-"}</div>
          </div>

          <button disabled={submitting || !startPoint || !endPoint || startPoint === endPoint} onClick={submitRequest} className="mt-2 px-5 py-2 bg-blue-600 text-white rounded-xl hover:bg-blue-700 disabled:opacity-50 transition">
            Submit Request
          </button>
        </div>
      )}


      {/* Current Trip */}
      {current && (
        <div className="bg-white rounded-2xl shadow-lg p-6 mb-8 hover:shadow-xl transition">
          <h2 className="text-xl font-semibold mb-2 flex items-center gap-2"><Clock size={18} /> Current Trip</h2>
          <div className="text-gray-700 mb-1 flex items-center gap-2">Status: {statusBadge(current.status)}</div>
          <div className="text-gray-700 mb-1 flex items-center gap-2"><MapPin size={16} /> Route: {current.startPoint} → {current.endPoint}</div>
          <div className="text-gray-700 mb-2 flex items-center gap-2">  Cost: ₹{current.totalCost} (Base ₹{current.baseCost} + Fine ₹{current.additionalFine})</div>
          {current.status === "ACTIVE" && (
            <button className="mt-2 px-4 py-2 bg-red-600 text-white rounded-xl hover:bg-red-700" onClick={async () => { try { await api.post(`/trips/${current.id}/stop`); await loadTrips(); alert("Trip stopped"); } catch (_) { } }}>Stop Trip</button>
          )}
          {current.status === "REQUESTED" && (
            <div className="text-sm text-gray-500 mt-2 flex items-center gap-1"><Clock size={14} /> Waiting for admin approval...</div>
          )}
        </div>
      )}

      {/* Trip History */}
      <div className="bg-white rounded-2xl shadow-lg p-6 hover:shadow-xl transition">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold flex items-center gap-2"><CheckCircle size={18} /> Trip History</h2>

          {/* Pill Filter Buttons */}
          <div className="flex gap-2">
            {["ALL", "COMPLETED", "REJECTED"].map((status) => (
              <button
                key={status}
                onClick={() => setHistoryFilter(status)}
                className={`px-3 py-1 rounded-full text-sm font-medium transition ${historyFilter === status
                  ? "bg-blue-600 text-white shadow-md"
                  : "bg-gray-200 text-gray-700 hover:bg-gray-300"
                  }`}
              >
                {status.charAt(0) + status.slice(1).toLowerCase()}
              </button>
            ))}
          </div>
        </div>

        {filteredHistory.length === 0 ? (
          <div className="text-sm text-gray-500">No trips yet.</div>
        ) : (
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-100 text-gray-600">
                <th className="p-2">Trip ID</th>
                <th className="p-2">Trip</th>
                <th className="p-2">Date & Time</th>
                <th className="p-2">Status</th>
                <th className="p-2">Cost</th>
                <th className="p-2">View</th>
              </tr>
            </thead>
            <tbody>
              {filteredHistory.map((t) => (
                <tr key={t.id} className="border-b hover:bg-gray-50 transition">
                  <td className="p-2">{t.id}</td>
                  <td className="p-2 flex items-center gap-1"><MapPin size={14} /> {t.startPoint} → {t.endPoint}</td>
                  <td className="p-2">
                    {formatDate(
                      t.status === "COMPLETED" ? t.endedAt :
                        t.status === "REJECTED" ? t.requestedAt :
                          t.startedAt || t.requestedAt
                    )}
                  </td>
                  <td className="p-2">{statusBadge(t.status)}</td>
                  <td className="p-2 flex items-center gap-1"> ₹{t.status === "REJECTED" ? 0 : t.totalCost}</td>
                  <td className="p-2">
                    <button
                      className="px-4 py-2 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-xl hover:from-blue-600 hover:to-blue-700 transform hover:scale-105 transition-all duration-200 shadow-md hover:shadow-lg flex items-center gap-2 font-medium text-sm"
                      onClick={() => navigate(`/trips/${t.id}`)}
                    >
                      <ExternalLink size={14} /> View Details
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default TripsPage;


