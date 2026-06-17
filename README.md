# BloodConnect — Donor & Request Matching System

BloodConnect is a web application built to connect patients/hospitals needing urgent blood with compatible, available donors nearby. The system manages donor profiles, availability cooling periods, blood requests, and verification routing, and hides contact information until verified by administrators.

## Technology Stack

- **Frontend**: JSP (JavaServer Pages), Tailwind CSS (via CDN), Vanilla JavaScript
- **Backend**: Java Servlets 4.0.1 (Tomcat 9 compatible)
- **Database**: MySQL, JDBC (Java Database Connectivity)
- **Build & Dependency Tool**: Maven
- **Security**: BCrypt password hashing, PreparedStatements (SQL injection protection), `<c:out>` tags (XSS protection)

---

## Key Features

1. **Role-Based Authentication**:
   - **Donor**: Can manage profile fields (age, gender, city dropdown, pincode, last donation date) and toggle availability status. Can view incoming requests and Accept or Decline matches.
   - **Requester**: Can post urgent blood requests. Can view their request history and matching donors count (contact numbers remain masked until verified by admin).
   - **Admin**: Views all registered users, verifies/approves requests to unmask contact details, and updates request fulfillment states.

2. **Automated Matching Engine**:
   - Checks matching criteria: Same Blood Group, Same City (case-insensitive), Donor availability = `TRUE`, and a minimum 90-day cool-down gap since the last donation date.

---

## Application Screenshots

### 1. Landing Homepage
Beautiful dark-themed landing page built using Tailwind CSS glassmorphic cards and features.
![Landing Homepage](screenshots/homepage.png)

### 2. Registration Page
Enables registering as a Requester or Donor, dynamically prompting location and availability settings.
![Registration Page](screenshots/register.png)

### 3. Donor Dashboard
Allows donors to manage their personal profile, update availability, and review matched request queues.
![Donor Dashboard](screenshots/donor_dashboard.png)

### 4. Compatible Match Results
Displays match results with donor availability status. Phone numbers are fully revealed when the request is verified by an admin.
![Match Results](screenshots/match_results.png)

### 5. Admin Administration Hub
Displays user/request statistics, request queue controls, and status update actions with matched donor response tracking.
![Admin Dashboard](screenshots/admin_dashboard.png)

---

## Local Setup & Run

### Prerequisites
- **Java JDK 17**
- **Apache Maven**
- **MySQL Server**

### Setup Database
Execute the SQL script to set up schema tables, indexes, and seed the admin user:
```bash
mysql -u root -p < schema.sql
```
- Default seed Admin: `admin@bloodconnect.com` / `Admin@123`

### Environment Variables
For security and PaaS compatibility, database credentials are loaded via environment variables. Define these in your environment (or use IDE configurations):
- `MYSQLHOST` (default: `localhost`)
- `MYSQLPORT` (default: `3306`)
- `MYSQLDATABASE` (default: `bloodconnect`)
- `MYSQLUSER` (default: `root`)
- `MYSQLPASSWORD` (default: your password)

### Compile and Package
```bash
mvn clean package
```
This produces `target/bloodconnect.war` which is ready to deploy to any Tomcat 9 server.

---

## Deploying to Railway

BloodConnect is configured with a `Dockerfile` and `entrypoint.sh` for instant PaaS packaging:

1. **Deploy MySQL database service** on Railway.
2. **Add a Web Service** linking to this GitHub repository.
3. Railway automatically detects the `Dockerfile`, builds the WAR file, and deploys it on Tomcat 9.
4. **Environment Variables**: Link the MySQL service to the Web Service. Railway will automatically inject:
   - `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`
   - `PORT` (automatically bound to Tomcat in container via `entrypoint.sh`).
