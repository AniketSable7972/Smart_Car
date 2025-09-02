// LoginPage.js
import React, { useState } from "react";
import { Car, AlertCircle, User, Lock } from "lucide-react";
import api from "../api/client";
import { Link } from "react-router-dom";

// Memoized input component to prevent re-renders
const InputWithIcon = React.memo(({ icon: Icon, type = "text", value, onChange, placeholder }) => (
    <div className="relative">
        <Icon className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
        <input
            type={type}
            value={value}
            onChange={onChange}
            placeholder={placeholder}
            className="w-full border border-gray-300 rounded-lg px-10 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-blue-400 transition"
            required
        />
    </div>
));

const LoginPage = ({ onLogin }) => {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setIsLoading(true);
        try {
            const res = await api.post("/users/login", { username, password });
            const { success, data, message } = res.data || {};
            if (!success || !data) throw new Error(message || "Login failed");

            const { token, userId, username: uName, role, name } = data;
            localStorage.setItem("token", token || "");
            localStorage.setItem("userId", String(userId));
            localStorage.setItem("username", uName);
            localStorage.setItem("role", role);
            onLogin({ id: userId, username: uName, role, name });
        } catch (err) {
            setError(err?.response?.data?.message || err.message || "Invalid username or password");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="login-page min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-white to-blue-100 relative overflow-hidden px-4">
            {/* Floating shapes */}
            <div className="absolute top-10 left-5 w-24 h-24 bg-blue-300 rounded-full opacity-20 animate-bounce-slow"></div>
            <div className="absolute top-1/3 right-10 w-36 h-36 bg-blue-500 rounded-full opacity-20 animate-bounce-slower"></div>
            <div className="absolute bottom-20 left-20 w-20 h-20 bg-blue-200 rounded-full opacity-20 animate-bounce"></div>
            <div className="absolute bottom-10 right-16 w-30 h-30 bg-blue-400 rounded-full opacity-20 animate-bounce-slow"></div>

            {/* Login Card */}
            <div className="bg-white/80 backdrop-blur-md shadow-2xl rounded-3xl p-10 w-full max-w-md border border-gray-100 relative z-10">
                {/* Header */}
                <div className="flex flex-col items-center mb-6">
                    <div className="bg-blue-500 p-3 rounded-full mb-4 shadow-lg">
                        <Car className="text-white" size={32} />
                    </div>
                    <h1 className="text-3xl font-bold text-gray-800">Fleet Management</h1>
                    <p className="text-gray-500 text-sm mt-1">Sign in to access your dashboard</p>
                </div>

                {/* Error Alert */}
                {error && (
                    <div className="flex items-center bg-red-100 text-red-600 p-3 mb-4 rounded-lg shadow-sm">
                        <AlertCircle className="mr-2" size={18} />
                        <span className="text-sm">{error}</span>
                    </div>
                )}

                {/* Login Form */}
                <form onSubmit={handleSubmit} className="space-y-5">
                    <InputWithIcon
                        icon={User}
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        placeholder="Username"
                    />
                    <InputWithIcon
                        icon={Lock}
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="Password"
                    />

                    <button
                        type="submit"
                        className="w-full bg-blue-500 text-white py-2 rounded-lg hover:bg-blue-600 disabled:opacity-70 transition-colors duration-200 font-medium"
                        disabled={isLoading}
                    >
                        {isLoading ? "Signing in..." : "Sign In"}
                    </button>
                </form>

                {/* Quick Links */}
                <div className="flex justify-between items-center mt-6 text-sm">
                    <span className="text-gray-600">New driver?</span>
                    <Link to="/register" className="text-blue-600 hover:underline font-medium">
                        Register here
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
