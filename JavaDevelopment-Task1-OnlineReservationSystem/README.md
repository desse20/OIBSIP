# Online Reservation System (Java Swing, JDBC, MySQL, Maven)

A complete, secure, enterprise-grade desktop application for train and transport ticket reservations built as part of the **OASIS INFOBYTE Internship (Task 1)**.

---

## 1. Project Overview

The **Online Reservation System** enables passengers to authenticate, browse scheduled trains, book travel tickets with automatic route and date validation, receive an auto-generated unique PNR (Passenger Name Record), and look up or cancel reservations with strict confirmation dialogs.

### Key Highlights
- **Layered Architecture:** Strict separation between Model, Data Access (DAO), Service, and Swing UI. No SQL in UI components.
- **Robust Security:** Passwords hashed using `PBKDF2WithHmacSHA256` with 16-byte cryptographically secure random salts (mitigating rainbow table & timing attacks). Zero hardcoded credentials in source code.
- **ACID Transactions:** Ticket booking and cancellation run in database transactions (`conn.setAutoCommit(false)`, explicit `commit()` and `rollback()`).
- **Collision-Resistant PNR Generation:** Automatic 10-digit PNR generation with algorithmic and database-level uniqueness enforcement.
- **Responsive Swing UI:** Modern desktop layout using standard Swing layout managers (`BorderLayout`, `GridBagLayout`, `BoxLayout`, `FlowLayout`) with clean typography and consistent palette.

---

## 2. Technology Stack

- **Java Version:** Java 17 (OpenJDK 17)
- **Build Tool:** Apache Maven 3.8+
- **GUI Framework:** Java Swing (`javax.swing.*`, `java.awt.*`)
- **Persistence:** JDBC with `com.mysql:mysql-connector-j` (version 9.5.0)
- **Database:** MySQL 8.0+ (InnoDB engine, `utf8mb4`)
- **Testing:** JUnit 5 (`org.junit.jupiter:junit-jupiter` 5.12.2)

---

## 3. Project Architecture

```
JavaDevelopment-Task1-OnlineReservationSystem/
├── pom.xml                                    <- Root Maven aggregator POM
├── db.properties                              <- Configurable database properties
├── db.properties.example                      <- Template configuration file
├── README.md                                  <- Documentation
├── database/
│   ├── schema.sql                             <- MySQL table definitions & constraints
│   └── seed.sql                               <- Seed catalog: sample users & trains
└── online-reservation-system/
    ├── pom.xml                                <- Core application Maven configuration
    ├── database/                              <- SQL scripts copy
    └── src/
        ├── main/
        │   ├── resources/
        │   │   └── db.properties              <- Classpath database configuration
        │   └── java/com/oasisinfobyte/reservation/
        │       ├── Main.java                  <- Application entry point
        │       ├── config/
        │       │   └── DatabaseConnection.java<- Centralized JDBC connection manager
        │       ├── model/
        │       │   ├── User.java              <- User entity
        │       │   ├── Train.java             <- Train entity & route details
        │       │   └── Reservation.java       <- Booking entity
        │       ├── dao/
        │       │   ├── UserDAO.java           <- User authentication & persistence
        │       │   ├── TrainDAO.java          <- Train schedule lookups
        │       │   └── ReservationDAO.java    <- Transactional booking & cancellation
        │       ├── service/
        │       │   ├── ReservationService.java<- Core business rules & validation
        │       │   └── ReservationException.java
        │       ├── util/
        │       │   └── PasswordUtil.java      <- PBKDF2 hashing & verification
        │       └── ui/
        │           ├── UITheme.java           <- Palette, typography & component styling
        │           ├── LoginFrame.java        <- User login window
        │           ├── DashboardFrame.java    <- User navigation dashboard
        │           ├── ReservationFrame.java  <- Ticket booking form
        │           ├── CancellationFrame.java <- PNR lookup & cancellation window
        │           └── ViewReservationsFrame.java <- Admin / User reservations table & search
        └── test/
            └── java/com/oasisinfobyte/reservation/
                ├── ReservationServiceTest.java <- JUnit 5 business logic unit tests
                └── DatabaseIntegrationTest.java<- Live MySQL integration lifecycle tests
```

---

## 4. Database Setup

The application uses a dedicated database named **`online_reservation_system`** (leaving any existing databases like `employees` intact).

### Tables Created:
1. **`users`**:
   - `id`: `BIGINT AUTO_INCREMENT PRIMARY KEY`
   - `username`: `VARCHAR(50) NOT NULL UNIQUE`
   - `password_hash`: `VARCHAR(255) NOT NULL` (PBKDF2 salt:hash)
   - `created_at`: `TIMESTAMP DEFAULT CURRENT_TIMESTAMP`
2. **`trains`**:
   - `id`: `BIGINT AUTO_INCREMENT PRIMARY KEY`
   - `train_number`: `VARCHAR(20) NOT NULL UNIQUE`
   - `train_name`: `VARCHAR(100) NOT NULL`
   - `source_station`: `VARCHAR(100) NOT NULL`
   - `destination_station`: `VARCHAR(100) NOT NULL`
   - `created_at`: `TIMESTAMP DEFAULT CURRENT_TIMESTAMP`
3. **`reservations`**:
   - `id`: `BIGINT AUTO_INCREMENT PRIMARY KEY`
   - `pnr`: `VARCHAR(20) NOT NULL UNIQUE`
   - `passenger_name`: `VARCHAR(100) NOT NULL`
   - `train_id`: `BIGINT NOT NULL` (FK -> `trains(id)`)
   - `class_type`: `VARCHAR(50) NOT NULL`
   - `journey_date`: `DATE NOT NULL`
   - `source_station`: `VARCHAR(100) NOT NULL`
   - `destination_station`: `VARCHAR(100) NOT NULL`
   - `user_id`: `BIGINT NOT NULL` (FK -> `users(id)`)
   - `created_at`: `TIMESTAMP DEFAULT CURRENT_TIMESTAMP`

### Initializing the Database:
Run the SQL scripts using the MySQL command line client:

```bash
# 1. Create database and tables
mysql -u root -p < database/schema.sql

# 2. Insert sample train schedules and default user accounts
mysql -u root -p < database/seed.sql
```

---

## 5. Configuration & Credentials

Database credentials are **never hardcoded in application source code**. The application dynamically checks configuration sources in the following priority order:

1. **System Properties**: `-Ddb.url=...`, `-Ddb.user=...`, `-Ddb.password=...`
2. **Environment Variables**: `DB_URL`, `DB_USER`, `DB_PASSWORD`
3. **External File**: `db.properties` in current working directory
4. **Classpath Resource**: `/db.properties` inside the JAR/resources

### Sample `db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/online_reservation_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=12345
db.driver=com.mysql.cj.jdbc.Driver
```

### Pre-seeded User Accounts:
| Username | Password | Role |
| :--- | :--- | :--- |
| `admin` | `admin123` | Administrator |
| `user1` | `user123` | Passenger User |

---

## 6. How to Build and Run

### Prerequisites
- JDK 17 (`java -version` should show 17.x)
- Maven 3.8+ (`mvn -version`)
- MySQL 8.0+ service running on port 3306

### Compile the Project
```bash
mvn clean compile
```

### Run Unit and Integration Tests
```bash
mvn test
```
*Note: Unit tests use in-memory DAO stubs and pass without requiring a live database. Integration tests automatically execute when MySQL is accessible.*

### Package the Executable JAR
```bash
mvn package
```
Generates `target/online-reservation-system-1.0-SNAPSHOT.jar`.

### Launch the Desktop Application
You can run the application directly through Maven or Java:

```bash
# Via Maven Exec Plugin:
mvn exec:java -pl online-reservation-system -Dexec.mainClass="com.oasisinfobyte.reservation.Main"

# Or via Java CLI with classpath:
java -cp "online-reservation-system/target/online-reservation-system-1.0-SNAPSHOT.jar:online-reservation-system/target/dependency/*:~/.m2/repository/com/mysql/mysql-connector-j/9.5.0/mysql-connector-j-9.5.0.jar" com.oasisinfobyte.reservation.Main
```

---

## 7. Functional Walkthrough

### A. Authentication (`LoginFrame`)
- Input validation: verifies username and password are not empty.
- Authenticates against `users` table via `ReservationService` & `UserDAO`.
- Automatically transitions to `DashboardFrame` on success.

### B. Dashboard (`DashboardFrame`)
- Displays personalized greeting for authenticated user.
- Provides 4 centralized actions:
  - **Book New Ticket** -> Opens `ReservationFrame`
  - **Cancel Ticket** -> Opens `CancellationFrame`
  - **Logout** -> Clears session and returns to login
  - **Exit** -> Prompts confirmation and terminates gracefully

### C. Train Reservation (`ReservationFrame`)
- **Quick Select / Auto-Population:**
  - Selecting a train from the dropdown or entering a train number automatically looks up the database and populates the Train Name, Source Station, and Destination Station.
- **Strict Business Validation:**
  - Passenger name cannot be blank.
  - Train number must be numeric and exist in catalog.
  - Travel class must be selected.
  - Journey date validated (`YYYY-MM-DD`) and cannot be in the past.
  - Source and destination cannot be identical.
  - Verifies that the selected train operates between the specified stations.
- **Transaction & PNR:**
  - Upon submission, generates an algorithmic 10-digit unique PNR.
  - Inserts reservation inside a committed transaction.
  - Displays a comprehensive confirmation dialog with all 8 ticket parameters.

### D. Ticket Cancellation (`CancellationFrame`)
- User inputs PNR and clicks **Fetch Details**.
- Displays full reservation details (passenger, train, route, booking timestamp).
- If PNR does not exist, an error dialog is presented and the cancellation button remains **disabled**.
- Clicking **Confirm Cancellation** triggers an explicit confirmation dialog: *"Are you sure you want to cancel this reservation?"*
- Upon confirmation, deletes the record from the database and displays confirmation. If declined, the record remains intact.

---

## 8. Verification & Testing Summary

All required business logic tests are implemented with **JUnit 5** in `ReservationServiceTest`:
- [x] Empty passenger name rejection (`testEmptyPassengerNameRejected`)
- [x] Invalid / non-numeric train number rejection (`testInvalidTrainNumberRejected`)
- [x] Nonexistent train rejection
- [x] Invalid / past journey date rejection (`testInvalidJourneyDateRejected`)
- [x] Identical source and destination rejection (`testIdenticalSourceAndDestinationRejected`)
- [x] Route mismatch rejection (`testRouteMismatchRejected`)
- [x] Successful valid reservation processing & PNR generation (`testValidReservationProcessed`)
- [x] Nonexistent PNR lookup handling (`testNonexistentPNRHandledCorrectly`)
- [x] Cancellation restricted to existing reservations (`testCancellationOnlySucceedsForExistingReservation`)
- [x] Password hashing verification & authentication (`testAuthentication`)
- [x] Live MySQL database integration tests (`DatabaseIntegrationTest`)
