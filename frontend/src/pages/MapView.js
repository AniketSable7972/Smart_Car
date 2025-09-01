// MapView.js
import React, { useState, useEffect } from "react";
import { MapPin, RefreshCcw, Car } from "lucide-react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import api from "../api/client";

// Fix for default marker icon missing in some setups
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-shadow.png',
});

const ChangeView = ({ center }) => {
  const map = useMap();
  useEffect(() => { if (center) map.flyTo(center, map.getZoom()); }, [center, map]);
  return null;
};

// Minimal city-to-coordinates mapping for simulator locations
const CITY_COORDS = {
  "New York, NY": [40.7128, -74.0060],
  "Los Angeles, CA": [34.0522, -118.2437],
  "Chicago, IL": [41.8781, -87.6298],
  "Houston, TX": [29.7604, -95.3698],
  "Phoenix, AZ": [33.4484, -112.0740],
  "Philadelphia, PA": [39.9526, -75.1652],
  "San Antonio, TX": [29.4241, -98.4936],
  "San Diego, CA": [32.7157, -117.1611],
  "Dallas, TX": [32.7767, -96.7970],
  "San Jose, CA": [37.3382, -121.8863]
};

const MapView = ({ user }) => {
  const [carId, setCarId] = useState(null);
  const [latest, setLatest] = useState(null);
  const [center, setCenter] = useState([40, -100]);
  const [fleet, setFleet] = useState([]);
  const [driversByCarId, setDriversByCarId] = useState({});
  const [searchTerm, setSearchTerm] = useState("");
  const [page, setPage] = useState(1);
  const pageSize = 10;

  const isAdmin = user?.role === 'ADMIN';

  const fetchDriverCar = async () => {
    try {
      const dres = await api.get(`/drivers/user/${user.id}`);
      const d = dres?.data?.data;
      setCarId(d?.assignedCarId || null);
    } catch (_) {
      setCarId(null);
    }
  };

  const fetchLatest = async () => {
    if (!carId) return;
    try {
      const tRes = await api.get(`/telemetry/car/${carId}/latest`);
      const list = tRes?.data?.data || [];
      const l = list[0] || null;
      setLatest(l);
      const coords = l?.location && CITY_COORDS[l.location] ? CITY_COORDS[l.location] : null;
      if (coords) setCenter(coords);
    } catch (_) { /* ignore */ }
  };

  const fetchAssignedDrivers = async () => {
    try {
      const dRes = await api.get('/drivers/assigned');
      const list = dRes?.data?.data || [];
      const map = {};
      for (const d of list) {
        if (d.assignedCarId) {
          map[d.assignedCarId] = { id: d.id, name: d.name || d.username || "" };
        }
      }
      setDriversByCarId(map);
    } catch (_) {
      setDriversByCarId({});
    }
  };

  const fetchFleetLatest = async () => {
    try {
      const [teleRes, carsRes] = await Promise.all([
        api.get('/telemetry/latest/all'),
        api.get('/cars'),
      ]);
      const arr = teleRes?.data?.data || [];
      const activeCars = carsRes?.data?.data || [];
      const activeIds = new Set(activeCars.map((c) => c.id));
      const filtered = arr.filter((t) => activeIds.has(t.carId));
      setFleet(filtered);
      const firstWithCoords = filtered.find((t) => t?.location && CITY_COORDS[t.location]);
      if (firstWithCoords) setCenter(CITY_COORDS[firstWithCoords.location]);
      await fetchAssignedDrivers();
    } catch (_) {
      setFleet([]);
    }
  };

  useEffect(() => { if (!isAdmin) fetchDriverCar(); }, [user.id, isAdmin]);
  useEffect(() => { if (!isAdmin) fetchLatest(); }, [carId, isAdmin]);
  useEffect(() => { if (isAdmin) fetchFleetLatest(); }, [isAdmin]);

  useEffect(() => {
    if (isAdmin) {
      const id = setInterval(fetchFleetLatest, 30000);
      return () => clearInterval(id);
    }
    const id = setInterval(fetchLatest, 10000);
    return () => clearInterval(id);
  }, [isAdmin, carId]);

  useEffect(() => { setPage(1); }, [searchTerm, fleet, driversByCarId]);

  // Precompute admin list filter/pagination consistently (no conditional hooks)
  const adminQuery = (searchTerm || "").toLowerCase().trim();
  const adminFilteredFleet = !isAdmin ? [] : (
    adminQuery
      ? fleet.filter((t) => {
          const carStr = String(t.carId || "").toLowerCase();
          const d = driversByCarId[t.carId];
          const nameStr = String(d?.name || "").toLowerCase();
          return carStr.includes(adminQuery) || nameStr.includes(adminQuery);
        })
      : fleet
  );
  const adminTotalPages = Math.max(1, Math.ceil(adminFilteredFleet.length / pageSize));
  const adminPagedFleet = adminFilteredFleet.slice((page - 1) * pageSize, (page - 1) * pageSize + pageSize);

  if (user.role === 'DRIVER' && !carId) {
    return (
      <div className="pt-16">
        <div className="p-6 bg-gray-100 min-h-screen flex items-center justify-center">
          <div className="bg-white p-6 rounded shadow text-center max-w-md">
            <h2 className="text-xl font-semibold mb-2">No vehicle assigned</h2>
            <p className="text-gray-600">You will see the map once a vehicle is assigned to you.</p>
          </div>
        </div>
      </div>
    );
  }

  if (isAdmin) {
    const filteredFleet = adminFilteredFleet;
    const pagedFleet = adminPagedFleet;
    const totalPages = adminTotalPages;
    return (
      <div className="pt-16">
        <div className="p-6 bg-gray-100 min-h-screen">
          <div className="w-full bg-white rounded-lg shadow p-4 mb-4">
            <div className="flex flex-col md:flex-row md:justify-between md:items-center gap-3">
              <h2 className="text-lg font-bold flex items-center gap-2">
                <MapPin size={20} /> Fleet Map & Vehicles
              </h2>
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Search by car ID or driver name"
                className="border px-3 py-2 rounded w-full md:w-80"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="md:col-span-2">
              <div className="w-full h-96 border rounded overflow-hidden bg-white">
                <MapContainer center={center} zoom={4} scrollWheelZoom={true} style={{ height: "100%", width: "100%" }}>
                  <ChangeView center={center} />
                  <TileLayer attribution='&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors' url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                  {filteredFleet.filter(t => t?.location && CITY_COORDS[t.location]).map((t) => (
                    <Marker key={t.carId} position={CITY_COORDS[t.location]}>
                      <Popup>
                        <div>
                          <h4 className="font-bold">Car {t.carId}, Driver {driversByCarId[t.carId]?.id ?? '-'}</h4>
                          <p>{t.location || "Unknown"}</p>
                        </div>
                      </Popup>
                    </Marker>
                  ))}
                </MapContainer>
              </div>
            </div>
            <div>
              <div className="bg-white rounded-lg shadow p-4 h-96 overflow-y-auto">
                <h3 className="text-lg font-semibold mb-2">Vehicles</h3>
                {filteredFleet.length === 0 ? (
                  <p className="text-gray-500 text-sm">No vehicles found.</p>
                ) : (
                  <ul className="divide-y">
                    {pagedFleet.map((t) => (
                      <li key={t.carId} className="py-2 flex items-center justify-between">
                        <div>
                          <div className="font-medium flex items-center gap-2"><Car size={16} className="text-gray-700" /> Car {t.carId}, Driver {driversByCarId[t.carId]?.id ?? '-'}</div>
                          <div className="text-sm text-gray-600 flex items-center gap-1"><MapPin size={14} className="text-blue-500" /> {t.location || "-"}</div>
                        </div>
                        {t.location && CITY_COORDS[t.location] && (
                          <button
                            className="text-blue-600 text-sm hover:underline"
                            onClick={() => setCenter(CITY_COORDS[t.location])}
                          >
                            Center
                          </button>
                        )}
                      </li>
                    ))}
                  </ul>
                )}
                <div className="flex items-center justify-center gap-2 mt-3">
                  <button className="px-3 py-1 border rounded disabled:opacity-50" onClick={() => setPage(p => Math.max(1, p - 1))} disabled={page === 1}>Prev</button>
                  <span className="text-sm">Page {page} of {totalPages}</span>
                  <button className="px-3 py-1 border rounded disabled:opacity-50" onClick={() => setPage(p => Math.min(totalPages, p + 1))} disabled={page === totalPages}>Next</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="pt-16">
      <div className="p-6 bg-gray-100 min-h-screen">
        <div className="w-full bg-white rounded-lg shadow p-4">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-bold flex items-center gap-2">
              <MapPin size={20} /> Vehicle Location (Car {carId})
            </h2>
            <button onClick={fetchLatest} className="flex items-center gap-2 text-blue-500 hover:text-blue-700">
              <RefreshCcw size={18} /> Refresh
            </button>
          </div>

          <div className="w-full h-96 border rounded overflow-hidden">
            <MapContainer center={center} zoom={latest ? 13 : 4} scrollWheelZoom={true} style={{ height: "100%", width: "100%" }}>
              <ChangeView center={center} />
              <TileLayer attribution='&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors' url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
              {latest && (
                <Marker position={center}>
                  <Popup>
                    <div>
                      <h4 className="font-bold">Car {carId}</h4>
                      <p>{latest.location || "Unknown"}</p>
                    </div>
                  </Popup>
                </Marker>
              )}
            </MapContainer>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MapView;
