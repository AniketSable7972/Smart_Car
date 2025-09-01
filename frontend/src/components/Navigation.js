import React, { useState } from "react";
import {
    Car,
    LayoutDashboard,
    Map,
    History,
    AlertTriangle,
    BarChart3,
    Settings as SettingsIcon,
    LogOut,
    Menu,
    X,
} from "lucide-react";

const Navigation = ({ user, currentPage, onPageChange, onLogout }) => {
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

    const driverPages = [
        { id: "driver-dashboard", label: "Dashboard", icon: LayoutDashboard },
        { id: "trips", label: "Trips", icon: History },
    ];

    const adminPages = [
        { id: "admin-dashboard", label: "Dashboard", icon: LayoutDashboard },
        { id: "trip-management", label: "Trips", icon: History },
        { id: "analytics", label: "Analytics", icon: BarChart3 },
        { id: "alerts", label: "Alerts", icon: AlertTriangle },
        { id: "settings", label: "Management", icon: SettingsIcon },
    ];

    const pages = user.role === "DRIVER" ? driverPages : adminPages;

    return (
        <nav className="fixed top-0 left-0 right-0 z-50">
            {/* Glassmorphism Background */}
            <div className="absolute inset-0 bg-white/80 backdrop-blur-xl border-b border-white/20 shadow-lg" />

            <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center justify-between h-16">
                    {/* Branding */}
                    <div className="flex items-center gap-3 group cursor-pointer">
                        <div className="relative">
                            <div className="absolute inset-0 bg-gradient-to-r from-blue-600 to-purple-600 rounded-lg blur-sm opacity-75 group-hover:opacity-100 transition-opacity" />
                            <div className="relative bg-gradient-to-r from-blue-600 to-purple-600 p-2 rounded-lg">
                                <Car className="text-white" size={24} />
                            </div>
                        </div>
                        <div>
                            <span className="font-bold text-xl bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-transparent">
                                Fleet Manager
                            </span>
                            <div className="text-xs text-gray-500 font-medium">Smart Transportation</div>
                        </div>
                    </div>

                    {/* Desktop Navigation */}
                    <div className="hidden md:flex items-center gap-1">
                        {pages.map((page) => {
                            const Icon = page.icon;
                            const isActive = currentPage === page.id;
                            return (
                                <button
                                    key={page.id}
                                    onClick={() => onPageChange(page.id)}
                                    className={`relative flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition-all duration-300 group ${isActive
                                        ? "bg-gradient-to-r from-blue-600 to-purple-600 text-white shadow-lg shadow-blue-500/25"
                                        : "text-gray-600 hover:text-gray-900 hover:bg-white/50"
                                        }`}
                                >
                                    {isActive && (
                                        <div className="absolute inset-0 bg-gradient-to-r from-blue-600 to-purple-600 rounded-xl blur-sm opacity-75" />
                                    )}
                                    <div className="relative flex items-center gap-2">
                                        <Icon size={18} className={`transition-transform duration-300 ${isActive ? 'scale-110' : 'group-hover:scale-110'}`} />
                                        {page.label}
                                    </div>
                                </button>
                            );
                        })}
                    </div>

                    {/* User Info & Logout */}
                    <div className="hidden md:flex items-center gap-4">
                        <div className="text-right">
                            <p className="text-sm font-semibold text-gray-900">Welcome, {user.username}</p>
                            <div className="flex items-center gap-2 mt-1">
                                <div className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold ${user.role === "ADMIN"
                                    ? "bg-gradient-to-r from-purple-500 to-pink-500 text-white shadow-md"
                                    : "bg-gradient-to-r from-blue-500 to-cyan-500 text-white shadow-md"
                                    }`}>
                                    <div className="w-2 h-2 rounded-full bg-white/30 mr-2" />
                                    {user.role}
                                </div>
                            </div>
                        </div>
                        <button
                            onClick={onLogout}
                            className="flex items-center gap-2 px-4 py-2 rounded-xl text-red-600 hover:text-white hover:bg-gradient-to-r hover:from-red-500 hover:to-pink-500 transition-all duration-300 font-medium group"
                        >
                            <LogOut size={18} className="transition-transform group-hover:-translate-x-1" />
                            Logout
                        </button>
                    </div>

                    {/* Mobile Menu Button */}
                    <button
                        onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                        className="md:hidden p-2 rounded-lg bg-white/50 backdrop-blur-sm border border-white/20 hover:bg-white/70 transition-all duration-300"
                    >
                        {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
                    </button>
                </div>
            </div>

            {/* Mobile Navigation */}
            {mobileMenuOpen && (
                <div className="md:hidden absolute top-16 left-0 right-0 bg-white/95 backdrop-blur-xl border-b border-white/20 shadow-xl">
                    <div className="px-4 py-4 space-y-2">
                        {pages.map((page) => {
                            const Icon = page.icon;
                            const isActive = currentPage === page.id;
                            return (
                                <button
                                    key={page.id}
                                    onClick={() => {
                                        onPageChange(page.id);
                                        setMobileMenuOpen(false);
                                    }}
                                    className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all duration-300 ${isActive
                                        ? "bg-gradient-to-r from-blue-600 to-purple-600 text-white shadow-lg"
                                        : "text-gray-600 hover:text-gray-900 hover:bg-gray-50"
                                        }`}
                                >
                                    <Icon size={20} className={isActive ? 'scale-110' : ''} />
                                    {page.label}
                                </button>
                            );
                        })}

                        <div className="border-t border-gray-200 pt-2 mt-4">
                            <button
                                onClick={() => {
                                    onLogout();
                                    setMobileMenuOpen(false);
                                }}
                                className="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-red-600 hover:text-white hover:bg-gradient-to-r hover:from-red-500 hover:to-pink-500 transition-all duration-300 font-medium"
                            >
                                <LogOut size={20} />
                                Logout
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Mobile Bottom Navigation (when menu is closed) */}
            {!mobileMenuOpen && (
                <div className="md:hidden fixed bottom-4 left-4 right-4 bg-white/90 backdrop-blur-xl rounded-2xl border border-white/20 shadow-2xl">
                    <div className="flex items-center justify-around p-2">
                        {pages.slice(0, 4).map((page) => {
                            const Icon = page.icon;
                            const isActive = currentPage === page.id;
                            return (
                                <button
                                    key={page.id}
                                    onClick={() => onPageChange(page.id)}
                                    className={`flex flex-col items-center p-2 rounded-xl transition-all duration-300 ${isActive
                                        ? "bg-gradient-to-r from-blue-600 to-purple-600 text-white shadow-lg"
                                        : "text-gray-600 hover:text-gray-900"
                                        }`}
                                >
                                    <Icon size={20} className={isActive ? 'scale-110' : ''} />
                                    <span className="text-xs mt-1 font-medium">{page.label}</span>
                                </button>
                            );
                        })}
                    </div>
                </div>
            )}
        </nav>
    );
};

export default Navigation;
