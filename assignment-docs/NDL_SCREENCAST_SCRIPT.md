# Numbers Don't Lie - Screencast Script

Use this script to record a clean reviewer demo of the current project behavior.  
This script focuses on **showable features** from `ASSIGNMENT_TEST.md`.

## 1. Recording Setup (Before you hit record)

1. Start app in demo mode:
```bash
cp .env.demo.example .env
docker compose down -v
docker compose up -d --build
```
2. Open tabs:
- Frontend: `http://localhost:5173`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- MailHog: `http://localhost:8025`
3. Use demo account (or register a fresh one if showing verification flow):
- `demo@example.com / demo@example.com`

## 2. Suggested Video Flow (10-15 min)

## 0:00-0:45 Intro
- Show README top section + project name.
- State: "This demo maps to assignment test requirements."

## 0:45-2:15 Auth + verification + reset
- Show registration/login options (email/password + Google + GitHub buttons).
- Show MailHog inbox for verification/reset email flow.
- Show forgot/reset password page quickly.
- Mention optional 2FA exists in Settings (you will demonstrate later).

## 2:15-3:30 Consent gate
- Log in with a user without consent (or toggle consent off in Settings first).
- Show app redirect to consent page.
- Click consent accept and show app unlock.
- Mention explicit data usage explanation on consent/settings screens.

## 3:30-5:30 Health profile + check-ins
- Open Health Profile page and show demographics/activity/dietary/fitness assessment fields.
- Save profile and show success without page reload.
- Go to Check In:
  - add weight entry
  - add activity check-in
  - show timeline updates
- Mention duplicate timestamp checks are enforced.

## 5:30-7:30 Goals + progress
- Open Goals page.
- Show active goals (or create one).
- Show goal history / archive behavior.
- Mention milestone tracking (5% steps) and progress snapshots.

## 7:30-10:00 Dashboard + trends + summaries
- Open Dashboard:
  - BMI card
  - wellness score
  - active goals
  - AI insight card
  - weekly/monthly summary cards
  - comparison view
- Open Trends:
  - weight trend with target line
  - wellness evolution line
  - wellness component breakdown
  - activity heatmap
  - 30/90/all range switch
- Mention loading/error states are implemented.

## 10:00-11:00 Privacy + account controls
- Open Settings:
  - privacy preferences toggles
  - linked identity section
  - 2FA setup (show QR if enabled path)
  - data export button
  - account delete confirmation flow (do not confirm if using main demo user)

## 11:00-12:00 API/reliability proof points
- Show Swagger endpoints quickly:
  - `/api/profile`
  - `/api/weight`
  - `/api/activity-checkins`
  - `/api/goals`
  - `/api/summary/weekly`, `/api/summary/monthly`
- Mention:
  - JWT access/refresh flow
  - rate limiting + graceful errors
  - AI cache/fallback behavior

## 3. "Show It" Checklist (while recording)

- [ ] Email/password + OAuth options visible
- [ ] Verification/reset email visible in MailHog
- [ ] Consent gate redirect works
- [ ] Health profile save works
- [ ] Weight + activity check-ins recorded
- [ ] Goals and progress visible
- [ ] Dashboard cards render correctly
- [ ] Trends charts render correctly
- [ ] Privacy settings editable
- [ ] 2FA setup screen + QR visible
- [ ] Data export action shown
- [ ] Error/loading UX briefly shown

## 4. Optional 2-3 minute Add-On (if time)

- Show one negative path:
  - disable consent -> insight blocked
  - malformed request -> meaningful API error
- Show logs:
```bash
docker compose logs -f backend
```
and point out AI fallback/rate-limit handling messages.

## 5. What to keep out of this video

- Deep implementation explanations (put these in oral defense notes).
- Long code walkthroughs.
- Environment debugging.

Keep the video product-behavior focused.

## 5.1 Optional Security Demo: Expired Access Token + Refresh Token

Use this to explicitly prove:
- access token expiry
- refresh flow issuance of a new access token
- Verify receiving a new access token by sending a request with expired access token and valid refresh token
  - Supported by current refresh endpoint and frontend auth handling.

### A. Run backend with short access-token expiry (1 min)

```bash
cd backend
APP_JWT_ACCESS_TOKEN_EXPIRY_MINUTES=1 ./mvnw spring-boot:run
```

### B. Login and capture tokens

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"demo@example.com"}'
```

Copy from response:
- `data.access_token`
- `data.refresh_token`

### C. Show valid access token works

```bash
curl -i http://localhost:8080/api/me \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

Expected: `200 OK`.

### D. Wait for expiry and show it fails

Wait ~70 seconds, then run again:

```bash
curl -i http://localhost:8080/api/me \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

Expected: `401 Unauthorized`.

### E. Refresh access token

```bash
curl -s -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refresh_token":"<REFRESH_TOKEN>"}'
```

Copy new `data.access_token`.

### F. Show refreshed token works

```bash
curl -i http://localhost:8080/api/me \
  -H "Authorization: Bearer <NEW_ACCESS_TOKEN>"
```

Expected: `200 OK`.

### Talking points (while recording)

- "Access token is intentionally short-lived for security."
- "When expired, protected endpoint returns 401."
- "Valid refresh token gets a new access token via `/api/auth/refresh`."
- "This preserves UX without weakening token-lifetime security."

## 6. Fast 5-Minute Version

Use this when reviewers want only the essentials.

## 0:00-0:30 Startup proof
- Show app running and login screen.
- Mention auth methods: email/password + Google + GitHub.

## 0:30-1:15 Consent + setup gate
- Show consent required flow (or explain with quick page switch).
- Accept consent and continue to app.

## 1:15-2:15 Profile + check-in
- Open Health Profile, show filled data, save.
- Open Check In and add one weight + one activity entry.

## 2:15-3:30 Dashboard
- Show BMI, wellness score, active goals, insight card.
- Show weekly/monthly summary cards.

## 3:30-4:30 Trends
- Show weight chart + target line.
- Show wellness evolution and activity heatmap.
- Switch range (30/90/all).

## 4:30-5:00 Settings + compliance highlights
- Show privacy preferences and 2FA QR setup section.
- Show data export button.
- Mention email verification/reset available (MailHog demo-capable).

### 5-Minute Must-Show Checklist
- [ ] Auth options visible
- [ ] Consent gate behavior
- [ ] Health profile save
- [ ] Weight/activity check-in
- [ ] Dashboard metrics + insight
- [ ] Trends charts + range switch
- [ ] Settings privacy + 2FA + export
