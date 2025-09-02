// RegisterPage.js
import React, { useState } from "react";
import api from "../api/client";
import { useNavigate } from "react-router-dom";
import { User, Lock, CreditCard, Mail, Phone, Calendar, UserCheck } from "lucide-react";

const InputWrapper = React.memo(({ icon: Icon, children }) => (
  <div className="relative flex items-center border border-gray-300 rounded-lg px-3 py-2 focus-within:ring-2 focus-within:ring-blue-400 transition">
    <Icon className="absolute left-3 text-gray-400" size={18} />
    {children}
  </div>
));

const RegisterPage = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: "",
    password: "",
    name: "",
    age: "",
    gender: "MALE",
    contactNumber: "",
    email: "",
    licenseNumber: "",
  });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);
    try {
      if (!form.username || form.username.length < 3) throw new Error("Username must be at least 3 characters");
      if (!form.password || form.password.length < 6) throw new Error("Password must be at least 6 characters");
      if (!form.name) throw new Error("Name is required");
      if (!form.age || Number(form.age) < 18) throw new Error("Age must be at least 18");
      if (!form.contactNumber) throw new Error("Contact number is required");
      if (!form.email) throw new Error("Email is required");
      if (!form.licenseNumber) throw new Error("License number is required");

      const payload = { ...form, age: Number(form.age), role: "DRIVER" };
      const res = await api.post("/users/register", payload);
      const user = res?.data?.data;
      if (!user?.id) throw new Error(res?.data?.message || "Registration failed");

      setSuccess("Registered successfully! You can now log in.");
      setForm({
        username: "",
        password: "",
        name: "",
        age: "",
        gender: "MALE",
        contactNumber: "",
        email: "",
        licenseNumber: "",
      });
    } catch (err) {
      setError(err?.response?.data?.message || err.message || "Failed to register");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="register-page min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-white to-blue-100 relative px-4 overflow-hidden">
      {/* Floating shapes */}
      <div className="absolute top-10 left-5 w-24 h-24 bg-blue-300 rounded-full opacity-20 animate-bounce-slow"></div>
      <div className="absolute top-1/3 right-10 w-36 h-36 bg-blue-500 rounded-full opacity-20 animate-bounce-slower"></div>
      <div className="absolute bottom-20 left-20 w-20 h-20 bg-blue-200 rounded-full opacity-20 animate-bounce"></div>
      <div className="absolute bottom-10 right-16 w-30 h-30 bg-blue-400 rounded-full opacity-20 animate-bounce-slow"></div>

      {/* Registration Card */}
      <div className="bg-white/80 backdrop-blur-md shadow-2xl rounded-3xl p-10 w-full max-w-lg border border-gray-100 relative z-10">
        <h1 className="text-3xl font-bold text-gray-800 text-center mb-4">Create Your Account</h1>
        <p className="text-gray-500 text-center mb-6">Fill in the details below to get started</p>

        {error && <div className="bg-red-100 text-red-700 p-3 mb-4 rounded-lg shadow-sm">{error}</div>}
        {success && <div className="bg-green-100 text-green-700 p-3 mb-4 rounded-lg shadow-sm">{success}</div>}

        <form onSubmit={onSubmit} className="grid grid-cols-1 gap-4">
          <InputWrapper icon={User}>
            <input
              name="username"
              placeholder="Username"
              className="w-full pl-10 border-none focus:ring-0 focus:outline-none"
              value={form.username}
              onChange={onChange}
              required
            />
          </InputWrapper>

          <InputWrapper icon={Lock}>
            <input
              name="password"
              type="password"
              placeholder="Password"
              className="w-full pl-10 border-none focus:ring-0 focus:outline-none"
              value={form.password}
              onChange={onChange}
              required
            />
          </InputWrapper>

          <InputWrapper icon={UserCheck}>
            <input
              name="name"
              placeholder="Full Name"
              className="w-full pl-10 border-none focus:ring-0 focus:outline-none"
              value={form.name}
              onChange={onChange}
              required
            />
          </InputWrapper>

          <div className="grid grid-cols-2 gap-3">
            <InputWrapper icon={Calendar}>
              <input
                name="age"
                type="number"
                min="18"
                max="100"
                placeholder="Enter your age"
                className="w-full pl-10 border-none focus:ring-0 focus:outline-none"
                value={form.age}
                onChange={onChange}
                required
              />
            </InputWrapper>

            <div className="relative">
              <select
                name="gender"
                className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400 transition"
                value={form.gender}
                onChange={onChange}
              >
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
          </div>

          <InputWrapper icon={Phone}>
            <input
              name="contactNumber"
              placeholder="Contact Number"
              className="w-full pl-10 border-none focus:ring-0 focus:outline-none"
              value={form.contactNumber}
              onChange={onChange}
              required
            />
          </InputWrapper>

          <InputWrapper icon={Mail}>
            <input
              name="email"
              type="email"
              placeholder="Email"
              className="w-full pl-10 border-none focus:ring-0 focus:outline-none"
              value={form.email}
              onChange={onChange}
              required
            />
          </InputWrapper>

          <InputWrapper icon={CreditCard}>
            <input
              name="licenseNumber"
              placeholder="License Number"
              className="w-full pl-10 border-none focus:ring-0 focus:outline-none"
              value={form.licenseNumber}
              onChange={onChange}
              required
            />
          </InputWrapper>

          <button
            type="submit"
            className="w-full bg-blue-500 text-white py-2 rounded-lg hover:bg-blue-600 disabled:opacity-70 transition-colors duration-200 font-medium"
          >
            {loading ? "Registering..." : "Register"}
          </button>
        </form>

        <div className="mt-6 text-center">
          <button
            onClick={() => navigate("/login")}
            className="text-blue-600 hover:underline font-medium text-sm"
          >
            Back to login
          </button>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;


