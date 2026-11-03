package com.example.movies;

import com.example.movies.model.Movie;
import com.example.movies.service.MovieService;
import com.example.movies.service.RecommendationService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationServiceTest {

    private final MovieService movies = new MovieService();
    private final RecommendationService recs = new RecommendationService(movies);

    @Test
    void similarMoviesExcludeTheMovieItself() {
        Movie inception = movies.findById(4).orElseThrow();
        List<Movie> similar = recs.similarTo(inception, 5);
        assertEquals(5, similar.size());
        assertTrue(similar.stream().noneMatch(m -> m.id() == inception.id()));
    }

    @Test
    void genrePreferenceIsRespected() {
        List<Movie> results = recs.recommend(List.of(), List.of("Animation"), 3);
        assertTrue(results.stream().allMatch(m -> m.genres().contains("Animation")));
    }

    @Test
    void likedMoviesAreNotRecommendedAgain() {
        List<Movie> results = recs.recommend(List.of(4, 5), List.of(), 8);
        assertTrue(results.stream().noneMatch(m -> m.id() == 4 || m.id() == 5));
    }
}
