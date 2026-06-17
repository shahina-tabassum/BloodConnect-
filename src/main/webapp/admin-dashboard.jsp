<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard — BloodConnect</title>
    <meta name="description" content="BloodConnect Administration Dashboard for request verification and user monitoring.">
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
<body class="font-sans bg-gray-950 text-white min-h-screen relative overflow-x-hidden">

    <!-- Background decorations -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none z-0">
        <div class="absolute top-10 right-10 w-[500px] h-[500px] bg-blood-600/10 rounded-full blur-3xl animate-pulse-slow"></div>
        <div class="absolute bottom-10 left-10 w-[500px] h-[500px] bg-blood-800/10 rounded-full blur-3xl animate-pulse-slow" style="animation-delay: 1.5s;"></div>
    </div>

    <!-- Header / Navbar -->
    <nav class="relative z-10 glass border-b border-white/10 py-4 px-6 md:px-12 flex justify-between items-center">
        <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-gradient-to-br from-blood-500 to-blood-700 rounded-xl flex items-center justify-center shadow-lg shadow-blood-500/20">
                <svg class="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                </svg>
            </div>
            <div>
                <span class="font-bold text-lg tracking-wide bg-gradient-to-r from-white to-gray-300 bg-clip-text text-transparent">BloodConnect</span>
                <span class="text-[10px] text-blood-400 font-semibold uppercase tracking-wider block leading-none">Administration Hub</span>
            </div>
        </div>
        <div class="flex items-center gap-4">
            <div class="hidden sm:flex flex-col text-right">
                <span class="text-sm font-semibold text-gray-200" id="user-display-name">
                    <c:out value="${sessionScope.userName}"/>
                </span>
                <span class="text-xs text-blood-400 font-medium">Administrator</span>
            </div>
            <a href="${pageContext.request.contextPath}/logout" id="logout-btn"
               class="px-4 py-2 bg-white/5 border border-white/10 hover:bg-blood-600/10 hover:border-blood-500/50 rounded-xl text-xs font-semibold tracking-wide transition-all duration-200">
                Sign Out
            </a>
        </div>
    </nav>

    <!-- Main Content -->
    <main class="relative z-10 max-w-7xl mx-auto px-4 py-8 md:py-12">

        <!-- Flash messages -->
        <c:if test="${not empty success}">
            <div class="bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 px-5 py-4 rounded-2xl mb-8 flex items-center justify-between text-sm shadow-xl" id="success-alert">
                <div class="flex items-center gap-3">
                    <span class="text-xl">✓</span>
                    <span><c:out value="${success}"/></span>
                </div>
                <button onclick="this.parentElement.remove()" class="text-emerald-400 hover:text-white transition-colors">✕</button>
            </div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="bg-red-500/10 border border-red-500/30 text-red-300 px-5 py-4 rounded-2xl mb-8 flex items-center justify-between text-sm shadow-xl" id="error-alert">
                <div class="flex items-center gap-3">
                    <span class="text-xl">⚠</span>
                    <span><c:out value="${error}"/></span>
                </div>
                <button onclick="this.parentElement.remove()" class="text-red-400 hover:text-white transition-colors">✕</button>
            </div>
        </c:if>

        <!-- Stats Overview Cards -->
        <div class="grid grid-cols-1 sm:grid-cols-3 gap-6 mb-8">
            <!-- Count Users -->
            <div class="glass rounded-2xl p-6 shadow-xl border border-white/5 flex items-center justify-between">
                <div>
                    <span class="text-xs text-gray-500 uppercase tracking-wider font-semibold">Total Accounts</span>
                    <h3 class="text-3xl font-extrabold text-white mt-1" id="total-users-stat">
                        <c:out value="${users.size()}"/>
                    </h3>
                </div>
                <div class="text-3xl p-3 bg-white/5 rounded-2xl">👥</div>
            </div>

            <!-- Count Requests -->
            <div class="glass rounded-2xl p-6 shadow-xl border border-white/5 flex items-center justify-between">
                <div>
                    <span class="text-xs text-gray-500 uppercase tracking-wider font-semibold">Active Blood Requests</span>
                    <h3 class="text-3xl font-extrabold text-white mt-1" id="total-requests-stat">
                        <c:out value="${requests.size()}"/>
                    </h3>
                </div>
                <div class="text-3xl p-3 bg-white/5 rounded-2xl">🏥</div>
            </div>

            <!-- Count Awaiting Verification -->
            <c:set var="pendingCount" value="0"/>
            <c:forEach var="req" items="${requests}">
                <c:if test="${!req.verified}">
                    <c:set var="pendingCount" value="${pendingCount + 1}"/>
                </c:if>
            </c:forEach>
            <div class="glass rounded-2xl p-6 shadow-xl border border-white/5 flex items-center justify-between">
                <div>
                    <span class="text-xs text-gray-500 uppercase tracking-wider font-semibold">Awaiting Verification</span>
                    <h3 class="text-3xl font-extrabold text-blood-400 mt-1" id="pending-verifications-stat">
                        <c:out value="${pendingCount}"/>
                    </h3>
                </div>
                <div class="text-3xl p-3 bg-blood-600/10 rounded-2xl text-blood-400">🛡️</div>
            </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
            
            <!-- Left 2 Columns: Requests Verification Queue & Status Update -->
            <div class="lg:col-span-2 space-y-6">
                <div class="glass rounded-2xl p-6 shadow-2xl border border-white/5">
                    <h3 class="text-lg font-bold bg-gradient-to-r from-white to-gray-300 bg-clip-text text-transparent mb-6 flex items-center gap-2">
                        <span>📋</span> Blood Request Control Queue
                    </h3>

                    <c:choose>
                        <c:when test="${empty requests}">
                            <div class="text-center py-16 border border-dashed border-white/10 rounded-2xl bg-white/5">
                                <div class="text-4xl mb-3">📭</div>
                                <h4 class="font-bold text-gray-400 text-sm">No Blood Requests Registered</h4>
                                <p class="text-gray-500 text-xs mt-1">When users post requests, they will show up here for admin review.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="space-y-4">
                                <c:forEach var="req" items="${requests}">
                                    <div class="glass border border-white/5 rounded-2xl p-5 hover:border-white/10 transition-all duration-200">
                                        <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
                                            
                                            <!-- Request Description -->
                                            <div class="space-y-2">
                                                <div class="flex flex-wrap items-center gap-2">
                                                    <span class="text-xs font-extrabold uppercase px-2 py-0.5 rounded bg-blood-500/20 text-blood-400 border border-blood-500/30">
                                                        Patient: <c:out value="${req.patientName}"/>
                                                    </span>
                                                    
                                                    <!-- Urgency Badge -->
                                                    <c:choose>
                                                        <c:when test="${req.urgency == 'CRITICAL'}">
                                                            <span class="text-[9px] font-bold uppercase px-2 py-0.5 rounded bg-red-500/20 text-red-400 border border-red-500/30 animate-pulse">Critical</span>
                                                        </c:when>
                                                        <c:when test="${req.urgency == 'HIGH'}">
                                                            <span class="text-[9px] font-bold uppercase px-2 py-0.5 rounded bg-orange-500/20 text-orange-400 border border-orange-500/30">High</span>
                                                        </c:when>
                                                        <c:when test="${req.urgency == 'MEDIUM'}">
                                                            <span class="text-[9px] font-bold uppercase px-2 py-0.5 rounded bg-yellow-500/20 text-yellow-400 border border-yellow-500/30">Medium</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-[9px] font-bold uppercase px-2 py-0.5 rounded bg-blue-500/20 text-blue-400 border border-blue-500/30">Low</span>
                                                        </c:otherwise>
                                                    </c:choose>

                                                    <!-- Verification Status -->
                                                    <c:choose>
                                                        <c:when test="${req.verified}">
                                                            <span class="text-[9px] font-semibold px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">Verified</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-[9px] font-semibold px-2 py-0.5 rounded bg-amber-500/10 text-amber-400 border border-amber-500/20 animate-pulse">Awaiting Verification</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>

                                                <div class="grid grid-cols-2 gap-x-6 gap-y-1 text-xs text-gray-400">
                                                    <div>🩸 Group Needed: <span class="font-medium text-white"><c:out value="${req.bloodGroupNeeded}"/></span></div>
                                                    <div>💧 Units: <span class="font-medium text-white"><c:out value="${req.unitsRequired}"/></span></div>
                                                    <div class="col-span-2">🏥 Hospital: <span class="font-medium text-white"><c:out value="${req.hospitalName}"/> (<c:out value="${req.city}"/>)</span></div>
                                                    <div class="col-span-2">👤 Requester: <span class="font-medium text-white"><c:out value="${req.requesterName}"/></span></div>
                                                </div>
                                            </div>

                                            <!-- Admin Controls -->
                                            <div class="flex flex-wrap items-center gap-3 self-start md:self-center">
                                                
                                                <!-- Verify Button -->
                                                <c:if test="${!req.verified}">
                                                    <form action="${pageContext.request.contextPath}/admin/verify" method="POST" class="inline">
                                                        <input type="hidden" name="action" value="verify">
                                                        <input type="hidden" name="requestId" value="${req.requestId}">
                                                        <button type="submit" id="verify-btn-${req.requestId}"
                                                                class="px-3.5 py-1.5 bg-gradient-to-r from-emerald-600 to-emerald-700 hover:from-emerald-500 hover:to-emerald-600 text-white font-semibold text-xs rounded-xl shadow transition-all duration-200">
                                                            Approve
                                                        </button>
                                                    </form>
                                                </c:if>

                                                <!-- Status Update Form -->
                                                <form action="${pageContext.request.contextPath}/admin/verify" method="POST" class="inline-flex items-center gap-2">
                                                    <input type="hidden" name="action" value="updateStatus">
                                                    <input type="hidden" name="requestId" value="${req.requestId}">
                                                    <select name="status" id="status-select-${req.requestId}"
                                                            class="bg-white/5 border border-white/10 rounded-xl text-white text-xs px-2 py-1.5 focus:outline-none focus:border-blood-500">
                                                        <option value="OPEN" class="bg-gray-900" <c:if test="${req.status == 'OPEN'}">selected</c:if>>Open</option>
                                                        <option value="MATCHED" class="bg-gray-900" <c:if test="${req.status == 'MATCHED'}">selected</c:if>>Matched</option>
                                                        <option value="FULFILLED" class="bg-gray-900" <c:if test="${req.status == 'FULFILLED'}">selected</c:if>>Fulfilled</option>
                                                        <option value="CLOSED" class="bg-gray-900" <c:if test="${req.status == 'CLOSED'}">selected</c:if>>Closed</option>
                                                    </select>
                                                    <button type="submit" id="update-status-btn-${req.requestId}"
                                                            class="px-2.5 py-1.5 bg-white/5 hover:bg-white/10 border border-white/10 text-xs font-semibold rounded-xl transition-all duration-200">
                                                        Update
                                                    </button>
                                                </form>

                                            </div>
                                        </div>
                                        
                                        <!-- Matched Donors Section -->
                                        <c:if test="${not empty req.matches}">
                                            <div class="mt-4 pt-4 border-t border-white/5 space-y-2">
                                                <h4 class="text-[10px] font-bold text-gray-500 uppercase tracking-wider">Matched Donors Status</h4>
                                                <div class="flex flex-wrap gap-2">
                                                    <c:forEach var="match" items="${req.matches}">
                                                        <div class="px-3 py-1.5 bg-white/5 border border-white/5 rounded-xl text-xs flex items-center gap-2">
                                                            <span class="text-gray-300 font-medium"><c:out value="${match.donorName}"/></span>
                                                            <c:choose>
                                                                <c:when test="${match.status == 'ACCEPTED'}">
                                                                    <span class="px-1.5 py-0.5 rounded text-[8px] font-extrabold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 uppercase tracking-wider">Accepted Match</span>
                                                                </c:when>
                                                                <c:when test="${match.status == 'DECLINED'}">
                                                                    <span class="px-1.5 py-0.5 rounded text-[8px] font-extrabold bg-red-500/20 text-red-400 border border-red-500/30 uppercase tracking-wider">Declined Match</span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="px-1.5 py-0.5 rounded text-[8px] font-extrabold bg-white/5 text-gray-400 border border-white/10 uppercase tracking-wider">Pending Response</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </div>
                                                    </c:forEach>
                                                </div>
                                            </div>
                                        </c:if>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- Right Column: User & Directory Details -->
            <div class="lg:col-span-1">
                <div class="glass rounded-2xl p-6 shadow-2xl border border-white/5">
                    <h3 class="text-base font-bold bg-gradient-to-r from-white to-gray-300 bg-clip-text text-transparent mb-5 flex items-center gap-2">
                        <span>👥</span> Registered Accounts
                    </h3>

                    <div class="space-y-4 max-h-[600px] overflow-y-auto pr-1">
                        <c:forEach var="u" items="${users}">
                            <div class="p-4 bg-white/5 border border-white/5 rounded-xl text-xs space-y-2 hover:border-white/10 transition-colors">
                                <div class="flex items-center justify-between gap-2">
                                    <span class="font-bold text-gray-200 truncate block max-w-[130px]"><c:out value="${u.fullName}"/></span>
                                    
                                    <c:choose>
                                        <c:when test="${u.role == 'ADMIN'}">
                                            <span class="px-1.5 py-0.5 rounded text-[8px] font-bold bg-purple-500/20 text-purple-400 border border-purple-500/30 uppercase">Admin</span>
                                        </c:when>
                                        <c:when test="${u.role == 'DONOR'}">
                                            <span class="px-1.5 py-0.5 rounded text-[8px] font-bold bg-red-500/20 text-red-400 border border-red-500/30 uppercase">Donor</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="px-1.5 py-0.5 rounded text-[8px] font-bold bg-blue-500/20 text-blue-400 border border-blue-500/30 uppercase">Requester</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="text-gray-400 space-y-1">
                                    <div class="truncate block">✉ <c:out value="${u.email}"/></div>
                                    <div>📞 <c:out value="${u.phone}"/></div>
                                    <div class="text-[10px] text-gray-500">Joined: <c:out value="${u.createdAt}"/></div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </div>

        </div>
    </main>

    <!-- Footer -->
    <footer class="relative z-10 glass border-t border-white/5 py-8 mt-16 text-center text-gray-500 text-xs">
        <p>&copy; 2026 BloodConnect. All rights reserved.</p>
        <p class="mt-1 text-gray-600">Saving lives, one match at a time.</p>
    </footer>

</body>
</html>
