# Numbers Don't Lie

Wellness tracking app with health profile collection, goal tracking, wellness scoring, AI-generated insights, and trend visualizations.

## Table Of Contents

- [Project Overview](#project-overview)
- [Tech Stack](#tech-stack)
- [Setup And Installation](#setup-and-installation)
- [Run Modes](#run-modes)
- [Demo Data Details](#demo-data-details)
- [Demo Troubleshooting](#demo-troubleshooting)
- [Usage Guide](#usage-guide)
- [5-Minute Overview](#5-minute-overview)
- [Local Development](#local-development)
- [Environment Variables](#environment-variables)
- [AI Insights Notes](#ai-insights-notes)
- [Further Development Ideas](#further-development-ideas)

## Project Overview

Main capabilities:
- Account system: email/password, Google OAuth, GitHub OAuth
- Email verification, refresh-token based sessions, password reset
- Optional user-enabled 2FA (TOTP + QR setup)
- Health profile + fitness assessment
- Weight + activity check-ins and historical tracking
- Goals, progress snapshots, and milestone tracking
- BMI + wellness score analytics
- Active-days (last 7d) activity indicator
- AI insights with caching/fallback and response guardrails
- Privacy preferences, data export, account deletion

Bonus/extra functionality implemented:
- Multi-provider account identity linking with email-collision guidance
- AI guardrails (grounding, safety checks, anti-repeat, restriction checks)
- Demo data mode for end-to-end visualization testing
- Application-layer at-rest protection for sensitive auth secrets

## Tech Stack

- Backend: Java, Spring Boot, Spring Security, JPA/Hibernate, Flyway, PostgreSQL
- Frontend: React, TypeScript, Vite
- Auth: Local JWT + Auth0 resource-server validation for OAuth tokens
- AI: OpenAI Responses API (optional), strict JSON schema output

## Setup And Installation

1. Copy env template:

```bash
cp .env.example .env
```

Quick alternatives:
- Demo template: `cp .env.demo.example .env`
- Clean template: `cp .env.clean.example .env`

2. Set secure values in `.env`:
- `APP_TOKEN_PEPPER`
- `APP_DATA_ENCRYPTION_KEY`

Generate them with:

```bash
openssl rand -base64 48
openssl rand -base64 48
```

Use first output as `APP_TOKEN_PEPPER`, second as `APP_DATA_ENCRYPTION_KEY`.

3. Start with one command:

```bash
docker compose up -d --build
```

## Run Modes

### Demo (Recommended First)

This mode is optimized for demo purposes:
- pre-seeded data (`DEMO_MODE=true`)
- frontend demo login mode
- real email flow captured in MailHog inbox UI (verification/reset links visible)

Run:

```bash
DEMO_MODE=true VITE_DEMO_MODE=true APP_EMAIL_ENABLED=true docker compose up -d --build
```

Open:
- Frontend: `http://localhost:5173`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- MailHog inbox UI: `http://localhost:8025`

Demo credentials:
- `demo@example.com` / `demo@example.com`

Read Docker logs for AI insight generation details and email payloads. From project root (./numbers-dont-lie):

All services, live stream:
```bash
docker compose logs -f
```

Specific services:
```bash
docker compose logs -f postgres
docker compose logs -f backend
docker compose logs -f frontend
```

Last 200 lines only:

```bash
docker compose logs --tail=200 postgres
docker compose logs --tail=200 backend
docker compose logs --tail=200 frontend
```

With timestamps:

```bash
docker compose logs -f -t backend
```

If service names differ, list them:

```bash
docker compose ps
```

## Demo Data Details

Seeded demo account:
- Email: `demo@example.com`
- Password: `demo@example.com`
- User ID: `00000000-0000-0000-0000-000000000001`

Seeded data includes:
- Health profile with realistic baseline values
- Active goal(s) and goal progress history
- Historical weight entries for trend visualization
- Activity check-ins for heatmap/timeline testing

Why this helps:
- Lets reviewers immediately validate dashboard/trends behavior
- Avoids manual data entry before testing assignment criteria
- Makes AI insight context richer during demos

Reset demo database:

```bash
docker compose down -v
DEMO_MODE=true VITE_DEMO_MODE=true APP_EMAIL_ENABLED=true docker compose up -d --build
```

## Demo Troubleshooting

Demo tab not visible:
- Ensure `VITE_DEMO_MODE=true`
- Rebuild frontend: `docker compose up -d --build`

Demo user/data not appearing:
- Ensure `DEMO_MODE=true`
- Reset volumes and start again (`docker compose down -v`)

Email verification/reset not visible in inbox:
- Ensure `APP_EMAIL_ENABLED=true`
- Ensure `MAIL_HOST=mailhog`, `MAIL_PORT=1025`
- Open `http://localhost:8025`

### Clean Mode (No Demo Seed Data)

Use this when you want an empty database and non-demo UX:

```bash
DEMO_MODE=false VITE_DEMO_MODE=false APP_EMAIL_ENABLED=false docker compose up -d --build
```

In this mode, email verification/reset content is logged to backend console unless you configure SMTP and enable `APP_EMAIL_ENABLED=true`.

### Default One-Line Run (Docker)

```bash
docker compose up -d --build
```

Open:
- Frontend: `http://localhost:5173`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- MailHog (if used): `http://localhost:8025`

Stop:

```bash
docker compose down
```

Reset database:

```bash
docker compose down -v
docker compose up -d --build
```

## Usage Guide

Typical user flow:
1. Sign up/login (email/password, Google, or GitHub)
2. Verify email (for local email/password auth)
3. Complete health profile
4. Add weight and activity check-ins
5. Create an active goal
6. View dashboard (BMI, wellness score, active days, summaries, AI insight)
7. View trends (weight line, wellness evolution, component breakdown, heatmap)
8. Use settings for privacy consent, 2FA, export, and account deletion

## 5-Minute Overview

Use this exact flow for fast requirement verification:

1. Run app and log in.
2. Toggle consent in Settings and confirm AI insight gating on Dashboard.
3. Save Health Profile updates and verify success without page reload.
4. Add one weight + one activity check-in; confirm timeline updates.
5. Open Dashboard and Trends:
   - Dashboard: BMI, wellness score, active days (7d), goal, insight, weekly/monthly cards
   - Trends: weight chart, wellness charts, activity heatmap, range switch
6. Run data export and confirm historical JSON payload.


## Local Development

1. Start database only:

```bash
docker compose -f ./infra/docker-compose.yml --env-file ./.env up -d
```

2. Start backend:

```bash
cd backend
./mvnw -q -DskipTests compile
./mvnw spring-boot:run
```

3. Start frontend:

```bash
cd frontend
npm install
npm run dev
```

## Environment Variables

Use root `.env` for all normal runs.

Important behavior:
- `docker compose` automatically reads `.env` from project root.
- Inline values override `.env` for that command only.
- For submission/one-line run, only root `.env` is required.

### Demo vs Clean Env

| Variable | Demo value | Clean value | Notes |
|---|---|---|---|
| `DEMO_MODE` | `true` | `false` | Backend demo seed data |
| `VITE_DEMO_MODE` | `true` | `false` | Frontend demo login tab |
| `APP_EMAIL_ENABLED` | `true` | `false` or `true` | `true` sends via SMTP/MailHog |
| `MAIL_HOST` | `mailhog` | `mailhog` or SMTP host | Docker default uses MailHog |
| `MAIL_PORT` | `1025` | `1025` or SMTP port | MailHog SMTP port |
| `OPENAI_API_KEY` | optional | optional | If absent, app uses fallback insight |

### Core Keys Checklist

- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`
- `APP_TOKEN_PEPPER`
- `APP_DATA_ENCRYPTION_KEY`
- `VITE_AUTH0_AUDIENCE`
- `OPENAI_API_KEY` (optional), `OPENAI_MODEL` (optional)
- `APP_EMAIL_*` and `MAIL_*` (for real email flow)

### Email Testing Setup

Reviewer-friendly local setup with MailHog:
1. Set `APP_EMAIL_ENABLED=true`
2. Keep `MAIL_HOST=mailhog` and `MAIL_PORT=1025` (docker compose defaults)
3. Run `docker compose up -d --build`
4. Open `http://localhost:8025` to see verification and password-reset emails

If `APP_EMAIL_ENABLED=false`, email payloads are intentionally logged to backend console for local testing only.

### Encryption In Transit (HTTPS)

You can run backend HTTPS locally using a PKCS12 keystore:

1. Generate a local keystore (example):

```bash
keytool -genkeypair \
  -alias tomcat \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore backend/local-dev.p12 \
  -validity 3650 \
  -storepass changeit \
  -dname "CN=localhost"
```

2. Set env vars:
- `SERVER_SSL_ENABLED=true`
- `SERVER_SSL_KEY_STORE=file:./local-dev.p12` (or absolute path)
- `SERVER_SSL_KEY_STORE_PASSWORD=...`
- `SERVER_SSL_KEY_STORE_TYPE=PKCS12`
- `SERVER_SSL_KEY_ALIAS=tomcat`

When enabled, backend traffic is encrypted over HTTPS.

### Encryption At Rest

Application-layer at-rest encryption is enabled for sensitive health fields:
- `health_profiles.dietary_preferences` (encrypted JSON text)
- `health_profiles.dietary_restrictions` (encrypted JSON text)
- `health_profiles.fitness_assessment` (encrypted JSON text)
- `weight_entries.note` (encrypted text)
- `activity_checkins.note` (encrypted text)

Encryption key material:
- `APP_DATA_ENCRYPTION_KEY`

If running backend directly and loading `.env` into shell:

```bash
set -a
source .env
set +a
```

### Which `.env` Files Are Needed?

- Required: `.env` (root)
- Template/reference: `.env.example`
- Ready presets: `.env.demo.example`, `.env.clean.example`

## AI Insights Notes

When `OPENAI_API_KEY` is configured:
- Insights are generated with strict JSON schema
- Responses are cached by prompt-context hash
- Guardrails enforce grounding/safety/novelty/restriction constraints

When AI is unavailable:
- Service returns latest cached insight or a safe fallback insight

## Further Development Ideas

Based on the current implementation, these are practical next steps:

1. Security and Compliance
- Move from app-layer encryption only to full infrastructure encryption-at-rest (managed volume encryption + key rotation policy).
- Run backend strictly behind HTTPS in all environments and add HSTS/reverse-proxy hardening.
- Add structured audit trail for sensitive actions (consent changes, exports, account deletion, security settings).

2. AI Quality and Evaluation
- Add an internal evaluation suite with scenario-based scoring for relevance, safety, and goal alignment.
- Add stricter domain validators (exercise contraindications, nutrition restriction ontology, unsafe advice classes).
- Support model fallback strategy by tier (primary model -> cheaper model -> deterministic safe template).

3. Goals and Progress
- Add auto-progress snapshots for weight goals on each weight check-in (similar to activity goal auto-progress).
- Add projected completion date per active goal using trend slope and confidence range.
- Add per-goal timeline views (milestones + notes + check-in correlation).

4. Analytics and Visualization
- Add richer comparison views (week-over-week and month-over-month component deltas).
- Add configurable dashboard widgets and saved ranges.
- Add trend annotations for key events (goal created, milestone reached, major weight change).

5. UX and Product Flow
- Add first-run onboarding wizard (profile -> consent -> first check-in -> first goal).
- Expand timeline interactions (edit/delete check-ins inline with optimistic updates).
- Add explicit “data completeness” hints to guide users toward better AI insight quality.

6. Testing and Reliability
- Increase backend integration coverage for auth, privacy, and AI fallback paths.
- Add frontend e2e smoke suite for reviewer-critical flows.
- Add migration verification tests to prevent schema drift and data conversion issues.

7. Operations and Observability
- Add structured logs and request tracing across auth/AI/export endpoints.
- Add metrics dashboards (error rates, response times, AI fallback frequency, rate-limit hits).
- Add environment health checks for external dependencies (email provider, Auth0, AI provider).
