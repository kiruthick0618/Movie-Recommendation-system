// CineMatch single-page frontend: hash router + views rendered into #app.
(() => {
  const app = document.getElementById("app");
  let favIds = new Set();
  let renderId = 0;

  const esc = s => String(s).replace(/[&<>"\x27]/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "\x27": "&#39;" }[c]));
  const hue = id => (id * 47) % 360;
  const toast = msg => {
    let t = document.getElementById("toast");
    if (!t) { t = document.createElement("div"); t.id = "toast"; document.body.appendChild(t); }
    t.textContent = msg; t.classList.add("show");
    clearTimeout(t._h); t._h = setTimeout(() => t.classList.remove("show"), 1800);
  };

  // ---------- shared pieces ----------
  const nav = () => `
    <header class="nav">
      <a class="brand" href="#/">Cine<span>Match</span></a>
      <nav>
        <a href="#/">Browse</a>
        <a href="#/recommend">For You</a>
        <a href="#/favorites">Favourites</a>
        ${Api.loggedIn
          ? `<span class="user">${esc(Api.username)}</span><button class="link-btn" id="signout" type="button">Sign out</button>`
          : `<a href="#/login">Sign in</a>`}
      </nav>
    </header>`;

  const footer = `<footer class="footer">CineMatch · Spring Boot API + static frontend</footer>`;
  const page = body => `${nav()}<main class="container">${body}</main>${footer}`;

  const card = m => `
    <div class="card">
      ${Api.loggedIn ? `<button type="button" class="fav${favIds.has(m.id) ? " on" : ""}" data-id="${m.id}" aria-label="Toggle favourite">♥</button>` : ""}
      <a class="card-link" href="#/movie/${m.id}">
        <div class="poster" style="--h:${hue(m.id)}">
          <span class="poster-title">${esc(m.title)}</span>
          <span class="poster-year">${m.year}</span>
        </div>
        <div class="card-body">
          <h3>${esc(m.title)}</h3>
          <p class="meta"><span class="rating">★ ${m.rating.toFixed(1)}</span> <span>${esc(m.genres.join(" · "))}</span></p>
        </div>
      </a>
    </div>`;

  const grid = list => `<section class="grid">${list.map(card).join("")}</section>`;
  const errorPage = msg => page(`<p class="empty">${esc(msg)}</p>`);

  // ---------- views ----------
  async function home(q) {
    const search = q.get("q") || "", genre = q.get("genre") || "";
    const [movies, genres] = await Promise.all([Api.movies(search, genre), Api.genres()]);
    return page(`
      <section class="hero">
        <h1>Find your next favourite movie</h1>
        <p>Browse the catalog, or tell us what you like and get personalised picks.</p>
        <a class="btn" href="#/recommend">Get Recommendations</a>
      </section>
      <form class="filters" id="filters">
        <input type="search" name="q" value="${esc(search)}" placeholder="Search title or director…">
        <select name="genre">
          <option value="">All genres</option>
          ${genres.map(g => `<option value="${esc(g)}"${g === genre ? " selected" : ""}>${esc(g)}</option>`).join("")}
        </select>
        <button class="btn" type="submit">Filter</button>
      </form>
      ${movies.length ? "" : `<p class="empty">No movies match your search.</p>`}
      ${grid(movies)}`);
  }

  async function detail(id) {
    const [m, similar] = await Promise.all([Api.movie(id), Api.similar(id)]);
    return page(`
      <a class="back" href="#/">← Back to browse</a>
      <section class="detail">
        <div class="poster big" style="--h:${hue(m.id)}">
          <span class="poster-title">${esc(m.title)}</span>
          <span class="poster-year">${m.year}</span>
        </div>
        <div class="detail-info">
          <h1>${esc(m.title)}</h1>
          <p class="meta"><span class="rating">★ ${m.rating.toFixed(1)}</span><span>${m.year}</span><span>${m.runtimeMinutes} min</span></p>
          <p class="tags">${m.genres.map(g => `<a class="tag" href="#/?genre=${encodeURIComponent(g)}">${esc(g)}</a>`).join("")}</p>
          <p>${esc(m.overview)}</p>
          ${Api.loggedIn ? `<button type="button" class="fav inline-fav${favIds.has(m.id) ? " on" : ""}" data-id="${m.id}">♥ Favourite</button>` : ""}
          <p class="director">Directed by <strong>${esc(m.director)}</strong></p>
        </div>
      </section>
      <h2>Because you\x27re viewing this</h2>
      ${grid(similar)}`);
  }

  async function recommend(q) {
    const liked = q.getAll("liked").map(Number), picked = q.getAll("genres");
    const submitted = liked.length > 0 || picked.length > 0;
    const [genres, all] = await Promise.all([Api.genres(), Api.movies()]);
    let favResults = null, results = null;
    if (submitted) results = await Api.recommend(liked, picked);
    else if (Api.loggedIn) { const r = await Api.favoriteRecommendations(); if (r.length) favResults = r; }
    return page(`
      <h1>Get recommendations</h1>
      <p class="muted">Pick genres you enjoy and/or movies you\x27ve liked. We\x27ll rank the rest of the catalog for you.</p>
      <form class="rec-form" id="recform">
        <fieldset>
          <legend>Favourite genres</legend>
          ${genres.map(g => `<label class="chip"><input type="checkbox" name="genres" value="${esc(g)}"${picked.includes(g) ? " checked" : ""}><span>${esc(g)}</span></label>`).join("")}
        </fieldset>
        <fieldset>
          <legend>Movies you liked</legend>
          ${all.map(m => `<label class="chip"><input type="checkbox" name="liked" value="${m.id}"${liked.includes(m.id) ? " checked" : ""}><span>${esc(m.title)}</span></label>`).join("")}
        </fieldset>
        <button class="btn" type="submit">Recommend</button>
        <a class="btn ghost" href="#/recommend">Reset</a>
      </form>
      ${favResults ? `<section><h2>Picked from your favourites</h2>${grid(favResults)}</section>` : ""}
      ${submitted ? `<section><h2>Recommended for you</h2>${grid(results)}</section>` : ""}`);
  }

  async function favorites() {
    if (!Api.loggedIn) { location.hash = "#/login"; return null; }
    const list = await Api.favorites();
    return page(`
      <h1>Your favourites</h1>
      ${list.length ? "" : `<p class="empty">Nothing saved yet. Tap the ♥ on any movie to add it.</p>`}
      ${grid(list)}`);
  }

  const authShell = (heading, art, inner) => `
    <div class="auth-body"><main class="auth-wrap">
      <section class="auth-art">
        <a class="brand big-brand" href="#/">Cine<span>Match</span></a>
        <h2>${heading}</h2><p>${art}</p>
      </section>
      <section class="auth-card">${inner}</section>
    </main></div>`;

  function login(q) {
    return authShell("Your personal movie night, curated.", "Save favourites and get recommendations tuned to your taste.", `
      <h1>Welcome back</h1>
      <p class="muted">Sign in to continue.</p>
      ${q.has("registered") ? `<div class="alert ok">Account created. Please sign in.</div>` : ""}
      ${q.has("logout") ? `<div class="alert ok">You have been signed out.</div>` : ""}
      <div class="alert error" id="formerr" hidden></div>
      <form id="loginform" class="auth-form">
        <label>Username <input type="text" name="username" required autofocus autocomplete="username"></label>
        <label>Password <input type="password" name="password" required autocomplete="current-password"></label>
        <button class="btn wide" type="submit">Sign in</button>
      </form>
      <p class="switch">New to CineMatch? <a href="#/register">Create an account</a></p>`);
  }

  function register() {
    return authShell("Join in under a minute.", "Build your watchlist and let the recommender do the rest.", `
      <h1>Create account</h1>
      <div class="alert error" id="formerr" hidden></div>
      <form id="regform" class="auth-form">
        <label>Username <input type="text" name="username" required minlength="3" maxlength="20" pattern="[A-Za-z0-9_]+" autocomplete="username"></label>
        <label>Email <input type="email" name="email" required autocomplete="email"></label>
        <label>Password (min 8 characters) <input type="password" name="password" required minlength="8" autocomplete="new-password"></label>
        <label>Confirm password <input type="password" name="confirm" required minlength="8" autocomplete="new-password"></label>
        <button class="btn wide" type="submit">Create account</button>
      </form>
      <p class="switch">Already registered? <a href="#/login">Sign in</a></p>`);
  }

  // ---------- router ----------
  async function route() {
    const my = ++renderId;
    const raw = location.hash.replace(/^#/, "") || "/";
    const [path, query = ""] = raw.split("?");
    const q = new URLSearchParams(query);
    let html;
    try {
      if (Api.loggedIn && favIds.size === 0 && !route.loaded) {
        route.loaded = true;
        try { favIds = new Set((await Api.me()).favorites); } catch { /* token expired: Api logs out */ }
      }
      let m;
      if (path === "/") html = await home(q);
      else if ((m = path.match(/^\/movie\/(\d+)$/))) html = await detail(m[1]);
      else if (path === "/recommend") html = await recommend(q);
      else if (path === "/favorites") html = await favorites();
      else if (path === "/login") html = login(q);
      else if (path === "/register") html = register();
      else html = errorPage("Page not found.");
    } catch (e) {
      html = e.status === 404 ? errorPage("Not found.") : errorPage(e.message);
    }
    if (my !== renderId || html === null) return; // a newer navigation won
    app.innerHTML = html;
    window.scrollTo(0, 0);
    document.title = "CineMatch";
  }

  // ---------- events ----------
  document.addEventListener("click", async e => {
    if (e.target.closest("#signout")) { Api.logout(); location.hash = "#/login?logout"; return; }

    const btn = e.target.closest("button.fav");
    if (!btn) return;
    const id = Number(btn.dataset.id);
    btn.disabled = true;
    try {
      const r = await Api.toggleFavorite(id);
      r.favorite ? favIds.add(id) : favIds.delete(id);
      document.querySelectorAll(`button.fav[data-id="${id}"]`).forEach(b => b.classList.toggle("on", r.favorite));
      if (!r.favorite && location.hash.startsWith("#/favorites")) btn.closest(".card")?.remove();
      toast(r.favorite ? "Added to favourites" : "Removed from favourites");
    } catch (err) {
      if (err.status === 401) location.hash = "#/login"; else toast("Something went wrong. Please try again.");
    } finally { btn.disabled = false; }
  });

  document.addEventListener("submit", async e => {
    const f = e.target;
    const data = Object.fromEntries(new FormData(f));
    const showErr = msg => { const el = document.getElementById("formerr"); if (el) { el.textContent = msg; el.hidden = false; } };

    if (f.id === "filters") {
      e.preventDefault();
      const p = new URLSearchParams();
      if (data.q) p.set("q", data.q);
      if (data.genre) p.set("genre", data.genre);
      location.hash = "#/" + (p.toString() ? "?" + p : "");
    } else if (f.id === "recform") {
      e.preventDefault();
      const p = new URLSearchParams();
      new FormData(f).forEach((v, k) => p.append(k, v));
      const target = "#/recommend" + (p.toString() ? "?" + p : "");
      if (location.hash === target) route(); else location.hash = target;
    } else if (f.id === "loginform") {
      e.preventDefault();
      try {
        await Api.login(data.username, data.password);
        favIds = new Set(); route.loaded = false;
        location.hash = "#/";
      } catch (err) { showErr(err.message); }
    } else if (f.id === "regform") {
      e.preventDefault();
      if (data.password !== data.confirm) return showErr("Passwords do not match.");
      try { await Api.register(data.username, data.email, data.password, data.confirm); location.hash = "#/login?registered"; }
      catch (err) { showErr(err.message); }
    }
  });

  Api.onChange = () => { if (!Api.loggedIn) { favIds = new Set(); route.loaded = false; } };
  window.addEventListener("hashchange", route);
  route();
})();
