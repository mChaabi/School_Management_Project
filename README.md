

# 🎓 EduManage – School Management System

**A full-stack school administration platform with role-based security, PDF/Excel reports and a built-in AI assistant that answers questions about your real data.**

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-Auth_&_Roles-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-UI-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![Gemini](https://img.shields.io/badge/Google_Gemini-AI-8E75B2?style=for-the-badge&logo=googlegemini&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

<img src="docs/screenshots/03-dashboard.png" alt="EduManage dashboard" width="900">

</div>

---

## 📑 Table of contents

- [About the project](#-about-the-project)
- [Key features](#-key-features)
- [Screenshots](#-screenshots)
- [Tech stack](#-tech-stack)
- [Architecture](#-architecture)
- [AI features](#-ai-features)
- [Security and roles](#-security-and-roles)
- [Getting started](#-getting-started)
- [Configuration](#-configuration)
- [Main routes and REST API](#-main-routes-and-rest-api)
- [Testing the REST API with Postman](#-testing-the-rest-api-with-postman)
- [Project structure](#-project-structure)
- [Roadmap](#-roadmap)
- [Author](#-author)

---

## 📖 About the project

**EduManage** is a school management application built with **Spring Boot** and **Thymeleaf**. It covers the daily work of a school for adult learners: students, teachers, courses, classrooms, enrollments, grades, attendance and the weekly timetable.

Two things make it different from a classic CRUD project:

1. **AI notifications.** Events such as an absence, a new grade or a timetable change generate a clear message written by **Google Gemini**, with a safe template fallback when the AI is not available.
2. **AI data assistant.** Administrators and teachers can ask questions in natural language (in any language) and get answers calculated from the **live database**, not guessed by the AI.

---

## ✨ Key features

| Area | What you can do |
|---|---|
| 📊 **Dashboard** | Totals, students per classroom, attendance of the last 7 days, grade distribution |
| 👩‍🎓 **Students** | Create, edit, search, paginate, details page, report card in **PDF**, export to **Excel / CSV** |
| 👨‍🏫 **Teachers** | Full management with specialization and workload |
| 🏫 **Classrooms** | Capacity control and occupancy |
| 📚 **Courses** | Credits, assigned teacher, grade export |
| 📝 **Enrollments and grades** | Enroll students, record and edit grades per course |
| ✅ **Attendance** | Daily attendance sheet per classroom (Present, Absent, Late, Excused) with remarks |
| 🗓️ **Timetable** | Weekly grid with **room and teacher conflict detection** |
| 🔔 **Notifications** | AI-written, de-duplicated, shown in a carousel with unread counter |
| 🤖 **AI assistant** | Ask anything about your data and get a verified answer |
| 🔐 **Users and roles** | Authentication and authorization for Admin, Teacher, Student and Parent |
| 🧪 **Validation** | Bean Validation plus business rules (unique email, classroom capacity, realistic age...) |

---

## 🖼️ Screenshots

### 🔐 Authentication

<table>
  <tr>
    <td align="center"><img width="960" height="479" alt="login" src="https://github.com/user-attachments/assets/3779311b-2d53-4b25-a316-b4693ee896b2" /><br><b>Login</b></td>
    <td align="center"><img width="960" height="475" alt="create-account" src="https://github.com/user-attachments/assets/7c736352-e3a6-4b02-9a92-ee4e5bdfbd72" /><br><b>Create account</b></td>
  </tr>
</table>

### 📊 Dashboard

<img width="960" height="479" alt="dashboard" src="https://github.com/user-attachments/assets/ce316fd3-1cae-4c62-99f3-eb0201c94387" />

*Totals, students per classroom, attendance of the last 7 days and grade distribution.*

### 👩‍🎓 Students

<table>
  <tr>
    <td align="center"><img width="960" height="476" alt="students-list-search" src="https://github.com/user-attachments/assets/38e6064e-dcaa-486a-8ea6-065b02c18b69" /><br><b>List and search</b></td>
    <td align="center"><img width="960" height="476" alt="student-add-form" src="https://github.com/user-attachments/assets/df0162e0-9c8d-4b8f-a59f-e3d1a51d75b7" /><br><b>Add a student</b></td>
  </tr>
</table>

### 👨‍🏫 Teachers

<table>
  <tr>
    <td align="center"><img width="960" height="474" alt="teachers-list-search" src="https://github.com/user-attachments/assets/26e9f85e-a708-40b5-a952-8484bdb29c0f" /><br><b>List and search</b></td>
    <td align="center"><img width="960" height="478" alt="teacher-add-form" src="https://github.com/user-attachments/assets/439663cb-9ad0-499f-8019-4586ca3db049" /><br><b>Add a teacher</b></td>
  </tr>
</table>

### 🏫 Classrooms

<table>
  <tr>
    <td align="center"><img width="960" height="478" alt="classrooms-list-search" src="https://github.com/user-attachments/assets/1e90aca5-63ee-485d-a334-afa12a5c890f" /><br><b>List and search</b></td>
    <td align="center"><img width="960" height="475" alt="classroom-add-form" src="https://github.com/user-attachments/assets/bdf3a534-460a-44e6-9dc4-34a9c0d0c2b6" /><br><b>Add a classroom</b></td>
  </tr>
</table>

### 📝 Enrollments

<table>
  <tr>
    <td align="center"><img width="960" height="474" alt="enrollments-list" src="https://github.com/user-attachments/assets/45677493-47f8-4ddf-bfff-327310a4552e" /><br><b>Enrollments and grades</b></td>
    <td align="center"><img width="960" height="476" alt="enrollment-add-form" src="https://github.com/user-attachments/assets/ca60144a-88f6-496b-a4de-252647cc1cc0" /><br><b>Add an enrollment</b></td>
  </tr>
</table>

### 🗓️ Timetable

<img width="960" height="480" alt="Search For Schedule" src="https://github.com/user-attachments/assets/2ffee015-b487-4cdd-ba97-67a365acebce" />

<img width="960" height="476" alt="timetable-new-lesson" src="https://github.com/user-attachments/assets/6ed0a965-1d09-4073-93ac-9884b3a90ad4" />

*Plan a lesson by choosing the course, classroom, day and time. Room and teacher conflicts are detected automatically.*

### 🔔 AI notifications

<img width="960" height="476" alt="ai-notifications" src="https://github.com/user-attachments/assets/6c757758-7485-4ccb-87f0-09ecb78f6706" />

<img width="960" height="476" alt="Utilisation De Assistant AI " src="https://github.com/user-attachments/assets/1f086839-e7df-4340-a44f-703bbefa4a66" />

*Notifications written by Gemini, shown one at a time with arrows, a counter and an unread badge.*

### 🔑 Users management

<table>
  <tr>
    <td align="center"><img width="960" height="473" alt="users-list" src="https://github.com/user-attachments/assets/8d5b07fd-3f9b-46ec-8183-3d72159dfcc6" /><br><b>Users and roles</b></td>
    <td align="center"><img width="960" height="475" alt="user-add-form" src="https://github.com/user-attachments/assets/ea9ddca2-b409-4f57-b5c4-c6c63c3e61d2" /><br><b>Add a user</b></td>
  </tr>
</table>

<!--
Optional screenshots to add later. Save them in docs/screenshots with these names, then remove this comment:

### 🤖 AI assistant
<img src="docs/screenshots/16-ai-assistant.png" alt="AI data assistant" width="900">

### ✅ Attendance
<img src="docs/screenshots/17-attendance-sheet.png" alt="Daily attendance sheet" width="900">

### 🗓️ Weekly timetable
<img src="docs/screenshots/18-timetable-weekly-grid.png" alt="Weekly timetable grid" width="900">

### 📚 Courses
<img src="docs/screenshots/19-courses-list.png" alt="Courses list" width="900">
-->

---

## 🧰 Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot (Spring Web / MVC) |
| View | Thymeleaf with Thymeleaf Spring Security extras |
| Persistence | Spring Data JPA, Hibernate, **MySQL** |
| Security | Spring Security: authentication, authorization, method security (`@PreAuthorize`) |
| Validation | Jakarta Bean Validation (Spring Validation) |
| API | REST controllers (`/api/**`) |
| AI | **Google Gemini** through the Gemini REST API (`AiClient`) |
| Reports | PDF report cards, Excel and CSV exports |
| Productivity | Lombok, Spring Boot DevTools |
| Build | Maven |

---

## 🏗️ Architecture

The project follows a layered architecture:

```
Browser (Thymeleaf pages)          REST clients (Postman, apps)
          │                                   │
          ▼                                   ▼
   @Controller (MVC)                  @RestController (/api)
          └───────────────┬───────────────────┘
                          ▼
                   Services (business rules, validators)
                          │
          ┌───────────────┼────────────────┐
          ▼               ▼                ▼
   JPA Repositories   AI layer         Notification
   (MySQL)         (Gemini client)     service (@Async)
```

**Design choices**

- DTOs and mappers keep entities away from the web layer.
- Business validation lives in the service layer, so both the web pages and the REST API share the same rules.
- A notification problem must never break the real action: events are processed asynchronously and failures are logged, not thrown.

---

## 🤖 AI features

### 1. AI notifications

Events (absence, late, new grade, enrollment, timetable change, new student, teacher or course, classroom closed) go through a single method:

1. Remove duplicate recipients and events already sent (anti-duplicate key).
2. Build the message with **Gemini**, using the prompt files in `src/main/resources/prompts/`.
3. If the AI is unavailable, use a **fallback template**, so the user is always notified.
4. Save one notification per recipient and show it in the notification carousel.

Prompts are plain text files, so you can change the tone or the language without touching Java code.

### 2. AI data assistant

The assistant answers questions about the school with **verified numbers**. The AI never calculates; the database does.

```
Question ─► AI writes a SELECT ─► Safety gate ─► Read-only execution ─► AI explains the real result
```

| Step | Component | Role |
|---|---|---|
| 1 | `SchemaProvider` | Reads the real database structure and gives it to the AI (only whitelisted tables) |
| 2 | `assistant_sql.txt` | Prompt with the school knowledge, rules and examples |
| 3 | `SqlGuard` | Accepts only one plain `SELECT`; blocks other statements, comments and forbidden tables |
| 4 | `ReadOnlyQueryRunner` | Read-only connection, 5 s timeout, 100 rows maximum |
| 5 | `assistant_answer.txt` | The AI writes the answer using **only** the returned rows |

Every answer shows the **rows and the query used**, so any result can be checked.

**Example questions**

- *How many students are there?*
- *¿Quién es el estudiante más joven?*
- *Which course has the most students?*
- *What is lacking in my application?*
- *Which classrooms have free seats?*

**Safety layers**

- The prompt only allows `SELECT`.
- `SqlGuard` rejects anything else.
- The connection is read-only.
- The `users` and `notifications` tables are never exposed to the AI.
- For production, also use a MySQL user with `SELECT` rights only.

---

## 🔐 Security and roles

Authentication and authorization are handled by **Spring Security** (session login, password hashing, CSRF protection, method security).

| Role | Access |
|---|---|
| `ROLE_ADMIN` | Everything: management pages, users, timetable, AI assistant |
| `ROLE_TEACHER` | Students, grades, attendance, exports, AI assistant |
| `ROLE_STUDENT` | Own information and notifications |
| `ROLE_PARENT` | Information and notifications of their child |

Menu items and buttons are shown or hidden with `sec:authorize`, and sensitive endpoints are protected with `@PreAuthorize`.

---

## 🚀 Getting started

### Prerequisites

- **JDK 21** or later
- **Maven 3.9+** (or use the Maven wrapper)
- **MySQL 8**
- A **Google Gemini API key** (optional, the app works without it, only the AI features are disabled)

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/<your-repository>.git
cd <your-repository>
```

### 2. Create the database

```sql
CREATE DATABASE school_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Tables are created automatically by Hibernate (`ddl-auto=update`).

### 3. Configure the application

Edit `src/main/resources/application.properties` (see [Configuration](#-configuration)) and set your MySQL user and password.

### 4. Set your Gemini key (never commit it)

**Linux / macOS**
```bash
export GEMINI_API_KEY="your-key"
```

**Windows (PowerShell)**
```powershell
$env:GEMINI_API_KEY="your-key"
```

### 5. Run

```bash
./mvnw spring-boot:run
```

Open **http://localhost:8080** and sign in.

> Create your first administrator account in the database or with your user seeding code. Never keep default passwords in a public repository.

---

## ⚙️ Configuration

`src/main/resources/application.properties`

```properties
spring.application.name=school-management
server.port=8080

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/school_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:change-me}

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false

# AI (Gemini)
app.ai.enabled=true
app.ai.api-key=${GEMINI_API_KEY:}
app.ai.model=gemini-2.5-flash
app.ai.max-tokens=200
app.ai.timeout-seconds=60
app.notifications.language=English

# Tables the AI assistant is allowed to read
app.assistant.tables=students,teachers,courses,classrooms,enrollments,schedules,attendances
```

| Property | Description |
|---|---|
| `app.ai.enabled` | Turn all AI features on or off |
| `app.ai.api-key` | Gemini API key, read from the `GEMINI_API_KEY` environment variable |
| `app.ai.model` | Gemini model name |
| `app.ai.timeout-seconds` | HTTP timeout for AI calls (the assistant needs a generous value) |
| `app.notifications.language` | Language of the generated notifications |
| `app.assistant.tables` | Whitelist of tables the assistant can query |

> ⚠️ **Security.** Never commit API keys or passwords. Use environment variables, or a `secrets.properties` file listed in `.gitignore`. If a key was ever pushed to GitHub, revoke it and create a new one.

---

## 🌐 Main routes and REST API

### Web pages

| Route | Description |
|---|---|
| `/dashboard` | Statistics and charts |
| `/students`, `/teachers`, `/classrooms`, `/courses` | Management pages |
| `/enrollments` | Enrollments and grades |
| `/courses/{id}/grades` | Grade sheet of a course |
| `/attendances` | Daily attendance sheet |
| `/schedules` | Weekly timetable |
| `/notifications` | Notification carousel |
| `/assistant` | AI assistant |
| `/users` | Users management (admin) |
| `/students/{id}/report-card` | Student report card (PDF) |
| `/export/students.xlsx`, `/export/students.csv` | Exports |

### REST API

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/students?keyword=&page=0&size=10` | Paginated list with search |
| `GET` | `/api/students/{id}` | One student |
| `POST` | `/api/students` | Create a student |
| `PUT` | `/api/students/{id}` | Update a student |
| `DELETE` | `/api/students/{id}` | Delete a student |

Validation errors return `400 Bad Request`, and business rule errors (duplicate email, classroom full...) return `409 Conflict`, both with the failing field and a clear message.

---

## 🧪 Testing the REST API with Postman

**Create a student**

`POST http://localhost:8080/api/students`

```json
{
  "firstName": "Ahmed",
  "lastName": "Benali",
  "email": "ahmed.benali@example.com",
  "phone": "+212 612-345678",
  "birthDate": "2010-05-14",
  "gender": "MALE",
  "classroomId": 1
}
```

**Validation error example**

```json
{
  "firstName": "",
  "email": "not-an-email",
  "gender": null
}
```

Expected: `400 Bad Request` with messages such as *First name is mandatory*, *Email should be valid* and *Gender is mandatory*.

---

## 🗂️ Project structure

```
src/main/java/com/example/School/Management/System
├── api/            REST controllers
├── assistant/      AI data assistant (schema, SQL guard, runner, service)
├── config/         Security and application configuration
├── controller/     MVC controllers (Thymeleaf)
├── dto/            Data transfer objects and records
├── entity/         JPA entities
├── enums/          Gender, roles, attendance status, notification types
├── exception/      Custom exceptions and handlers
├── mapper/         Entity ↔ DTO mappers
├── prompt/         Prompt loading and rendering
├── repository/     Spring Data JPA repositories
└── service/        Business logic, AI client, notifications, PDF/Excel export

src/main/resources
├── prompts/        AI prompt files (.txt)
├── static/         CSS and assets
├── templates/      Thymeleaf views
└── application.properties
```

---

## 🛣️ Roadmap

- [ ] Docker Compose (application + MySQL)
- [ ] Unit and integration tests (JUnit, Mockito, Testcontainers)
- [ ] OpenAPI / Swagger documentation
- [ ] Flyway database migrations
- [ ] Email and SMS notifications
- [ ] More AI assistant questions: workload, absences trends, grade predictions
- [ ] CI pipeline with GitHub Actions

---

## 👤 Author

**Mohamed Chaabi**

[![GitHub]https://github.com/mChaabi/School_Management_Project]

[![LinkedIn]https://www.linkedin.com/in/mohamed-chaabi-8214112a5/?isSelfProfile=true]

---

<div align="center">

⭐ If you like this project, give it a star on GitHub.

</div>
