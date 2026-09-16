# QuestiFy — AI Powered Question Paper Generation System (Frontend)

React frontend for QuestiFy: CO and Bloom (K1–K6) mapped question paper generation,
repetition checking, evaluation scheme drafting and multi-level approvals for colleges
and universities.

## Technology

**Frontend (this repository)**

- React 18
- Vite
- JavaScript + JSX (no TypeScript)
- React Router
- Axios (REST API communication)
- Plain CSS, ported from the QuestiFy design

**Backend (separate repository)**

- Java 21
- Spring Boot 4
- Spring Web, Spring Data JPA, Hibernate
- H2 Database
- Maven
- Microservice architecture

## Frontend / backend relationship

```
React frontend
      ↓  REST (HTTPS/JSON)
API gateway
      ↓
Spring Boot microservices
      ↓
Spring Data JPA / Hibernate
      ↓
H2 database
```

The frontend never talks to the database directly. All data access happens through
REST endpoints exposed by the Spring Boot services. Every network call goes through
the service layer in `src/services/`; components never call `fetch`/`axios` directly.

Endpoint paths are declared in `src/config/api.js` so they can be aligned with the
backend API contract in one place.

## Installation

```bash
npm install
```

## Environment

```bash
cp .env.example .env
```

Then set the backend API gateway URL:

```
VITE_API_BASE_URL=<backend-api-url>
```

No secrets (database passwords, JWT secrets, API keys) belong in this project — the
frontend only stores the public API base URL.

## Development

```bash
npm run dev
```

## Production build

```bash
npm run build
```

## Preview the production build

```bash
npm run preview
```

## Project structure

```
questify-frontend/
├── public/                  static files (favicon)
├── src/
│   ├── assets/              image/media assets
│   ├── components/
│   │   ├── common/          BrandMark, PageHeader, Loading/Empty/Error states, Reveal
│   │   ├── landing/         landing page sections
│   │   └── layout/          SiteHeader, SiteFooter, Sidebar, Topbar
│   ├── config/              api.js (base URL + endpoints), navigation.js (role menus)
│   ├── context/             AuthContext.jsx
│   ├── hooks/
│   ├── layouts/             PublicLayout, AuthLayout, MainLayout
│   ├── pages/               LandingPage, LoginPage, DashboardPage, ...
│   ├── routes/              AppRoutes.jsx, ProtectedRoute.jsx
│   ├── services/            api.js, authService.js
│   ├── styles/              global.css, landing.css, auth.css, workspace.css
│   ├── utils/               roles.js, validation.js, errors.js
│   ├── App.jsx
│   └── main.jsx
├── .env.example
├── index.html
├── package.json
└── vite.config.js
```

## Roles

QuestiFy is role driven. Roles are resolved from the authenticated user returned by the
backend and drive both sidebar navigation (`src/config/navigation.js`) and route access
(`src/routes/ProtectedRoute.jsx`):

`super-admin`, `institution-admin`, `faculty`, `hod`, `reviewer`, `course-coordinator`

## Authentication

Authentication is performed by the backend. `authService` posts credentials to the
auth endpoint, `AuthContext` stores the returned user (and token, if the backend issues
one) and `ProtectedRoute` guards the workspace routes. No credentials are hardcoded and
no authentication is simulated in the frontend. When the backend's exact mechanism is
confirmed (bearer token, session cookie or other), only `src/services/api.js`,
`src/services/authService.js` and `src/config/api.js` need updating.

## Implementation status

- **Phase 1 (complete)** — project setup, design system, landing page, login page,
  routing, role-aware workspace shell (sidebar, topbar, layouts), protected routes,
  centralized API/service layer, loading / empty / error / 404 / 403 states.
- **Phases 2–6 (pending)** — dashboard data, question bank, question generator, paper
  generation, history, user management and the remaining role consoles, each wired to
  its backend service.

Screens that are scheduled for a later phase render inside the real workspace shell and
state clearly that they are not implemented yet — no mock data is used anywhere.

## Required backend endpoints

Currently referenced (see `src/config/api.js`):

- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET  /api/auth/me`

Additional endpoints (question bank, generation, papers, approvals, users, analytics)
will be added to the service layer as each phase is implemented against the real
backend contract.
