# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

- Build: `./gradlew build`
- Run locally: `./gradlew bootRun` (serves on port 8080 by default; `spring-boot-docker-compose` is a `developmentOnly` dependency, so `bootRun` can auto-start the Postgres container defined in `compose.yaml`)
- Tests: `./gradlew test` (JUnit 5 via `useJUnitPlatform()`, `mockk` for mocking)
- To run a single test class: `./gradlew test --tests "org.jagrati.jagratibackend.SomeTestClass"`
- No lint task is configured (no ktlint/detekt plugin).

Docker: `Dockerfile` is a multi-stage build (`gradle:8.8-jdk17` → `eclipse-temurin:17-jdk-alpine`), producing `jagrati_app.jar`, exposing 8080. `compose.yaml` at the repo root only runs Postgres 16 for local dev (port 5433→5432). `docker/prod/` and `docker/staging/` hold environment-specific compose files.

## Configuration

Config lives in `src/main/resources/application.properties` (properties, not YAML). Required env vars:
- `JWT_SECRET_BASE_64` — required, no default
- `DATABASE_URL` (default `jdbc:postgresql://localhost:5432/jagrati`), `POSTGRES_USER`/`POSTGRES_PASSWORD` (default `postgres`/`postgres`)
- `MAIL_ID`, `GMAIL_PASS` — SMTP via Gmail
- `BASE_URL` (default `http://localhost:8080`)
- `OAUTH2_CLIENT_ID` — Google OAuth2 client id (client secret is not required for the Android OAuth2 flow)
- `ADMIN_MAIL`, `ADMIN_PASS` — seeds the super-admin account on startup
- `IMAGE_KIT_PRIVATE_KEY`

Schema is Flyway-controlled (`spring.jpa.hibernate.ddl-auto=none`) — migrations live in `src/main/resources/db/migration/` (`V1__DB_INIT.sql`, `V2__FCM_IMPLEMENTATION.sql`, `V3__POSTS_FEATURE.sql`, ...). Add new schema changes as new versioned migration files there, never by relying on Hibernate auto-DDL.

## Architecture

Kotlin + Spring Boot 3.5 (Java 17), single Gradle module. Standard layered MVC structure under `src/main/kotlin/org/jagrati/jagratibackend/`:

- `controller/` — REST controllers, one per feature (`AuthController`, `UserController`, `StudentController`, `VolunteerController`, `AttendanceController`, `PostsController`, `RoleController`, `PermissionController`, `GroupController`, `VillageController`, `ImageKitController`)
- `services/` (interfaces) + `services/impl/` (implementations)
- `repository/` — Spring Data JPA repositories
- `entities/` (+ `entities/enums/`) — JPA entities
- `dto/` (+ `dto/imagekit/`) — request/response DTOs
- `security/` — JWT auth (`JWTService`), OAuth2 success handler, permission-based method security (`@RequiresPermission` annotation + `PermissionAspect`, `CustomMethodSecurityExpressionRoot/Handler`), refresh-token cleanup
- `config/` — `SecurityConfiguration`, `JWTAuthenticationFilter`, `ThrottlingFilter` (bucket4j rate limiting), `OpenApiConfig`, `FirebaseConfig` (FCM push), `MethodSecurityConfig`, `DataInitializationConfig` (seeds super-admin)
- `exception/`, `utils/`

Auth model: stateless JWT (jjwt) plus Google OAuth2 (verifying Android client ID tokens), layered with a custom permission-based authorization system (`@RequiresPermission` + AOP aspect) on top of Spring Security — this is a role/permission model, not just role-based auth. Also integrates Firebase Admin (FCM push notifications), ImageKit (image storage), and email via Spring Mail + Thymeleaf templates.

API is RESTful/JSON, documented via SpringDoc OpenAPI — Swagger UI at `/swagger-ui.html`, spec at `/v3/api-docs`.

## Related repo

The Android client for this API lives in a sibling repo, `../Jagrati-Android` (Kotlin/Jetpack Compose), which points at this service via its own `BASE_URL` config.
