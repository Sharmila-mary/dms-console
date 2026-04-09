# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**DMS Field App** — a full-stack application with:
- **Backend**: Spring Boot 3.2.4 (Java 17) REST API on port 8080
- **Frontend**: React 19 (Create React App) on port 3000
- **Database**: MySQL (`kellogg_uat` schema)
- **AI Integration**: Anthropic Claude API (claude-sonnet-4-20250514 model)

## Commands

### Backend (Maven)
```bash
./mvnw spring-boot:run       # Start backend dev server
./mvnw clean package         # Build JAR
./mvnw test                  # Run all tests
./mvnw test -Dtest=ClassName # Run a single test class
```

### Frontend (npm, run from `frontend/`)
```bash
npm start        # Start dev server (http://localhost:3000)
npm test         # Run tests (watch mode)
npm run build    # Production build
```

## Architecture

The backend and frontend are developed separately — the Spring Boot app serves the REST API, and the React app consumes it via `http://localhost:8080`.

### Backend (`src/main/java/com/example/reportportal/`)
- `DemoApplication.java` — Spring Boot entry point
- `controller/ReportController.java` — REST controller under `/api/reports`, CORS-configured for `localhost:3000`
- `src/main/resources/application.properties` — DB connection, Claude API config, JPA settings (DDL auto = update)

### Frontend (`frontend/src/`)
- `App.js` — root React component
- `recharts` is installed for data visualization

### Key Config
- CORS is enabled in `ReportController` (`@CrossOrigin("http://localhost:3000")`)
- JPA DDL is set to `update` — schema changes apply automatically on startup
- Claude API endpoint and model are configured in `application.properties` under `anthropic.*` properties
