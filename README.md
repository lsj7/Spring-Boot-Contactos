# spring-agenda

Contacts app built with Spring Boot. It keeps the agenda pages and the `/agenda` API, stores contacts in a new SQLite file, and signs in a single administrator with a server session.

The FastAPI project stays separate. This app does not add public registration, JWT, or a user table.

## Requirements

- JDK 21 or newer (the project compiles for Java 21)
- Maven is included through the `./mvnw` wrapper, so a separate Maven install is optional

Point `JAVA_HOME` at a JDK before the commands below. On this machine:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home
```

## Administrator

Set both variables in the environment. The password is the plain login password; the app hashes it in memory at startup and does not store an account in the database.

```bash
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=cambia-esta-clave
```

Startup fails if either value is missing.

## Database

SQLite creates `agenda-spring.db` in the directory where you start the app. Hibernate creates the `contacto` table on startup (`ddl-auto=update`). This file is not the FastAPI `agenda.db`.

Tests use a temporary database under the system temp directory and do not write `agenda-spring.db`.

## Run

From this directory:

```bash
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=cambia-esta-clave
./mvnw spring-boot:run
```

Open http://localhost:8080. The browser asks for the administrator, then shows the contact directory.

## Routes

| Method | Path | Auth |
| --- | --- | --- |
| GET | `/login` | public |
| POST | `/login` | public, CSRF |
| POST | `/logout` | session, CSRF |
| GET | `/`, `/views/agenda` | session |
| GET | `/views/contacts/new` | session |
| GET | `/views/contacts/{id}/edit` | session; 404 if the contact is missing |
| GET | `/agenda`, `/agenda/{id}` | session; 401 without a session; 404 if the contact is missing |
| POST | `/agenda` | session and CSRF; 201 or 422 |
| PUT | `/agenda/{id}` | session and CSRF; partial update; 404 or 422 |
| DELETE | `/agenda/{id}` | session and CSRF; 404 if the contact is missing |

JSON fields are `id`, `nombre`, `telefono`, and `mail`. A missing contact returns `{"detail":"Contacto no encontrado"}`. Create and update validate the name (2–50 characters), the phone (`7–15` digits), and the email. Responses include `X-Process-Time`.

Browser create, edit, and delete calls send the session cookie and the page CSRF token (`X-CSRF-TOKEN`). CSRF stays enabled.

## Tests

```bash
./mvnw test
```

The tests cover login and logout, access rules, CSRF, CRUD status codes, validation, missing contacts, the Spanish pages, and reads through a temporary SQLite file.
