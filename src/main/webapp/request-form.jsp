<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>New Blood Request — BloodConnect</title>
    <meta name="description" content="Create a new blood request on BloodConnect.">
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
<body class="font-sans bg-gray-950 text-white min-h-screen flex items-center justify-center relative overflow-y-auto py-12 px-6">

    <!-- Background decorations -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none z-0">
        <div class="absolute -top-40 -right-40 w-96 h-96 bg-blood-600/20 rounded-full blur-3xl animate-pulse-slow"></div>
        <div class="absolute -bottom-40 -left-40 w-96 h-96 bg-blood-800/20 rounded-full blur-3xl animate-pulse-slow" style="animation-delay: 1.5s;"></div>
    </div>

    <div class="relative z-10 w-full max-w-lg">
        
        <!-- Back Button -->
        <a href="${pageContext.request.contextPath}/request/list" id="back-dashboard-btn"
           class="inline-flex items-center gap-2 text-xs font-semibold text-gray-400 hover:text-white mb-6 transition-colors group">
            <span class="transform group-hover:-translate-x-1 transition-transform">←</span> Back to Dashboard
        </a>

        <!-- Header -->
        <div class="text-center mb-8">
            <div class="inline-flex items-center justify-center w-14 h-14 bg-gradient-to-br from-blood-500 to-blood-700 rounded-2xl shadow-lg shadow-blood-500/30 mb-3 animate-float">
                <svg class="w-7 h-7 text-white" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                </svg>
            </div>
            <h1 class="text-2xl font-bold bg-gradient-to-r from-white to-gray-400 bg-clip-text text-transparent">Request Blood</h1>
            <p class="text-gray-400 mt-1 text-sm">Submit patient details to match nearby eligible donors.</p>
        </div>

        <!-- Form Card -->
        <div class="glass rounded-2xl p-8 shadow-2xl">

            <!-- Error message -->
            <c:if test="${not empty error}">
                <div class="bg-red-500/10 border border-red-500/30 text-red-300 px-4 py-3 rounded-xl mb-5 text-sm" id="error-alert">
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/request/new" method="POST" class="space-y-5" id="request-form">

                <!-- Patient Name -->
                <div>
                    <label for="patientName" class="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Patient Name</label>
                    <input type="text" id="patientName" name="patientName"
                           value="<c:out value='${formPatientName}'/>"
                           placeholder="Patient Full Name" required
                           class="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-600 focus:outline-none input-glow transition-all duration-200">
                </div>

                <div class="grid grid-cols-2 gap-4">
                    <!-- Blood Group Needed -->
                    <div>
                        <label for="bloodGroupNeeded" class="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Blood Group</label>
                        <select id="bloodGroupNeeded" name="bloodGroupNeeded" required
                                class="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white focus:outline-none input-glow transition-all duration-200 appearance-none">
                            <option value="" class="bg-gray-900">Select</option>
                            <option value="A+" class="bg-gray-900" <c:if test="${formBloodGroup == 'A+'}">selected</c:if>>A+</option>
                            <option value="A-" class="bg-gray-900" <c:if test="${formBloodGroup == 'A-'}">selected</c:if>>A-</option>
                            <option value="B+" class="bg-gray-900" <c:if test="${formBloodGroup == 'B+'}">selected</c:if>>B+</option>
                            <option value="B-" class="bg-gray-900" <c:if test="${formBloodGroup == 'B-'}">selected</c:if>>B-</option>
                            <option value="AB+" class="bg-gray-900" <c:if test="${formBloodGroup == 'AB+'}">selected</c:if>>AB+</option>
                            <option value="AB-" class="bg-gray-900" <c:if test="${formBloodGroup == 'AB-'}">selected</c:if>>AB-</option>
                            <option value="O+" class="bg-gray-900" <c:if test="${formBloodGroup == 'O+'}">selected</c:if>>O+</option>
                            <option value="O-" class="bg-gray-900" <c:if test="${formBloodGroup == 'O-'}">selected</c:if>>O-</option>
                        </select>
                    </div>

                    <!-- Units Required -->
                    <div>
                        <label for="unitsRequired" class="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Units Required</label>
                        <input type="number" id="unitsRequired" name="unitsRequired"
                               value="<c:out value='${formUnits != null ? formUnits : 1}'/>"
                               min="1" placeholder="e.g. 2" required
                               class="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-600 focus:outline-none input-glow transition-all duration-200">
                    </div>
                </div>

                <!-- Hospital Name -->
                <div>
                    <label for="hospitalName" class="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Hospital Name & Branch</label>
                    <input type="text" id="hospitalName" name="hospitalName"
                           value="<c:out value='${formHospitalName}'/>"
                           placeholder="e.g. Apollo Hospital, Jubilee Hills" required
                           class="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-600 focus:outline-none input-glow transition-all duration-200">
                </div>

                <div class="grid grid-cols-2 gap-4">
                    <!-- City Dropdown -->
                    <div>
                        <label for="city" class="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">City</label>
                        <select id="city" name="city" required
                                class="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white focus:outline-none input-glow transition-all duration-200 appearance-none">
                            <option value="" class="bg-gray-900">Select City</option>
                            <c:forEach var="c" items="${cities}">
                                <option value="${c}" class="bg-gray-900"
                                        <c:if test="${formCity == c}">selected</c:if>><c:out value="${c}"/></option>
                            </c:forEach>
                        </select>
                    </div>

                    <!-- Urgency level -->
                    <div>
                        <label for="urgency" class="block text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Urgency Level</label>
                        <select id="urgency" name="urgency" required
                                class="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white focus:outline-none input-glow transition-all duration-200 appearance-none">
                            <option value="LOW" class="bg-gray-950" <c:if test="${formUrgency == 'LOW'}">selected</c:if>>Low</option>
                            <option value="MEDIUM" class="bg-gray-950" <c:if test="${formUrgency == 'MEDIUM' || empty formUrgency}">selected</c:if>>Medium</option>
                            <option value="HIGH" class="bg-gray-950" <c:if test="${formUrgency == 'HIGH'}">selected</c:if>>High</option>
                            <option value="CRITICAL" class="bg-gray-950" <c:if test="${formUrgency == 'CRITICAL'}">selected</c:if>>Critical 🚨</option>
                        </select>
                    </div>
                </div>

                <!-- Submit Button -->
                <button type="submit" id="submit-request-btn"
                        class="w-full py-3 bg-gradient-to-r from-blood-600 to-blood-700 hover:from-blood-500 hover:to-blood-600 text-white font-semibold rounded-xl shadow-lg shadow-blood-600/30 hover:shadow-blood-500/40 transition-all duration-300 transform hover:scale-[1.02] active:scale-[0.98] mt-2">
                    Submit Blood Request
                </button>
            </form>
        </div>

        <!-- Footer -->
        <p class="text-center text-gray-600 text-xs mt-8">
            &copy; 2025 BloodConnect. Saving lives, one match at a time.
        </p>
    </div>

    <!-- Client-side Validation -->
    <script>
        const form = document.getElementById('request-form');
        const unitsInput = document.getElementById('unitsRequired');

        form.addEventListener('submit', function(e) {
            const units = parseInt(unitsInput.value, 10);
            if (isNaN(units) || units <= 0) {
                e.preventDefault();
                alert('Please enter a valid positive number of blood units.');
            }
        });
    </script>
</body>
</html>
