# Asset Management – Frontend

React SPA for asset management. Communicates with the REST API backend (Spring Boot, port 8080).

## Stack

| Layer | Technology |
|-------|-----------|
| UI framework | React 19, TypeScript 6 |
| Components | MUI 9 (Material UI), Tailwind CSS 4 |
| Server state | TanStack Query 5 (caching, mutations, invalidation) |
| Client state | Zustand 5 (auth store) |
| Forms | React Hook Form 7 + Zod 4 (validation) |
| Routing | React Router 7 |
| HTTP | Axios (with automatic JWT renewal) |
| Build | Vite 8 |
| PDF | @react-pdf-viewer |
| Date | date-fns 4 (Czech locale) |
| Search | Fuse.js 7 (fuzzy search) |

## Structure

```
src/
  components/     -- UI components (card/, dictionary/, report/, ...)
  hooks/
    queries/      -- TanStack Query hooks (useCardQueries, useLocationQueries, ...)
    useNotification.tsx
    useSortableData.ts
  pages/          -- pages (CardPage, ReportPage, inventoryAudit/, ...)
  schemas/        -- Zod validation schemas (cardSchema, dictionarySchema, userSchema)
  services/       -- Axios API calls (cardService, authService, ...)
  store/          -- Zustand store (authStore)
  types/          -- TypeScript types (card/, inventoryAudit/, ...)
  utils/          -- utility functions (authorizedRequest, formatDate, ...)
  theme.ts        -- MUI themes (defaultTheme, dictionaryTheme)
```

## Running

```bash
npm install
npm run dev       # dev server at http://localhost:3000
npm run build     # production build to build/
npm run preview   # preview production build
```

Backend must be running on port 8080. In production, nginx proxies `/api/` to the backend.

## Architecture

- All data is fetched via TanStack Query hooks (`src/hooks/queries/`). No direct service calls from components.
- Forms use React Hook Form with Zod resolver. Schemas are in `src/schemas/`.
- Dictionary entities (class, location, group, ...) share a generic `DictionaryPage<T>` component.
- Pages are lazy-loaded (React.lazy + Suspense). Vite splits vendor code into separate chunks (MUI, PDF, React, Query).
- Auth uses JWT bearer token + httpOnly refresh cookie. The store rehydrates on app startup.
