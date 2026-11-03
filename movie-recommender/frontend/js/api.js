// Thin wrapper around the backend REST API. Auth is a Bearer token kept in localStorage.
const Api = (() => {
  const base = window.CINEMATCH_API.replace(/\/$/, "");
  const KEY = "cinematch.session";

  const read = () => { try { return JSON.parse(localStorage.getItem(KEY)) || null; } catch { return null; } };
  let session = read(); // { token, username }

  async function request(path, { method = "GET", body, auth = false } = {}) {
    const headers = {};
    if (body !== undefined) headers["Content-Type"] = "application/json";
    if (session && (auth || path.startsWith("/api/favorites") || path === "/api/auth/me"))
      headers["Authorization"] = "Bearer " + session.token;
    let res;
    try {
      res = await fetch(base + path, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
    } catch {
      throw new Error("Cannot reach the server. Is the backend running on " + base + "?");
    }
    if (res.status === 401 && session && !path.startsWith("/api/auth/login")) api.logout(); // expired token
    const data = await res.json().catch(() => null);
    if (!res.ok) {
      const err = new Error((data && data.error) || "Request failed (" + res.status + ")");
      err.status = res.status;
      throw err;
    }
    return data;
  }

  const qs = params => {
    const u = new URLSearchParams();
    for (const [k, v] of Object.entries(params))
      (Array.isArray(v) ? v : [v]).forEach(x => { if (x !== undefined && x !== null && x !== "") u.append(k, x); });
    const s = u.toString();
    return s ? "?" + s : "";
  };

  const api = {
    get username() { return session ? session.username : null; },
    get loggedIn() { return !!session; },
    onChange: () => {},

    _save(s) {
      session = s;
      if (s) localStorage.setItem(KEY, JSON.stringify(s)); else localStorage.removeItem(KEY);
      api.onChange();
    },
    async login(username, password) {
      const r = await request("/api/auth/login", { method: "POST", body: { username, password } });
      api._save({ token: r.token, username: r.username });
    },
    async register(username, email, password, confirm) {
      await request("/api/auth/register", { method: "POST", body: { username, email, password, confirm } });
    },
    logout() { api._save(null); },

    movies: (q, genre) => request("/api/movies" + qs({ q, genre })),
    genres: () => request("/api/genres"),
    movie: id => request("/api/movies/" + id),
    similar: id => request("/api/movies/" + id + "/similar?limit=6"),
    recommend: (liked, genres) => request("/api/recommendations" + qs({ liked, genres })),

    me: () => request("/api/auth/me"),
    favorites: () => request("/api/favorites"),
    favoriteRecommendations: () => request("/api/favorites/recommendations"),
    toggleFavorite: id => request("/api/favorites/" + id, { method: "POST" }),
  };
  return api;
})();
