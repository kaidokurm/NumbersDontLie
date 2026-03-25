### Testing

Ensures that software works as expected by validating features against requirements.

How to do testing?

1. Download, build, and run the submitted code.
2. Agree on your teamwork - how do you divide testing between reviewers.
3. Test functionality and check compliance with the requirements.
4. Provide feedback in the group chat and request fixes if necessary.
5. Clearly state what changes are mandatory, and what are optional fixes.
6. Repeat the testing cycle after submitters make the requested changes.

#### Mandatory

* [X] Student can explain how PII removal affects AI model's ability to generate personalized recommendations
  - Explanation prepared in `REQUIREMENTS_FULFILLMENT_EXPLANATIONS.md` (PII removed from prompt, behavior-level personalization retained).

* [X] Student can explain their strategy for detecting and handling AI hallucinations in health recommendations
  - Implemented and documented: strict JSON schema, runtime validation, grounding checks, percent-claim gating, safety filter, fallback/cached responses.

* [X] User receives an email with verification link/code after registration
  - Registration triggers verification generation and email sending flow.

* [X] Authentication options include email-password and at least 2 OAuth providers
  - Implemented: email/password + Auth0 Google + Auth0 GitHub.

* [X] Password reset is handled via email
  - Implemented `/api/password-reset/request` and `/api/password-reset/complete` with email token flow.

* [X] Users can optionally enable two-factor authentication
  - Implemented optional TOTP 2FA setup/enable/disable + QR setup UX.

* [X] User cannot access protected routes without verifying email
  - Enforced in login flow for local accounts (`emailVerified` required).

* [X] Student can explain the security implications of access token duration in JWT authentication
  - Explanation prepared in `REQUIREMENTS_FULFILLMENT_EXPLANATIONS.md` (short-lived access tokens + refresh model).

* [X] Access token expires after configured minutes of inactivity
  - Configured in backend auth properties (`access-token-expiry-minutes`, default 15).

* [X] New access token is issued when valid refresh token is provided
  - Implemented `/api/auth/refresh`.

* [X] Verify receiving new access token by sending a request with expired access token and valid refresh token
  - Supported by current refresh endpoint and frontend auth handling.

* [X] Platform provides clear data usage consent with explicit user approval before data collection
  - Implemented dedicated first-use consent gate page (`/consent`) with app-wide redirect until consent is accepted.

* [X] At minimum, consent includes what data is collected and how it's used
  - Settings page consent section now explicitly states collected data categories and usage purposes (analytics/summaries/AI insights).

* [X] Platform collects basic demographics, physical metrics, lifestyle indicators, dietary preferences and restrictions, wellness goals
  - Implemented in profile/check-in/goals modules.

* [X] User data is encrypted in transit and at rest
  - Implemented app-layer at-rest encryption for sensitive health fields (dietary preferences/restrictions, fitness assessment, weight/activity notes) and HTTPS-ready in-transit configuration via backend SSL keystore settings.

* [X] Platform collects initial fitness assessment data
  - Implemented with fitness assessment fields in health profile.

* [X] Student can explain how normalization of health metrics impacts data visualization accuracy
  - Explanation prepared in `REQUIREMENTS_FULFILLMENT_EXPLANATIONS.md`.

* [X] Health metrics are converted to standard units before storage
  - Implemented explicit backend conversion pipeline: `height_unit` (`cm`/`in`) converts to stored `height_cm`, and `weight_unit` (`kg`/`lb`) converts to stored `weight_kg`.

* [X] Platform allows user to change their data sharing preferences
  - Implemented via privacy preferences settings endpoint/UI.

* [X] Historical weight changes are tracked with timestamps
  - Implemented weight history with `measuredAt`.

* [X] Verify each weight entry has a unique timestamp by adding multiple entries and checking their history display
  - Duplicate timestamp protection implemented in `WeightService`.

* [X] System prevents duplicate activity entries for the same timestamp
  - Implemented dedicated `activity_checkins` entity with unique `(user_id, checkin_at)` and service-level collision handling.

* [X] Student can explain how BMI classifications affect wellness score calculation
  - Explanation prepared and formula implemented in `WellnessScoreCalculator`.

* [X] Wellness score changes when user updates weekly activity frequency
  - Wellness now uses real activity check-ins (last 7 days) and recalculates on activity/profile/goal updates.

* [X] Student can explain their choice of AI model(s) based on response quality and latency requirements
  - Explanation prepared in `REQUIREMENTS_FULFILLMENT_EXPLANATIONS.md`.

* [X] Student can explain what model capabilities were most important for implementation
  - Explanation prepared in `REQUIREMENTS_FULFILLMENT_EXPLANATIONS.md` (structured output reliability prioritized).

* [X] System recalculates wellness score components when contributing metrics change
  - Triggered on profile update, weight update, and goal progress record.

* [X] Verify scores update when changing BMI range, activity level, goal progress, or health habits
  - Supported by recalculation points and component model.

* [X] System generates different health insights when user's goals change
  - Goal data is part of insight context hash; changed context triggers regeneration path.

* [X] Verify insight adjustment after changing user goals from one type to another
  - Supported by active-goal context and cache key regeneration.

* [X] Student can explain the difference between AI response caching and regeneration
  - Explanation prepared in `REQUIREMENTS_FULFILLMENT_EXPLANATIONS.md`.

* [X] Student can explain how context length affects AI response quality in health recommendations
  - Explanation prepared in `REQUIREMENTS_FULFILLMENT_EXPLANATIONS.md`.

* [X] AI recommendations include specific references to user's stated fitness goals
  - Enforced by strict AI validation guard (`validateGoalSpecificReference`) and prompt rule requiring explicit active-goal references.

* [X] AI health insights exclude any personally identifiable information
  - Insight context excludes direct PII (email/name/auth identifiers).

* [X] Student can explain prompt engineering approach to ensure consistent recommendation format
  - Explanation prepared in `REQUIREMENTS_FULFILLMENT_EXPLANATIONS.md` and implemented with strict schema + prompt structure.

* [X] System implements response validation to filter out recommendations that don't match user's health restrictions
  - Implemented post-generation dietary restriction validation in `AiInsightService`.

* [X] Student can explain tradeoffs between zero-shot and few-shot prompting
  - Explanation prepared in `REQUIREMENTS_FULFILLMENT_EXPLANATIONS.md`.

### Functional Checks

- [X] System displays cached recommendations when AI service is unavailable
  - Implemented fallback to cached/latest safe insight.

- [X] System generates weekly and monthly health summaries including progress and key metrics
  - Implemented `/api/summary/weekly` and `/api/summary/monthly`.

- [X] Health dashboard shows BMI, wellness score, progress towards goals and AI insights based on user data
  - Implemented dashboard cards and data orchestration.

- [X] Progress chart shows weight tracking over time, wellness score evolution and activity level changes
  - Implemented weight trend + wellness evolution + activity heatmap from activity check-ins.

- [X] Goal progress includes milestone tracking
  - Implemented milestone tracking at 5% intervals.

- [X] Comparison view shows current vs target metrics, weekly/monthly comparison, trends and AI recommendations
  - Implemented dedicated `ComparisonViewCard` on Dashboard with current-vs-target, weekly/monthly comparison, trends link, and AI recommendation context.

- [X] Student can explain how data visualization choices affect user's understanding of progress
  - Explanation prepared in `REQUIREMENTS_FULFILLMENT_EXPLANATIONS.md`.

- [X] AI insights are visually presented with priority-based highlighting and expandable details
  - Implemented priority labels and expandable recommendation items.

- [X] Weight progress chart displays goal weight as a reference line
  - Implemented in weight chart.

- [X] Charts resize without data loss on mobile devices
  - Trend charts now use responsive + minimum-width SVG containers with horizontal overflow handling to preserve full data visibility on mobile.

- [X] Student can explain impact of missing health data on AI recommendation accuracy
  - Explanation prepared in `REQUIREMENTS_FULFILLMENT_EXPLANATIONS.md`.

- [X] Error messages appear without page reload when API requests fail
  - Implemented frontend API error handling and inline alerts.

- [X] Student can explain approach to preventing API abuse through rate limiting
  - Explanation prepared in `REQUIREMENTS_FULFILLMENT_EXPLANATIONS.md`; per-scope in-memory limiter implemented.

- [X] System blocks rapid-fire API requests from same user
  - Implemented rate limits with 429 + `Retry-After`.

- [X] Health data export includes all historical metrics and timestamps
  - Implemented export endpoint includes account/profile/weights/goals/progress/insights.

- [X] Student can explain tradeoffs of chosen visualization approach/library
  - Explanation prepared in `REQUIREMENTS_FULFILLMENT_EXPLANATIONS.md`.

- [X] Dashboard loads placeholder UI while data is being fetched
  - Implemented skeleton cards and loading states.

- [X] README contains clear project overview, setup instructions, usage guide
  - Updated README now includes all required sections.

- [X] Code is organized and follows backend/frontend best practices reasonably well
  - Layered backend modules + feature-based frontend structure; residual refactor opportunities remain.

#### Extra

- [X] Project runs entirely through Docker with a single command
  - `docker-compose up -d --build`.

- [X] Quality of AI-generated health insights and progress evaluations during testing
  - Improved with guardrails.

- [X] Relevance and practicality of AI-generated weekly/monthly summaries and recommendations
  - Generally good stricter goal-specific phrasing.

- [X] System handles AI service limitations (rate limits, availability)
  - Implemented graceful degradation to cached/fallback output.

- [X] Additional technologies/security/features beyond core requirements
  - Added account linking, consent gating, 2FA QR flow, rate limiting, export, AI guardrails, auth-secret at-rest protection.
