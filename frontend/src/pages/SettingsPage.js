// SettingsPage.js
import React, { useEffect, useMemo, useState } from "react";
import { Users, Truck, Trash2, Wrench, CheckCircle } from "lucide-react";
import api from "../api/client";

const PAGE_SIZE = 8;

const SettingsPage = () => {
    const [activeTab, setActiveTab] = useState("users");

    // Drivers (User Management)
    const [drivers, setDrivers] = useState([]);
    const [driversLoading, setDriversLoading] = useState(false);
    const [driversError, setDriversError] = useState("");
    const [driversSearch, setDriversSearch] = useState("");
    const [driversPage, setDriversPage] = useState(1);

    // Fleet
    const [cars, setCars] = useState([]);
    const [carsLoading, setCarsLoading] = useState(false);
    const [carsError, setCarsError] = useState("");
    const [carsSearch, setCarsSearch] = useState("");
    const [carsPage, setCarsPage] = useState(1);

    // Add Car modal
    const [showAddCarModal, setShowAddCarModal] = useState(false);
    const [newCarStatus, setNewCarStatus] = useState("");
    const [newCarNumber, setNewCarNumber] = useState("");
    const [newCarModel, setNewCarModel] = useState("");
    const [creatingCar, setCreatingCar] = useState(false);
    const [createError, setCreateError] = useState("");

    // Fleet feedback
    const [fleetMessage, setFleetMessage] = useState("");
    const [fleetMessageType, setFleetMessageType] = useState("");

    useEffect(() => {
        const fetchDrivers = async () => {
            setDriversLoading(true);
            setDriversError("");
            try {
                const res = await api.get("/users/role/DRIVER");
                const { success, data, message } = res.data || {};
                if (!success || !Array.isArray(data)) throw new Error(message || "Failed to load drivers");
                setDrivers(data);
            } catch (err) {
                setDriversError(err?.response?.data?.message || err.message || "Failed to load drivers");
            } finally {
                setDriversLoading(false);
            }
        };
        const fetchCars = async () => {
            setCarsLoading(true);
            setCarsError("");
            try {
                const res = await api.get("/cars");
                const { success, data, message } = res.data || {};
                if (!success || !Array.isArray(data)) throw new Error(message || "Failed to load cars");
                setCars(data);
            } catch (err) {
                setCarsError(err?.response?.data?.message || err.message || "Failed to load cars");
            } finally {
                setCarsLoading(false);
            }
        };
        fetchDrivers();
        fetchCars();
    }, []);

    const refreshFleet = async () => {
        try {
            const carsRes = await api.get("/cars");
            if (carsRes.data?.success && Array.isArray(carsRes.data?.data)) {
                setCars(carsRes.data.data);
            }
        } catch (_e) { }
    };

    useEffect(() => { setDriversPage(1); }, [driversSearch, drivers.length]);
    useEffect(() => { setCarsPage(1); }, [carsSearch, cars.length]);
    useEffect(() => {
        if (!fleetMessage) return;
        const t = setTimeout(() => setFleetMessage(""), 2500);
        return () => clearTimeout(t);
    }, [fleetMessage]);

    const filteredDrivers = useMemo(() => {
        if (!driversSearch) return drivers;
        const q = driversSearch.toLowerCase().trim();
        return drivers.filter((d) =>
            String(d.id ?? "").includes(q) ||
            (d.username || "").toLowerCase().includes(q) ||
            (d.name || "").toLowerCase().includes(q) ||
            (d.contactNumber || "").toLowerCase().includes(q)
        );
    }, [drivers, driversSearch]);
    const totalDriversPages = Math.max(1, Math.ceil(filteredDrivers.length / PAGE_SIZE));
    const currentDriversPage = Math.min(driversPage, totalDriversPages);
    const paginatedDrivers = useMemo(() => {
        const start = (currentDriversPage - 1) * PAGE_SIZE;
        return filteredDrivers.slice(start, start + PAGE_SIZE);
    }, [filteredDrivers, currentDriversPage]);

    const filteredCars = useMemo(() => {
        if (!carsSearch) return cars;
        const q = carsSearch.toLowerCase().trim();
        return cars.filter((c) => String(c.id ?? "").includes(q) || (c.driverName || "").toLowerCase().includes(q) || (c.carNumber || "").toLowerCase().includes(q) || (c.carModel || "").toLowerCase().includes(q));
    }, [cars, carsSearch]);
    const totalCarsPages = Math.max(1, Math.ceil(filteredCars.length / PAGE_SIZE));
    const currentCarsPage = Math.min(carsPage, totalCarsPages);
    const paginatedCars = useMemo(() => {
        const start = (currentCarsPage - 1) * PAGE_SIZE;
        return filteredCars.slice(start, start + PAGE_SIZE);
    }, [filteredCars, currentCarsPage]);

    const formatDateTime = (value) => {
        if (!value) return "";
        try {
            const d = new Date(value);
            if (!isNaN(d.getTime())) return d.toLocaleString();
            if (Array.isArray(value)) {
                const [y, m, day, h = 0, min = 0, s = 0] = value;
                return new Date(y, (m || 1) - 1, day || 1, h, min, s).toLocaleString();
            }
            return String(value);
        } catch {
            return String(value);
        }
    };

    const handleCreateCar = async (e) => {
        e.preventDefault();
        setCreateError("");
        setCreatingCar(true);

        try {
            // Normalize input → remove spaces + force uppercase
            const normalizedCarNumber = newCarNumber.replace(/\s+/g, "").toUpperCase();

            // Check if car number already exists (case-insensitive, ignore spaces)
            const exists = cars.some(
                (c) => c.carNumber && c.carNumber.replace(/\s+/g, "").toUpperCase() === normalizedCarNumber
            );

            if (exists) {
                setCreateError("❌ Car number already exists in the system");
                setCreatingCar(false);
                return;
            }

            const payload = {
                status: newCarStatus || "IDLE",
                carNumber: normalizedCarNumber,
                carModel: newCarModel,
                speed: 0,
                fuelLevel: 100,
                temperature: 40,
                location: "Shivajinagar, Pune"
            };

            const res = await api.post("/cars", payload);
            const { success, data, message } = res.data || {};
            if (!success || !data?.id) throw new Error(message || "Failed to create car");

            // Reset form
            setNewCarStatus("");
            setNewCarNumber("");
            setNewCarModel("");
            setShowAddCarModal(false);

            await refreshFleet();
            setCarsPage(1);

            setFleetMessage("✅ Car added successfully");
            setFleetMessageType("success");
        } catch (err) {
            const msg = err?.response?.data?.message || err.message || "Failed to create car";
            setCreateError(msg);
            setFleetMessage(msg);
            setFleetMessageType("error");
        } finally {
            setCreatingCar(false);
        }
    };


    const handleDeleteCar = async (car) => {
        const ok = window.confirm(`Delete car #${car.id}? This cannot be undone.`);
        if (!ok) return;
        try {
            const res = await api.delete(`/cars/${car.id}`);
            if (!res.data?.success) throw new Error(res.data?.message || "Failed to delete car");
            await refreshFleet();
            setFleetMessage("Car deleted successfully");
            setFleetMessageType("success");
        } catch (err) {
            setFleetMessage(err?.response?.data?.message || err.message || "Failed to delete car");
            setFleetMessageType("error");
        }
    };

    const resetAddCarForm = () => {
        setNewCarStatus("");
        setNewCarNumber("");
        setNewCarModel("");
        setCreateError("");
    };

    return (
        <div className="pt-16">
            <div className="p-6 bg-gray-50 min-h-screen">

                {/* Page Header */}
                <div className="mb-8">
                    <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">
                        System Management
                    </h1>
                    <p className="text-gray-600">
                        Manage users, vehicles, and configure system settings
                    </p>
                </div>

                {/* Tabs */}
                <div className="flex gap-3 mb-8 border-b pb-2">
                    <button
                        onClick={() => setActiveTab("users")}
                        className={`flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium shadow-sm transition
                            ${activeTab === "users"
                                ? "bg-blue-500 text-white shadow-md"
                                : "bg-white text-gray-600 hover:bg-gray-100 border"}`}
                    >
                        <Users size={18} /> User Management
                    </button>

                    <button
                        onClick={() => setActiveTab("fleet")}
                        className={`flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium shadow-sm transition
                            ${activeTab === "fleet"
                                ? "bg-blue-500 text-white shadow-md"
                                : "bg-white text-gray-600 hover:bg-gray-100 border"}`}
                    >
                        <Truck size={18} /> Fleet Management
                    </button>
                </div>

                {/* Tab Content */}
                <div className="bg-white p-6 rounded-2xl shadow-lg">
                    {activeTab === "users" && (
                        <>
                            {/* Search + Header */}
                            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-6">
                                <h2 className="text-xl font-semibold text-gray-800">User Management</h2>
                                <input
                                    type="text"
                                    placeholder="🔍 Search drivers..."
                                    value={driversSearch}
                                    onChange={(e) => setDriversSearch(e.target.value)}
                                    className="border px-4 py-2 rounded-lg w-full md:w-96 shadow-sm focus:ring focus:ring-blue-200"
                                />
                            </div>

                            {driversError && (
                                <div className="mb-4 text-sm text-red-600 bg-red-50 px-3 py-2 rounded-lg">
                                    {driversError}
                                </div>
                            )}

                            {/* Table */}
                            <div className="overflow-x-auto rounded-xl border">
                                <table className="w-full text-sm">
                                    <thead className="bg-gray-100 text-gray-700 text-xs uppercase">
                                        <tr>
                                            <th className="p-3 text-left">ID</th>
                                            <th className="p-3 text-left">Username</th>
                                            <th className="p-3 text-left">Name</th>
                                            <th className="p-3 text-left">Contact</th>
                                            <th className="p-3 text-left">Email</th>
                                            <th className="p-3 text-center">Age</th>
                                            <th className="p-3 text-center">Gender</th>
                                            <th className="p-3 text-left">License</th>
                                            <th className="p-3 text-left">Created</th>
                                            {/* <th className="p-3 text-left">Updated</th> */}
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-gray-200">
                                        {driversLoading ? (
                                            <tr><td colSpan={10} className="p-3 text-center text-gray-500">Loading drivers…</td></tr>
                                        ) : paginatedDrivers.length === 0 ? (
                                            <tr><td colSpan={10} className="p-3 text-center text-gray-500">No drivers found</td></tr>
                                        ) : (
                                            paginatedDrivers.map((u) => (
                                                <tr key={u.id} className="hover:bg-gray-50">
                                                    <td className="p-3 text-left">{u.id}</td>
                                                    <td className="p-3 text-left">{u.username}</td>
                                                    <td className="p-3 text-left">{u.name}</td>
                                                    <td className="p-3 text-left">{u.contactNumber}</td>
                                                    <td className="p-3 text-left">{u.email}</td>
                                                    <td className="p-3 text-center">{u.age}</td>
                                                    <td className="p-3 text-center">{u.gender}</td>
                                                    <td className="p-3 text-left">{u.licenseNumber}</td>
                                                    <td className="p-3 text-left">{formatDateTime(u.creationDate)}</td>
                                                    {/* <td className="p-3 text-left">{formatDateTime(u.lastUpdateOn)}</td> */}
                                                </tr>
                                            ))
                                        )}
                                    </tbody>
                                </table>
                            </div>

                            {/* Pagination */}
                            <div className="flex items-center justify-end gap-2 mt-4">
                                <button
                                    onClick={() => setDriversPage((p) => Math.max(1, p - 1))}
                                    disabled={currentDriversPage <= 1}
                                    className="px-3 py-1 rounded-full border text-sm disabled:opacity-50 hover:bg-gray-100"
                                >
                                    Prev
                                </button>
                                <span className="text-sm text-gray-600">
                                    Page {currentDriversPage} of {totalDriversPages}
                                </span>
                                <button
                                    onClick={() => setDriversPage((p) => Math.min(totalDriversPages, p + 1))}
                                    disabled={currentDriversPage >= totalDriversPages}
                                    className="px-3 py-1 rounded-full border text-sm disabled:opacity-50 hover:bg-gray-100"
                                >
                                    Next
                                </button>
                            </div>
                        </>
                    )}

                    {activeTab === "fleet" && (
                        <>
                            {/* Fleet Header */}
                            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-6">
                                <h2 className="text-xl font-semibold text-gray-800">Fleet Management</h2>
                                <div className="flex flex-col md:flex-row gap-2 md:items-center">
                                    <input
                                        type="text"
                                        placeholder="🔍 Search cars..."
                                        value={carsSearch}
                                        onChange={(e) => setCarsSearch(e.target.value)}
                                        className="border px-4 py-2 rounded-lg w-full md:w-80 shadow-sm focus:ring focus:ring-blue-200"
                                    />
                                    <button
                                        onClick={() => {
                                            resetAddCarForm();
                                            setShowAddCarModal(true);
                                        }}
                                        className="bg-blue-500 text-white px-4 py-2 rounded-full shadow hover:bg-blue-600"
                                    >
                                        + Add Car
                                    </button>
                                </div>
                            </div>

                            {/* Fleet Messages */}
                            {fleetMessage && (
                                <div
                                    className={`mb-4 px-4 py-2 rounded-lg text-sm shadow 
                                        ${fleetMessageType === "success"
                                            ? "bg-green-100 text-green-700"
                                            : "bg-red-100 text-red-700"}`}
                                >
                                    {fleetMessage}
                                </div>
                            )}

                            {/* Add Car Modal */}
                            {showAddCarModal && (
                                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
                                    <div className="bg-white rounded-lg shadow-lg w-full max-w-md p-4">
                                        <div className="flex items-center justify-between mb-2">
                                            <h3 className="text-lg font-semibold">Add New Car</h3>
                                            <button onClick={() => { setShowAddCarModal(false); resetAddCarForm(); }} className="px-2 py-1 text-gray-500 hover:text-gray-700">✕</button>
                                        </div>
                                        <form onSubmit={handleCreateCar} className="space-y-3">
                                            <div>
                                                <label className="block text-sm mb-1">Car Number</label>
                                                <input
                                                    type="text"
                                                    value={newCarNumber}
                                                    onChange={(e) => setNewCarNumber(e.target.value.toUpperCase())}
                                                    className="border px-3 py-2 rounded w-full"
                                                    placeholder="e.g., MH12AB1234"
                                                    required
                                                />
                                            </div>
                                            <div>
                                                <label className="block text-sm mb-1">Car Model</label>
                                                <input type="text" value={newCarModel} onChange={(e) => setNewCarModel(e.target.value)} className="border px-3 py-2 rounded w-full" placeholder="e.g., Swift DZire" required />
                                            </div>
                                            <div>
                                                <label className="block text-sm mb-1">Status</label>
                                                <select value={newCarStatus} onChange={(e) => setNewCarStatus(e.target.value)} className="border px-3 py-2 rounded w-full" required>
                                                    <option value="IDLE">IDLE</option>
                                                </select>
                                            </div>
                                            {createError && <div className="text-red-600 text-sm">{createError}</div>}
                                            <div className="flex justify-end gap-2 pt-2">
                                                <button type="button" onClick={() => { setShowAddCarModal(false); resetAddCarForm(); }} className="px-4 py-2 border rounded hover:bg-gray-50">Cancel</button>
                                                <button type="submit" disabled={creatingCar} className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 disabled:opacity-60">{creatingCar ? "Adding…" : "Save"}</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            )}

                            {/* Cars Table */}
                            <div className="overflow-x-auto rounded-xl border">
                                <table className="w-full text-sm">
                                    <thead className="bg-gray-100 text-gray-700 text-xs uppercase">
                                        <tr>
                                            <th className="p-3 text-left">ID</th>
                                            <th className="p-3 text-center">Status</th>
                                            <th className="p-3 text-left">Car Number</th>
                                            <th className="p-3 text-left">Model</th>
                                            {/* <th className="p-3 text-center">Speed</th>
                                            <th className="p-3 text-center">Fuel</th>
                                            <th className="p-3 text-center">Temp</th>
                                            <th className="p-3 text-left">Location</th> */}
                                            <th className="p-3 text-left">Driver</th>
                                            <th className="p-3 text-center">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-gray-200">
                                        {carsLoading ? (
                                            <tr><td colSpan={10} className="p-3 text-center text-gray-500">Loading cars…</td></tr>
                                        ) : paginatedCars.length === 0 ? (
                                            <tr><td colSpan={10} className="p-3 text-center text-gray-500">No cars found</td></tr>
                                        ) : (
                                            paginatedCars.map((c) => (
                                                <tr key={c.id} className="hover:bg-gray-50">
                                                    <td className="p-3 text-left">{c.id}</td>
                                                    <td className="p-3 text-center">{c.status}</td>
                                                    <td className="p-3 text-left">{c.carNumber || "-"}</td>
                                                    <td className="p-3 text-left">{c.carModel || "-"}</td>
                                                    {/* <td className="p-3 text-center">{c.speed}</td>
                                                    <td className="p-3 text-center">{c.fuelLevel}</td>
                                                    <td className="p-3 text-center">{c.temperature}</td>
                                                    <td className="p-3 text-left">{c.location}</td> */}
                                                    <td className="p-3 text-left">{c.driverName || "-"}</td>
                                                    <td className="p-3 text-center">
                                                        <div className="flex gap-2 justify-center">
                                                            {/* Delete Button */}
                                                            <button
                                                                onClick={() => handleDeleteCar(c)}
                                                                className="flex items-center gap-1 px-3 py-1.5 rounded-full bg-red-500 text-white text-xs font-medium shadow-sm hover:bg-red-600 hover:scale-105 transition"
                                                            >
                                                                <Trash2 size={14} /> Delete
                                                            </button>

                                                            {/* Set Maintenance */}
                                                            <button
                                                                onClick={async () => {
                                                                    await api.put(`/cars/${c.id}`, { ...c, status: "UNDER MAINTAINANCE" });
                                                                    await refreshFleet();
                                                                }}
                                                                className="flex items-center gap-1 px-3 py-1.5 rounded-full bg-yellow-500 text-white text-xs font-medium shadow-sm hover:bg-yellow-600 hover:scale-105 transition"
                                                            >
                                                                <Wrench size={14} /> Set Maintenance
                                                            </button>

                                                            {/* End Maintenance */}
                                                            <button
                                                                onClick={async () => {
                                                                    await api.put(`/cars/${c.id}`, { ...c, status: "IDLE" });
                                                                    await refreshFleet();
                                                                }}
                                                                className="flex items-center gap-1 px-3 py-1.5 rounded-full bg-green-500 text-white text-xs font-medium shadow-sm hover:bg-green-600 hover:scale-105 transition"
                                                            >
                                                                <CheckCircle size={14} /> End Maintenance
                                                            </button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            ))
                                        )}
                                    </tbody>
                                </table>
                            </div>

                            {/* Pagination */}
                            <div className="flex items-center justify-end gap-2 mt-4">
                                <button
                                    onClick={() => setCarsPage((p) => Math.max(1, p - 1))}
                                    disabled={currentCarsPage <= 1}
                                    className="px-3 py-1 rounded-full border text-sm disabled:opacity-50 hover:bg-gray-100"
                                >
                                    Prev
                                </button>
                                <span className="text-sm text-gray-600">
                                    Page {currentCarsPage} of {totalCarsPages}
                                </span>
                                <button
                                    onClick={() => setCarsPage((p) => Math.min(totalCarsPages, p + 1))}
                                    disabled={currentCarsPage >= totalCarsPages}
                                    className="px-3 py-1 rounded-full border text-sm disabled:opacity-50 hover:bg-gray-100"
                                >
                                    Next
                                </button>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default SettingsPage;


