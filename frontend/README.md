# Evidence majetku -- frontend

React SPA pro evidenci dlouhodobého majetku. Komunikuje s REST API backendem (Spring Boot, port 8080).

## Stack

| Vrstva | Technologie |
|--------|-------------|
| UI framework | React 19, TypeScript 5.9 |
| Komponenty | MUI 7 (Material UI), Tailwind CSS 3 |
| Stav serveru | TanStack Query 5 (caching, mutace, invalidace) |
| Stav klienta | Zustand 5 (auth store) |
| Formuláře | React Hook Form 7 + Zod 4 (validace) |
| Routing | React Router 6 |
| HTTP | Axios (s automatickou obnovou JWT) |
| Build | Vite 7 |
| PDF | @react-pdf-viewer |
| Datum | date-fns 4 (česká lokalizace) |
| Hledání | Fuse.js 7 (fuzzy search) |

## Struktura

```
src/
  components/     -- UI komponenty (card/, dictionary/, report/, ...)
  hooks/
    queries/      -- TanStack Query hooky (useCardQueries, useLocationQueries, ...)
    useNotification.tsx
    useSortableData.ts
  pages/          -- stránky (CardPage, ReportPage, inventoryAudit/, ...)
  schemas/        -- Zod validační schémata (cardSchema, dictionarySchema, userSchema)
  services/       -- Axios volání API (cardService, authService, ...)
  store/          -- Zustand store (authStore)
  types/          -- TypeScript typy (card/, inventoryAudit/, ...)
  utils/          -- pomocné funkce (authorizedRequest, formatDate, ...)
  theme.ts        -- MUI témata (defaultTheme, dictionaryTheme)
```

## Spuštění

```bash
npm install
npm run dev       # vývojový server na http://localhost:3000
npm run build     # produkční build do build/
npm run preview   # náhled produkčního buildu
```

Backend musí běžet na portu 8080. V produkci nginx proxy směruje `/api/` na backend.

## Architektura

- Všechna data se načítají přes TanStack Query hooky (`src/hooks/queries/`). Žádné přímé volání servisů z komponent.
- Formuláře používají React Hook Form s Zod resolverem. Schémata jsou v `src/schemas/`.
- Slovníkové entity (třída, lokalita, skupina, ...) sdílí generickou `DictionaryPage<T>` komponentu.
- Stránky se načítají lazy (React.lazy + Suspense). Vite rozděluje vendor kód do samostatných chunků (MUI, PDF, React, Query).
- Auth používá JWT bearer token + httpOnly refresh cookie. Store se rehydratuje při startu aplikace.
