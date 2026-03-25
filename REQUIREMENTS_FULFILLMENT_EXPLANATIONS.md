# Requirements Fulfillment Explanations

Use this as oral-defense notes for requirement-focused discussion.

## 1) PII Removal vs Personalization

- We do not send direct identifiers (email, name, auth IDs) to the AI model.
- AI context uses health metrics, goal/progress, activity, and dietary data only.
- Tradeoff: less identity-level personalization, but still strong behavior-level personalization.
- Result: recommendations stay user-relevant without exposing unnecessary personal identifiers.

## 2) Hallucination Detection and Handling Strategy

- We enforce strict JSON schema output from the model.
- Backend validates response structure and content lengths before accepting output.
- We added guardrails for:
  - grounding to known user metrics,
  - rejecting ungrounded percent claims,
  - blocking unsafe/extreme medical wording,
  - avoiding recent recommendation repetition.
- If generation fails validation, system falls back to cached/known-safe output.

## 3) Access Token Duration Security Implications

- Short-lived access tokens reduce blast radius if stolen.
- Refresh tokens enable continuity without forcing frequent re-login.
- In this project, access token lifetime is configured (15 minutes), balancing UX and risk.
- Security rationale: short token + refresh path is standard for web apps.

## 4) Metric Normalization and Visualization Accuracy

- Core health units are standardized in domain model (e.g., kg, cm).
- Consistent units avoid distorted charts and incorrect BMI/wellness calculations.
- If mixed units were allowed without conversion, trend lines and score components would become misleading.

## 5) BMI Classification Effect on Wellness Score

- BMI classification is mapped to a component score.
- Wellness score is weighted: BMI (30%), activity (30%), goal progress (20%), habits (20%).
- BMI changes can shift overall wellness score, but not alone dominate it.
- This keeps score sensitive to multiple behavior factors, not just body composition.

## 6) Why This AI Model / Capabilities

- We use OpenAI model via Responses API for reliable structured output with JSON schema.
- Most important capabilities:
  - consistent structured responses,
  - low integration complexity,
  - acceptable latency for dashboard insights.
- Reliability of formatting mattered more than long-form creativity.

## 7) Caching vs Regeneration

- Caching: return recent insight for same input context hash (stable UX, lower cost/latency).
- Regeneration: happens when context changes or cache is stale.
- Tradeoff: cache improves consistency/cost; regeneration improves freshness/adaptation.

## 8) Context Length and AI Quality

- Too little context => generic recommendations.
- Too much noisy context => diluted relevance and higher token use.
- We include high-value signals (current metrics, trends, goal/progress, activity, dietary context).
- This keeps prompt focused and improves recommendation usefulness.

## 9) Prompt Engineering Approach

- Prompt is structured in sections: demographics, current metrics, trends, activity, dietary, goals, progress, wellness.
- Output task is explicit: exactly 3 recommendations, 1 reflection question, 1 summary.
- Additional strict rules enforce grounding, safe wording, and non-repetition.

## 10) Zero-shot vs Few-shot Tradeoffs

- Current design is mostly zero-shot with strict schema + backend validation.
- Zero-shot benefits: simpler prompt maintenance, lower token cost, easier iteration.
- Few-shot could improve style consistency but increases token usage and maintenance complexity.
- Given project scope, zero-shot + strong validation is the pragmatic choice.

## 11) Restriction-Aware Recommendation Validation

- Current implementation passes dietary restrictions and preferences into AI context.
- Backend also validates generated recommendations against known restrictions before accepting output.
- This is a practical hybrid approach: prompt guidance + deterministic post-generation guard.
- Limitation acknowledged: deeper clinical rule-engine checks can be added later.

## 12) Missing Data Impact on AI Accuracy

- Missing profile/weight/goal context lowers specificity.
- The system degrades gracefully: blocks insight where prerequisite consent/context is missing or falls back safely.
- This avoids misleading pseudo-personalized output when key data is absent.

## 13) Rate Limiting / API Abuse Prevention

- Backend has in-memory scope-based limits (auth, password reset, AI insight endpoints).
- Exceeding limit returns 429 with `Retry-After` for predictable client behavior.
- This protects from rapid-fire abuse and reduces accidental overload.

## 14) Visualization Choices and User Understanding

- Dashboard cards provide quick state (BMI, wellness, goal, AI insight).
- Trends page provides temporal understanding (weight, wellness evolution, component breakdown, heatmap).
- Priority labels and expandable insight details improve scanability without hiding detail.
- Mobile behavior uses responsive layout and horizontal overflow for dense charts.

## 15) Encryption in Transit / At Rest

- Transit: HTTPS is supported via backend SSL configuration (`SERVER_SSL_*`), with local keystore setup documented in README.
- At rest: application-layer encryption is implemented for sensitive health fields.
  - `health_profiles.dietary_preferences`
  - `health_profiles.dietary_restrictions`
  - `health_profiles.fitness_assessment`
  - `weight_entries.note`
  - `activity_checkins.note`
- Tradeoff: application-layer encryption protects sensitive columns in DB dumps but reduces direct queryability on encrypted fields.
