React UI Improvement Suggestions

This document outlines a minimal plan to migrate or enhance the current static UI to a modern React-based frontend and how to integrate it with the Spring Boot backend.

1) Goals
- Provide a component-based UI for maintainability and reactivity.
- Keep the backend API endpoints (`/api/agent/*`) unchanged.
- Make builds output into `src/main/resources/static/` for Spring Boot to serve the app.

2) Suggested Component Structure
- `App` — global layout and routing.
- `Header` — title and description.
- `GoalInput` — textarea, example chips, submit button.
- `Results` — renders trace and final output with status markers.
- `Spinner` / `ErrorBanner` — utility components.

3) UX Improvements
- Add form validation, keyboard shortcuts, and accessible labels.
- Show streamable progress (optimistic UI) and allow cancelling in-flight requests.
- Save recent goals to localStorage and provide example presets.
- Add theme toggle (light/dark) using CSS variables.

4) Build & Integration
- Use Vite to scaffold a small React app in `frontend/`.
- In `package.json` set `build` to produce `dist/`, then copy `dist/*` into `src/main/resources/static/`.
- Example build command (Linux):

```
npm run build
rm -rf src/main/resources/static/*
cp -r dist/* src/main/resources/static/
```

5) Incremental Migration
- Start by building React components that mirror the existing static UI and hook them to `/api/agent/execute`.
- Keep the current `index.html` as a fallback during migration.

6) Accessibility & Testing
- Add basic unit tests for components (Vitest / React Testing Library).
- Run Lighthouse audits and add simple automated checks in CI.

7) Next Steps I Can Do Now
- Scaffold a minimal `frontend/` Vite + React skeleton and add a small `App` component wired to `/api/agent/execute`.
- Add instructions and a `package.json` with `dev` and `build` scripts.

If you want, I can scaffold the minimal frontend now and commit/push/tag the changes.
