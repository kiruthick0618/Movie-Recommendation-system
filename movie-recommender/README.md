# CineMatch – Movie Recommendation Website

```
movie-recommender/
├── index.html            ← open this to use the website
├── frontend/             ← static site (no build step)
│   ├── css/style.css
│   └── js/ config.js · api.js · app.js
└── backend/              ← Spring Boot REST API (Java 17+, Maven)
    ├── pom.xml
    └── src/…
```

## Run

1. **Start the backend** (API on http://localhost:8080):
   ```bash
   cd backend
   mvn spring-boot:run
   ```
2. **Open the website**: double-click `index.html` (or serve the folder, e.g. `python3 -m http.server 3000` and visit http://localhost:3000).

The frontend finds the API through `frontend/js/config.js` (`window.CINEMATCH_API`). Change it if the backend runs elsewhere.

Tests: `cd backend && mvn test`

## How it fits together

- Frontend: a single-page app. `index.html` loads the scripts, which render pages into `#app` using hash routes (`#/`, `#/movie/4`, `#/recommend`, `#/favorites`, `#/login`, `#/register`).
- Backend: JSON API only. Users + favourites live in an H2 file DB (`backend/data/`). Passwords are BCrypt-hashed. Login returns a signed JWT that the frontend sends as `Authorization: Bearer …`. CORS is open so the page works from `file://` or any port.
- Set `APP_JWT_SECRET` (32+ chars) in production.

## API

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | – | `{username,email,password,confirm}` |
| POST | `/api/auth/login` | – | `{username,password}` → `{token,username}` |
| GET | `/api/auth/me` | ✔ | username + favourite ids |
| GET | `/api/movies?q=&genre=` | – | search / filter |
| GET | `/api/movies/{id}` , `/api/movies/{id}/similar` | – | details, similar movies |
| GET | `/api/genres` | – | all genres |
| GET | `/api/recommendations?liked=1&genres=Drama` | – | ranked picks |
| GET | `/api/favorites` , `/api/favorites/recommendations` | ✔ | your favourites / picks from them |
| POST | `/api/favorites/{id}` | ✔ | toggle favourite |

## How recommendations work
Content-based filtering (`RecommendationService`):
`similarity = 0.6 × genre overlap (Jaccard) + 0.2 × same director + 0.2 × rating closeness`.
Candidates are scored by average similarity to liked movies, blended with genre match and a small rating bonus.
