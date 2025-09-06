# 📌 User Data & OTP Verification System

A **Java-based authentication and File Hider system** that manages user accounts, stores and retrieves user files, Hide files, UnHide Files and implements **OTP (One-Time Password) authentication** for secure login and verification.  
The project follows the **DAO (Data Access Object) pattern** for clean and maintainable database interactions.

---

 ## 📂 Project Structure

| File | Description |
|------|-------------|
| **DataDAO.java** | Data Access Object for handling file/data operations in the database. |
| **UserDAO.java** | Data Access Object for managing user-related operations (registration, login, retrieval). |
| **data.java** | Entity/model class representing a stored file or user data record. |
| **User.java** | Entity/model class representing a user (id, name, email, password, etc.). |
| **GenerateOTP.java** | Utility class for generating secure random OTPs. |
| **SendOTPService.java** | Service class for sending OTPs (via email/SMS integration). |

---

## ✨ Features

- 🔐 **User Authentication** – Register, login, and manage users securely.  
- 📁 **File/Data Storage** – Upload and retrieve user-related files.  
- 📧 **OTP Verification** – Generate and validate OTPs for secure login.  
- 🛠 **DAO Pattern** – Decoupled and modular database operations.  
- 💾 **Database Integration** – Store user credentials and files in SQL databases.  

---

## 🛠️ Tech Stack

- **Language:** Java (JDK 8+)  
- **Database:** MySQL (or any SQL-based DB)  
- **Libraries/Concepts:** JDBC, DAO Pattern  
- **OTP Handling:** Custom OTP generator + sending service  

---

## 🚀 Getting Started

### 1️⃣ Clone the Repository
```bash
git clone https://github.com/satwik12dev/File-Hider.git
cd File-Hider
```

### 2️⃣ Configure Database
Create a new database (e.g., `userdb`) and required tables:

```sql
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255)
);

CREATE TABLE data (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100),
    filename VARCHAR(255),
    filedata BLOB
);
```

Update your **MyConnection.java** file with database credentials:
```java
private static final String URL = "jdbc:mysql://localhost:3306/userdb";
private static final String USER = "root";
private static final String PASSWORD = "yourpassword";
```

### 3️⃣ Build & Run
Compile the project:
```bash
javac *.java
```

Run your main class:
```bash
java Main
```

---

## 🔐 OTP Workflow

1. **Generate** → `GenerateOTP.java` creates a secure 4-digit OTP.  
2. **Send** → `SendOTPService.java` sends OTP to user’s email/phone.  
3. **Validate** → System validates OTP before granting access.  

---

## 📈 Future Enhancements

- ✅ Password hashing (e.g., BCrypt) for secure credential storage.  
- ✅ Integration with **SMS gateways** (Twilio, AWS SNS).  
- ✅ Support for **JWT-based authentication**.  
- ✅ REST API layer for microservice integration.  
- ✅ Dockerized deployment.  

---

## 👨‍💻 Author

Developed by **[Satwik Saxena]**  
📧 Contact: satwiksaxena41@gmail.com

---

🔒 *Secure authentication, clean design, and scalable OTP verification — built with Java.*  
