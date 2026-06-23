# AI Interview Assistant — Frontend

React 18 single-page app. Communicates with the Spring Boot backend via REST.

## Stack

- React 18 + React Router v6
- Plain CSS with CSS variables (no Tailwind, no component libraries)
- Google Fonts: Inter (body) + Space Grotesk (display)

## Pages

| Route | Page | Description |
|-------|------|-------------|
| `/login` | AuthPage | Register / login. JWT stored in localStorage. |
| `/dashboard` | Dashboard | Stats, interview history list. |
| `/create` | CreateInterview | Role input, difficulty picker, question count slider. |
| `/interview/:id` | InterviewSession | Question navigator, answer textarea, progress bar. |
| `/scorecard/:id` | Scorecard | Score ring, strengths/weaknesses, per-question feedback. |

## Setup

```bash
cd frontend
npm install
npm start
```

The app proxies API calls to `http://localhost:8080` (configured in `package.json`).
Make sure your Spring Boot backend is running first.

For production, build the static files:

```bash
npm run build
```

Then serve the `build/` folder from your backend or a static host (S3, Nginx, etc.).
