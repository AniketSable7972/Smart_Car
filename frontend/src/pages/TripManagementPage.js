// TripManagementPage.js
import React, { useEffect, useState } from "react";
import api from "../api/client";
import { useNavigate } from "react-router-dom";
import { Car, Clock, CheckCircle2, XCircle, MapPin } from "lucide-react";

// ✅ Helper to format time nicely
const formatDateTime = (dateStr) => {
  if (!dateStr) return "N/A";
  const d = new Date(dateStr);
  return new Intl.DateTimeFormat("en-GB", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    hour12: true,
  }).format(d);
};

const TripManagementPage = () => {
  const [pending, setPending] = useState([]);
  const [active, setActive] = useState([]);
  const [completed, setCompleted] = useState([]);
  const [carIdByTrip, setCarIdByTrip] = useState({});
  const [idleCars, setIdleCars] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const navigate = useNavigate();

  const tripsPerPage = 12;

  const load = async () => {
    try {
      const [pRes, aRes, idleRes, cRes] = await Promise.all([
        api.get("/trips/pending"),
        api.get("/trips/active"),
        api.get("/cars/status/IDLE"),
        api.get("/trips/completed"),
      ]);
      setPending(pRes?.data?.data || []);
      setActive(aRes?.data?.data || []);
      setIdleCars(idleRes?.data?.data || []);
      setCompleted(cRes?.data?.data || []);
    } catch (_) {
      setPending([]);
      setActive([]);
      setIdleCars([]);
      setCompleted([]);
    }
  };

  useEffect(() => {
    load();
    const id = setInterval(load, 30000);
    return () => clearInterval(id);
  }, []);

  // Helper: sort trips
  const sortedCompleted = [...completed].sort(
    (a, b) => new Date(b.endedAt) - new Date(a.endedAt)
  );

  // Pagination logic
  const indexOfLastTrip = currentPage * tripsPerPage;
  const indexOfFirstTrip = indexOfLastTrip - tripsPerPage;
  const currentTrips = sortedCompleted.slice(indexOfFirstTrip, indexOfLastTrip);

  const totalPages = Math.ceil(sortedCompleted.length / tripsPerPage);

  const approveStart = async (tripId) => {
    try {
      const carId = carIdByTrip[tripId];
      if (!carId) {
        alert("⚠️ Please assign an IDLE car before approving the trip.");
        return;
      }

      const url = `/trips/${tripId}/approve-start?carId=${carId}`;
      await api.post(url);
      await load();
      alert("✅ Trip approved and started");
    } catch (_) {
      alert("❌ Failed to approve/start");
    }
  };

  const reject = async (tripId) => {
    try {
      await api.post(`/trips/${tripId}/reject`);
      await load();
      alert("Trip rejected");
    } catch (_) {
      alert("Failed to reject");
    }
  };

  return (
    <div className="pt-16 font-inter">
      <div className="p-6 bg-gray-50 min-h-screen">
        {/* Main Header */}
        <h1 className="text-3xl font-bold mb-6 text-gray-900 flex items-center gap-2 tracking-tight">
          <Car className="w-8 h-8 text-blue-600" />
          Trip Management Dashboard
        </h1>

        {/* Pending Requests */}
        <div className="mb-8">
          <h2 className="text-lg font-semibold mb-3 text-gray-700 uppercase tracking-wide">
            Pending Requests
          </h2>
          {pending.length === 0 ? (
            <div className="text-gray-500 bg-white p-4 rounded-lg shadow-sm text-sm font-medium">
              No pending requests 🚫
            </div>
          ) : (
            <div className="grid gap-4 md:grid-cols-2">
              {pending.map((t) => (
                <div
                  key={t.id}
                  className="bg-white shadow rounded-xl p-5 border hover:shadow-md transition"
                >
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center gap-2 text-gray-800 font-semibold">
                      <MapPin className="w-4 h-4 text-blue-500" />
                      {t.startPoint} → {t.endPoint}
                    </div>
                    <span className="text-xs px-2 py-1 bg-yellow-100 text-yellow-700 rounded font-medium tracking-wide">
                      Pending
                    </span>
                  </div>
                  <p className="text-sm text-gray-600 mb-2">
                    Driver: <span className="font-medium">{t.driverId}</span> {t.driverName && `(${t.driverName})`}
                  </p>

                  <select
                    className="border rounded-lg px-3 py-2 w-full text-sm mb-3 focus:ring-2 focus:ring-blue-400"
                    value={carIdByTrip[t.id] || ""}
                    onChange={(e) =>
                      setCarIdByTrip((prev) => ({ ...prev, [t.id]: e.target.value }))
                    }
                  >
                    <option value="">🚗 Select IDLE car</option>
                    {idleCars.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.carNumber} • {c.carModel}
                      </option>
                    ))}
                  </select>

                  <div className="flex gap-2">
                    <button
                      className="flex-1 px-3 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 flex items-center justify-center gap-1 font-medium"
                      onClick={() => approveStart(t.id)}
                    >
                      <CheckCircle2 className="w-4 h-4" /> Approve
                    </button>
                    <button
                      className="flex-1 px-3 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 flex items-center justify-center gap-1 font-medium"
                      onClick={() => reject(t.id)}
                    >
                      <XCircle className="w-4 h-4" /> Reject
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Active Trips */}
        <div className="mb-8">
          <h2 className="text-lg font-semibold mb-3 text-gray-700 uppercase tracking-wide">
            Active Trips
          </h2>
          {active.length === 0 ? (
            <div className="text-gray-500 bg-white p-4 rounded-lg shadow-sm text-sm font-medium">
              No active trips 🚙
            </div>
          ) : (
            <div className="grid gap-4 md:grid-cols-2">
              {active.map((t) => (
                <div
                  key={t.id}
                  className="bg-blue-50 shadow rounded-xl p-5 border border-blue-200 hover:shadow-md transition"
                >
                  <div className="flex justify-between items-center mb-2">
                    <p className="font-semibold text-gray-900">
                      {t.startPoint} → {t.endPoint}
                    </p>
                    <span className="text-xs px-2 py-1 bg-green-100 text-green-700 rounded font-medium">
                      Active
                    </span>
                  </div>
                  <p className="text-sm text-gray-600">
                    Driver: <span className="font-medium">{t.driverId}</span> {t.driverName && `(${t.driverName})`}
                  </p>
                  <p className="text-sm text-gray-600">
                    Cost: <span className="font-medium">₹{t.totalCost}</span> (+ fine ₹{t.additionalFine})
                  </p>
                  <div className="flex items-center text-xs text-gray-500 mt-2">
                    <Clock className="w-4 h-4 mr-1" />
                    Started at: {formatDateTime(t.startedAt)}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Completed Trips */}
        <div>
          <h2 className="text-lg font-semibold mb-3 text-gray-700 uppercase tracking-wide">
            Completed Trips
          </h2>
          {completed.length === 0 ? (
            <div className="text-gray-500 bg-white p-4 rounded-lg shadow-sm text-sm font-medium">
              No completed trips ✅
            </div>
          ) : (
            <>
              <div className="grid gap-4 md:grid-cols-3">
                {currentTrips.map((t) => (
                  <div
                    key={t.id}
                    className="bg-white shadow rounded-xl p-5 border hover:shadow-md transition"
                  >
                    <p className="font-semibold text-gray-900 mb-1">
                      {t.startPoint} → {t.endPoint}
                    </p>
                    <p className="text-sm text-gray-600 mb-1">
                      Driver: <span className="font-medium">{t.driverId}</span> {t.driverName && `(${t.driverName})`}
                    </p>
                    <p className="text-sm text-gray-600 mb-1">
                      Cost: <span className="font-medium">₹{t.totalCost}</span> (+ fine ₹{t.additionalFine})
                    </p>
                    <p className="text-xs text-gray-500 mb-3">
                      Ended: {formatDateTime(t.endedAt)}
                    </p>
                    <button
                      className="px-3 py-2 text-sm border rounded-lg w-full hover:bg-gray-100 font-medium"
                      onClick={() => navigate(`/trips/${t.id}`)}
                    >
                      Open Details
                    </button>
                  </div>
                ))}
              </div>

              {/* Pagination Controls */}
              {totalPages > 1 && (
                <div className="flex justify-center items-center gap-2 mt-6">
                  <button
                    onClick={() => setCurrentPage((p) => Math.max(p - 1, 1))}
                    disabled={currentPage === 1}
                    className="px-3 py-1.5 text-sm rounded-lg border bg-white disabled:opacity-50 hover:bg-gray-100"
                  >
                    Prev
                  </button>
                  <span className="text-sm font-medium">
                    Page {currentPage} of {totalPages}
                  </span>
                  <button
                    onClick={() => setCurrentPage((p) => Math.min(p + 1, totalPages))}
                    disabled={currentPage === totalPages}
                    className="px-3 py-1.5 text-sm rounded-lg border bg-white disabled:opacity-50 hover:bg-gray-100"
                  >
                    Next
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default TripManagementPage;
