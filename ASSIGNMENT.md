# numbers-dont-lie

## The situation 👀

AI is changing how fast we can analyze data across industries. What once took analysts weeks now takes seconds. Look at [MyFitnessPal](https://d3.harvard.edu/platform-rctom/submission/smarter-eating-smarter-fitness-ai-supports-your-goals/) - it started as a simple food tracking app and grew into an AI nutrition guide. [Headspace](https://vorecol.com/blogs/blog-the-role-of-ai-in-personalized-mental-wellness-software-172772) followed the same path, going from a meditation timer to an AI wellness coach.

In this project, you'll build part one of a wellness platform. Your app will collect **user health profiles** (think height, weight, fitness goals), calculate **key health indicators** like BMI and wellness scores, and use AI to  **generate personalized health insights** .
You'll also implement **data visualization** to help users track their metrics, and lay the groundwork for future features like **nutrition planning** and an  **AI health assistant** .

## Functional requirements 📋

### User Health Profile

This is where your wellness journey begins - a profile system that collects and manages user data to power AI-driven insights.
Before diving into implementation, note that this section serves two purposes:

1. Collecting standard user data for platform functionality
2. Gathering specific health/fitness data that will power AI insights

While building user profiles is familiar territory, special attention must be paid to **data structure** and **normalization** for AI processing.

#### Core Account Features:

* User registration with email verification
* Authentication options (email-password, at least 2 OAuth providers)
* Session management with JWT (access/refresh tokens)
* Password reset via email
* Optional two-factor authentication
* Proper input validation with helpful error messages

#### Health Profile Data Collection:

* Basic demographics (age, gender)
* Physical metrics (height, weight)
* Lifestyle indicators (occupation type, activity level)
* Dietary preferences and restrictions
* Fitness goals (weight loss, muscle gain, general fitness, desired activity level)
* Initial fitness assessment:
  * Current weekly activity frequency (0-7 days)
  * Exercise types (cardio, strength, flexibility, sports)
  * Average session duration (15-30min, 30-60min, 60+ min)
  * Self-assessed fitness level (beginner/intermediate/advanced)
  * Preferred exercise environment (home, gym, outdoors)
  * Time of day preference to exercise (morning, afternoon, evening)
  * Current endurance level (can run/walk for X minutes)
  * Basic strength indicators (can do X pushups/squats)
  * ...

#### Profile Management:

* Edit/update health metrics
* View historical changes
* Export personal data including historical metrics
* Privacy settings and data sharing preferences
  * What data can be used
  * What data is visible publicly
  * Email notification preferences

#### AI Data Preparation:

* Key metrics must be [normalized](https://estuary.dev/data-normalization/) before AI processing (e.g., convert all weights to kg, heights to cm)
* Sensitive data must be [anonymized](https://www.imperva.com/learn/data-security/anonymization/#:~:text=Data%20anonymization%20is%20the%20process,an%20individual%20to%20stored%20data.):

  * Remove PII (names, emails)
  * Use unique identifiers
* [Structure data](https://www.kdnuggets.com/guide-data-structures-ai-and-machine-learning) for AI consumption:

  ```
  {
    "user_metrics": {
      "current_state": { "weight": 70, "activity_level": "moderate" },
      "target_state": { "weight": 65, "activity_level": "active" },
      "preferences": ["morning_workouts", "vegetarian", ..],
      "restrictions": ["dairy_free", "gluten_free", ..],
      ..
    }
  }
  ```

  > The provided JSON structure is an example of how processed data should look before AI consumption.
  > Your implementation should:
  >
  > * Define clear data schemas
  > * Include data validation
  > * Handle missing or incomplete data
  > * Support future expansion of data points
  >

#### Data Privacy & Security:

* Clear data usage consents.
* Secure data [transmission](https://www.newsoftwares.net/blog/how-to-secure-data-transmission/) (encryption in transit)
* Proper data storage ([encryption at rest](https://www.freecodecamp.org/news/encryption-at-rest/))

Common pitfalls to avoid:

* Collecting unnecessary data that won't be used for insights
* Missing key metrics that AI will need later
* Storing sensitive data in formats accessible to AI
* Not planning for data model expansion

The effectiveness of your AI recommendations will depend heavily on this data - design your profile system to be comprehensive yet simple to use.

### Health Analytics

Your platform's core - where raw health data transforms into meaningful insights through AI processing. This system calculates essential health metrics and generates personalized recommendations that users can actually act on.

Before implementation, understand that this section has three key components:

1. Standard health calculations that follow established formulas
2. Custom scoring system that weights various health factors
3. AI-driven analysis that turns metrics into actionable insights

#### Core Health Calculations:

* [BMI](https://en.wikipedia.org/wiki/Body_mass_index) processing with classifications (underweight, normal weight, overweight, obese)
* Wellness score (0-100) that factors in:
  * BMI range contribution (30%)
  * Activity level impact (30%)
  * Goal progress status (20%)
  * Health habits (20%)
    * Example formula (each component is normalized to 0-100):
      `score = (bmi_score * 0.3) + (activity_score * 0.3) + (progress_score * 0.2) + (habits_score * 0.2)`
* Weekly and monthly progress tracking (weight changes, activity frequency, achievement of set goals, consistency in habits)
* Goal proximity calculations (distance to target weight, progress in activity level, improvement in fitness metrics)

#### AI-Powered Insights:

> **Getting started with AI**
> The choice of AI model is yours - some excel at detailed analysis (e.g., [GPT-3.5-turbo](https://platform.openai.com/docs/models/gpt-3-5-turbo#gpt-3-5-turbo)), others at quick responses (e.g., [LLAMA-2-7B](https://huggingface.co/meta-llama/Llama-2-7b)), or you could even train your own model for extra challenge. Start with one model to learn [AI integration basics](https://www.rst.software/blog/8-steps-to-successful-ai-integration), then experiment with others to find what best fits your features' needs.

* Structure prompts to generate:

  * Health status analysis based on current metrics
  * Progress evaluations with actionable feedback
  * Custom recommendations ("increase daily steps", "morning stretches", ..) using:
    * User's health profile
    * Progress history
    * Stated goals
  * Weekly health summaries

  Your AI's effectiveness depends on:

  * Clean, normalized data
  * Consistent calculation methods
  * Well-structured historical records
  * Clear context for recommendations
  * Regular validation of outputs
* Progress Management:

  * Track changes in key metrics over time
    * Use time-series data structure
    * Store raw values and calculated metrics
    * Track frequency of updates
    * Note significant changes
  * Monitor goal progress with milestones, for example:
    * Weight: every 5% towards goal
    * Activity: each additional day/week
    * Habits: streak achievements
  * Compare current stats against targets
  * Record activity level changes

This analytics engine will power your platform's personalized insights - ensure your calculations are accurate and your data structures support comprehensive AI analysis.

### Data Visualization

Transform your health analytics into clear, engaging visual [insights](https://wpdatatables.com/chart-js-examples/). Users should be able to understand their progress and health status at a glance, while having access to deeper insights when needed.

This section combines:

1. Real-time data displays showing current status
2. Historical data visualization showing progress
3. AI-enhanced insights presented visually
4. Interactive elements for deeper data exploration

#### Health Dashboard:

* BMI with visual classification
  * Color-coded BMI ranges
  * Visual indicator of current position
  * Quick comparison with healthy range
* Wellness score display
  * Circular gauge with 0-100 scale
  * Color gradient reflecting score ranges
  * Visual breakdown of score components
* Progress towards goals
  * Progress bars for quantifiable goals
  * Milestone markers on timeline
  * Achievement indicators
* Latest AI insights
  * Highlighted key findings
  * Actionable recommendations
  * Quick tips based on recent data

#### Progress Charts:

* Weight tracking over time
  * Chart with trend line
  * Target weight reference line
  * Notable milestone markers
  * Configurable time range
* Wellness score evolution
  * Score progression line
  * Component breakdown area chart
  * Annotation of significant changes
* Activity level changes
  * Weekly activity heatmap
* Goal progress visualization
  * Multiple goal tracking
  * Success rate indicators
  * Projected completion dates

#### Comparison Views:

* Current vs target metrics
  * Side-by-side comparisons
  * Progress percentage
* Weekly/monthly progress comparisons
  * Bar charts for discrete comparisons
* Health trends analysis
  * Trend identification
  * Pattern highlighting
* AI recommendations display
  * Context-aware placement (highlight relevant tips based on current dashboard)
  * Visual priority based on relevance (high, medium and low priority)
  * Interactive exploration options (expandable details)

Common pitfalls to avoid:

* Overwhelming users with too much data
* Poor mobile responsiveness
* Missing loading/error states
* Inadequate color contrast
* Confusing or misleading scales

Design these visualizations to clearly communicate complex health data - your charts and displays should tell a story that users can easily follow.

### Important Considerations ❗

* **[GDPR](https://gdpr-info.eu/) considerations** : As you are handling heatlh data, follow basic GDPR-aligned patterns. This means implementing fundamental security measures like encryption, secure data transmission, and proper access controls. You don't need to make it fully GDPR-compliant, but understanding these principles will serve you well in real-world health applications.
* **Error handling and API reliability** : AI services can be [unpredictable](https://www.quantamagazine.org/the-unpredictable-abilities-emerging-from-large-ai-models-20230316/) - APIs might be temporarily unavailable, responses might time out, or you might hit rate limits. Your app should handle these gracefully, keeping core functionality working and communicating clearly with users about any limitations. No need fo complex retry mechanisms - focus on graceful degradation and clear user communication.
* **Content management** : When generating AI insights and recommendations, consider how to store and track this content over time. Basic versioning helps you understand what recommendations were given and when, which can be valuable for improving your prompts and responses. Maintain enough history to understand your AI's performance.
* **Context in AI interactions** : While generating insights, your AI should have enough context about the user to make meaningful recommendations. Consider what historical data and user preferences need to be included in your prompts. Focus on passing relevant information that helps generate personalized insights.

#### Free-Tier API Realities:

Free API tiers are great for learning and development, but come with natural limitations:

* Rate limits may affect response times
* Usage quotas might require request throttling
* Possible service interruptions
* Response latency variations

When working with AI models, expect:

* Occasional hallucinations or inconsistent responses
* Varying degrees of output accuracy
* Context limitations based on token windows
* Potential response quality variations

These limitations are normal and expected in a development environment. Focus on building core functionality while being aware of these constraints, rather than trying to solve every edge case.

## Useful links 🔗

* [OpenAI Cookbook](https://cookbook.openai.com/)
* [OpenAI Concepts](https://platform.openai.com/docs/concepts)
* [LangChain Tutorials](https://python.langchain.com/docs/tutorials/)
* [Prompt Engineering Guide](https://www.promptingguide.ai/)
* [Open Source Models](https://huggingface.co/docs)
* [AI in Healthcare](https://www.foreseemed.com/artificial-intelligence-in-healthcare)
* [Design a Database for Health and Fitness Tracking Applications](https://www.geeksforgeeks.org/how-to-design-a-database-for-health-and-fitness-tracking-applications/)
* [A Guide To Charts](https://www.tableau.com/chart)
* [GDPR checklist](https://gdpr.eu/checklist/)
* [Docker](https://www.docker.com/)

## Extra requirements 📚

### Dockerization

* **Containerize the project** : use Docker to simplify setup and execution:
* Provide a Dockerfile (or multiple, if the project includes separate frontend and backend components)
* Include a simple startup command or script that builds and runs the entire application with one step
* Docker should be the only requirement, no manual setup, dependency installation, or external tools

## Bonus functionality 🎁

You're welcome to implement other bonuses as you see fit. But anything you implement must not change the default functional behavior of your project.

You may use additional feature flags, command line arguments or separate builds to switch your bonus functionality on.

## What you'll learn 🧠

* **Practical AI Integration** : Integrating AI models into applications, handling API limitations and managing context for optimal responses.
* **Data Architecture for AI** : Designing data structures for AI processing, normalization techniques and systems for tracking AI-generated content.
* **Health Analytics** : Implementing health calculations and scoring systems that combine traditional algorithms with AI-enhanced insights.
* **AI Response Management** : Engineering prompts and managing context for health-related AI interactions while maintaining appropriate boundaries.
* **Privacy-Aware Development** : Implementing AI features while respecting user privacy, including data anonymization and secure handling of health information.

## Deliverables and Review Requirements 📁

* All source code and configuration files
* A README file with:
  * Project overview
  * Setup and installation instructions
  * Usage guide
  * Any additional features or bonus functionality implemented

During the review, be prepared to:

* Demonstrate your platforms's functionality
* Explain your code and design choices
* Discuss any challenges you faced and how you overcame them
