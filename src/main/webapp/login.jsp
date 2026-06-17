<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login — BloodConnect</title>
    <meta name="description" content="Login to BloodConnect - Donor and Request Matching System">
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    fontFamily: { sans: ['Inter', 'sans-serif'] },
                    colors: {
                        blood: {
                            50: '#fef2f2', 100: '#fee2e2', 200: '#fecaca',
                            300: '#fca5a5', 400: '#f87171', 500: '#ef4444',
                            600: '#dc2626', 700: '#b91c1c', 800: '#991b1b',
                            900: '#7f1d1d', 950: '#450a0a'
                        }
                    }
                }
            }
        }
    </script>
    <style>
        @keyframes pulse-slow { 0%, 100% { opacity: 1; } 50% { opacity: 0.7; } }
        @keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-10px); } }
        .animate-pulse-slow { animation: pulse-slow 3s ease-in-out infinite; }
        .animate-float { animation: float 6s ease-in-out infinite; }
        .glass { background: rgba(255, 255, 255, 0.05); backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.1); }
        .input-glow:focus { box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.3); }
    </style>
</head>
<body class="font-sans bg-gray-950 text-white min-h-screen flex items-center justify-center relative overflow-hidden">

    <!-- Background decorations -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
        <div class="absolute -top-40 -right-40 w-96 h-96 bg-blood-600/20 rounded-full blur-3xl animate-pulse-slow"></div>
        <div class="absolute -bottom-40 -left-40 w-96 h-96 bg-blood-800/20 rounded-full blur-3xl animate-pulse-slow" style="animation-delay: 1.5s;"></div>
        <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-blood-700/5 rounded-full blur-3xl"></div>
    </div>

    <div class="relative z-10 w-full max-w-md px-6">
        <!-- Logo / Brand -->
        <div class="text-center mb-8">
            <div class="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-blood-500 to-blood-700 rounded-2xl shadow-lg shadow-blood-500/30 mb-4 animate-float">
                <svg class="w-8 h-8 text-white" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                </svg>
            </div>
            <h1 class="text-3xl font-bold bg-gradient-to-r from-white to-gray-400 bg-clip-text text-transparent">BloodConnect</h1>
            <p class="text-gray-400 mt-2 text-sm">Donor & Request Matching System</p>
        </div>

        <!-- Login Card -->
        <div class="glass rounded-2xl p-8 shadow-2xl">
            <h2 class="text-xl font-semibold text-center mb-6">Welcome Back</h2>

            <!-- Error message -->
            <c:if test="${not empty error}">
                <div class="bg-red-500/10 border border-red-500/30 text-red-300 px-4 py-3 rounded-xl mb-4 text-sm" id="error-alert">
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <!-- Success message (after registration) -->
            <c:if test="${not empty success}">
                <div class="bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 px-4 py-3 rounded-xl mb-4 text-sm" id="success-alert">
                    <c:out value="${success}"/>
                </div>
            </c:if>

            <!-- Unauthorized message -->
            <c:if test="${param.error == 'unauthorized'}">
                <div class="bg-amber-500/10 border border-amber-500/30 text-amber-300 px-4 py-3 rounded-xl mb-4 text-sm" id="unauth-alert">
                    You don't have permission to access that page.
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/login" method="POST" class="space-y-5" id="login-form">
                <!-- Email -->
                <div>
                    <label for="email" class="block text-sm font-medium text-gray-300 mb-1.5">Email</label>
                    <input type="email" id="email" name="email"
                           value="<c:out value='${formEmail}'/>"
                           placeholder="you@example.com"
                           required
                           class="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:outline-none input-glow transition-all duration-200">
                </div>

                <!-- Password -->
                <div>
                    <label for="password" class="block text-sm font-medium text-gray-300 mb-1.5">Password</label>
                    <input type="password" id="password" name="password"
                           placeholder="••••••••"
                           required
                           class="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:outline-none input-glow transition-all duration-200">
                </div>

                <!-- Submit -->
                <button type="submit" id="login-btn"
                        class="w-full py-3 bg-gradient-to-r from-blood-600 to-blood-700 hover:from-blood-500 hover:to-blood-600 text-white font-semibold rounded-xl shadow-lg shadow-blood-600/30 hover:shadow-blood-500/40 transition-all duration-300 transform hover:scale-[1.02] active:scale-[0.98]">
                    Sign In
                </button>
            </form>

            <!-- Register link -->
            <p class="text-center text-gray-400 text-sm mt-6">
                Don't have an account?
                <a href="${pageContext.request.contextPath}/register" class="text-blood-400 hover:text-blood-300 font-medium transition-colors" id="register-link">
                    Register here
                </a>
            </p>
        </div>

        <!-- Footer -->
        <p class="text-center text-gray-600 text-xs mt-8">
            &copy; 2025 BloodConnect. Saving lives, one match at a time.
        </p>
    </div>

</body>
</html>
