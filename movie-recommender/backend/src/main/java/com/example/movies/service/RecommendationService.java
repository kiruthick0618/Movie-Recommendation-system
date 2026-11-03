package com.example.movies.service;

import com.example.movies.model.Movie;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Content-based recommender.
 * Score = 0.6 * genre similarity (Jaccard) + 0.2 * same director + 0.2 * rating closeness.
 */
@Service
public class RecommendationService {

    private final MovieService movieService;

    public RecommendationService(MovieService movieService) {
        this.movieService = movieService;
    }

    /** Movies similar to a single movie. */
    public List<Movie> similarTo(Movie base, int limit) {
        return movieService.findAll().stream()
                .filter(m -> m.id() != base.id())
                .sorted(Comparator.comparingDouble((Movie m) -> similarity(base, m)).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /** Movies recommended from a set of liked movies and/or preferred genres. */
    public List<Movie> recommend(Collection<Integer> likedIds, Collection<String> preferredGenres, int limit) {
        List<Movie> liked = likedIds.stream()
                .map(movieService::findById)
                .flatMap(java.util.Optional::stream)
                .toList();
        Set<String> genres = new HashSet<>(preferredGenres);

        return movieService.findAll().stream()
                .filter(m -> !likedIds.contains(m.id()))
                .sorted(Comparator.comparingDouble((Movie m) -> score(m, liked, genres)).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private double score(Movie candidate, List<Movie> liked, Set<String> genres) {
        double likedScore = liked.isEmpty() ? 0
                : liked.stream().mapToDouble(l -> similarity(l, candidate)).average().orElse(0);
        double genreScore = 0;
        if (!genres.isEmpty()) {
            long overlap = candidate.genres().stream().filter(genres::contains).count();
            genreScore = (double) overlap / genres.size();
        }
        double base = liked.isEmpty() ? genreScore : (genres.isEmpty() ? likedScore : 0.6 * likedScore + 0.4 * genreScore);
        // small tie-breaker toward highly rated movies
        return base + candidate.rating() / 100.0;
    }

    double similarity(Movie a, Movie b) {
        Set<String> inter = new HashSet<>(a.genres());
        inter.retainAll(b.genres());
        Set<String> union = new HashSet<>(a.genres());
        union.addAll(b.genres());
        double jaccard = union.isEmpty() ? 0 : (double) inter.size() / union.size();
        double sameDirector = a.director().equals(b.director()) ? 1 : 0;
        double ratingCloseness = 1 - Math.min(1, Math.abs(a.rating() - b.rating()) / 5.0);
        return 0.6 * jaccard + 0.2 * sameDirector + 0.2 * ratingCloseness;
    }
}
