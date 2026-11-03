package com.example.movies.web;

import com.example.movies.model.AppUser;
import com.example.movies.model.Movie;
import com.example.movies.repo.UserRepository;
import com.example.movies.service.MovieService;
import com.example.movies.service.RecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/** Per-user favourites. All endpoints require a valid token. */
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final UserRepository users;
    private final MovieService movies;
    private final RecommendationService recs;

    public FavoriteController(UserRepository users, MovieService movies, RecommendationService recs) {
        this.users = users;
        this.movies = movies;
        this.recs = recs;
    }

    private AppUser user(Principal p) {
        return users.findByUsernameIgnoreCase(p.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<Movie> list(Principal principal) {
        var favs = user(principal).getFavorites();
        return movies.findAll().stream().filter(m -> favs.contains(m.id())).toList();
    }

    @GetMapping("/recommendations")
    @Transactional(readOnly = true)
    public List<Movie> recommendations(Principal principal) {
        var favs = user(principal).getFavorites();
        return favs.isEmpty() ? List.of() : recs.recommend(favs, List.of(), 8);
    }

    @PostMapping("/{id}")
    @Transactional
    public Map<String, Object> toggle(@PathVariable int id, Principal principal) {
        movies.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"));
        AppUser user = user(principal);
        boolean nowFavorite = !user.getFavorites().remove(id);
        if (nowFavorite) user.getFavorites().add(id);
        users.save(user);
        return Map.of("favorite", nowFavorite, "count", user.getFavorites().size());
    }
}
