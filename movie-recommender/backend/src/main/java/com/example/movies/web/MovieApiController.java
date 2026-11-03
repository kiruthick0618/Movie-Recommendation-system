package com.example.movies.web;

import com.example.movies.model.Movie;
import com.example.movies.service.MovieService;
import com.example.movies.service.RecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Public, read-only movie endpoints. */
@RestController
@RequestMapping("/api")
public class MovieApiController {

    private final MovieService movies;
    private final RecommendationService recs;

    public MovieApiController(MovieService movies, RecommendationService recs) {
        this.movies = movies;
        this.recs = recs;
    }

    @GetMapping("/movies")
    public List<Movie> search(@RequestParam(required = false) String q, @RequestParam(required = false) String genre) {
        return movies.search(q, genre);
    }

    @GetMapping("/genres")
    public List<String> genres() {
        return movies.allGenres();
    }

    @GetMapping("/movies/{id}")
    public Movie one(@PathVariable int id) {
        return movies.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"));
    }

    @GetMapping("/movies/{id}/similar")
    public List<Movie> similar(@PathVariable int id, @RequestParam(defaultValue = "6") int limit) {
        return recs.similarTo(one(id), Math.max(1, Math.min(limit, 20)));
    }

    @GetMapping("/recommendations")
    public List<Movie> recommend(@RequestParam(name = "liked", required = false) List<Integer> liked,
                                 @RequestParam(name = "genres", required = false) List<String> genres,
                                 @RequestParam(defaultValue = "8") int limit) {
        return recs.recommend(liked == null ? List.of() : liked, genres == null ? List.of() : genres,
                Math.max(1, Math.min(limit, 20)));
    }
}
