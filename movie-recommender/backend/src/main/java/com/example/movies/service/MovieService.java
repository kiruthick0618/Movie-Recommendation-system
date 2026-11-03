package com.example.movies.service;

import com.example.movies.model.Movie;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** In-memory movie catalog. Swap this for a database repository later if needed. */
@Service
public class MovieService {

    private final List<Movie> movies = List.of(
            new Movie(1, "The Shawshank Redemption", 1994, 9.3, 142, "Frank Darabont",
                    List.of("Drama"), "Two imprisoned men bond over years, finding solace and eventual redemption through acts of common decency."),
            new Movie(2, "The Godfather", 1972, 9.2, 175, "Francis Ford Coppola",
                    List.of("Crime", "Drama"), "The aging patriarch of a crime dynasty transfers control of his empire to his reluctant son."),
            new Movie(3, "The Dark Knight", 2008, 9.0, 152, "Christopher Nolan",
                    List.of("Action", "Crime", "Drama"), "Batman faces the Joker, a criminal mastermind who plunges Gotham into chaos."),
            new Movie(4, "Inception", 2010, 8.8, 148, "Christopher Nolan",
                    List.of("Action", "Sci-Fi", "Thriller"), "A thief who steals secrets through dream-sharing is offered a chance to have his past erased."),
            new Movie(5, "Interstellar", 2014, 8.7, 169, "Christopher Nolan",
                    List.of("Adventure", "Drama", "Sci-Fi"), "Explorers travel through a wormhole in space to ensure humanity's survival."),
            new Movie(6, "The Matrix", 1999, 8.7, 136, "Lana & Lilly Wachowski",
                    List.of("Action", "Sci-Fi"), "A hacker discovers reality is a simulation and joins a rebellion against its controllers."),
            new Movie(7, "Pulp Fiction", 1994, 8.9, 154, "Quentin Tarantino",
                    List.of("Crime", "Drama"), "Interwoven tales of hitmen, a boxer and a gangster's wife in Los Angeles."),
            new Movie(8, "Forrest Gump", 1994, 8.8, 142, "Robert Zemeckis",
                    List.of("Drama", "Romance"), "A kind-hearted man witnesses and unwittingly influences several defining events of the 20th century."),
            new Movie(9, "Fight Club", 1999, 8.8, 139, "David Fincher",
                    List.of("Drama", "Thriller"), "An insomniac office worker and a soap maker form an underground fight club."),
            new Movie(10, "Gladiator", 2000, 8.5, 155, "Ridley Scott",
                    List.of("Action", "Adventure", "Drama"), "A betrayed Roman general seeks vengeance against the emperor who murdered his family."),
            new Movie(11, "The Lord of the Rings: The Fellowship of the Ring", 2001, 8.8, 178, "Peter Jackson",
                    List.of("Adventure", "Fantasy", "Action"), "A hobbit and a fellowship set out to destroy a ring of unimaginable power."),
            new Movie(12, "Toy Story", 1995, 8.3, 81, "John Lasseter",
                    List.of("Animation", "Adventure", "Comedy"), "A cowboy doll is threatened when a new spaceman toy becomes the boy's favorite."),
            new Movie(13, "Spirited Away", 2001, 8.6, 125, "Hayao Miyazaki",
                    List.of("Animation", "Adventure", "Fantasy"), "A girl wanders into a world of spirits and must work to free her parents."),
            new Movie(14, "The Silence of the Lambs", 1991, 8.6, 118, "Jonathan Demme",
                    List.of("Crime", "Thriller", "Drama"), "A young FBI cadet seeks help from an imprisoned cannibal to catch another killer."),
            new Movie(15, "Parasite", 2019, 8.5, 132, "Bong Joon-ho",
                    List.of("Thriller", "Drama", "Comedy"), "A poor family schemes to become employed by a wealthy household, with unexpected consequences."),
            new Movie(16, "Mad Max: Fury Road", 2015, 8.1, 120, "George Miller",
                    List.of("Action", "Adventure", "Sci-Fi"), "In a wasteland, a drifter and a rebel warrior flee a tyrant across the desert."),
            new Movie(17, "La La Land", 2016, 8.0, 128, "Damien Chazelle",
                    List.of("Comedy", "Drama", "Romance", "Musical"), "A jazz pianist and an aspiring actress fall in love while chasing their dreams in Los Angeles."),
            new Movie(18, "Whiplash", 2014, 8.5, 106, "Damien Chazelle",
                    List.of("Drama", "Musical"), "A young drummer enrolls at a cutthroat music conservatory under a ruthless instructor."),
            new Movie(19, "Get Out", 2017, 7.8, 104, "Jordan Peele",
                    List.of("Horror", "Thriller", "Mystery"), "A young man visits his girlfriend's family estate and uncovers a disturbing secret."),
            new Movie(20, "Knives Out", 2019, 7.9, 130, "Rian Johnson",
                    List.of("Comedy", "Crime", "Mystery"), "A detective investigates the death of a patriarch in a wealthy, dysfunctional family."),
            new Movie(21, "Blade Runner 2049", 2017, 8.0, 164, "Denis Villeneuve",
                    List.of("Sci-Fi", "Drama", "Thriller"), "A young blade runner's discovery of a long-buried secret leads him to track down a former blade runner."),
            new Movie(22, "Dune", 2021, 8.0, 155, "Denis Villeneuve",
                    List.of("Adventure", "Sci-Fi", "Drama"), "A noble family becomes embroiled in a war over control of the galaxy's most valuable resource."),
            new Movie(23, "Coco", 2017, 8.4, 105, "Lee Unkrich",
                    List.of("Animation", "Adventure", "Musical", "Family"), "An aspiring musician journeys to the Land of the Dead to unlock his family's history."),
            new Movie(24, "The Grand Budapest Hotel", 2014, 8.1, 99, "Wes Anderson",
                    List.of("Comedy", "Adventure", "Crime"), "A legendary concierge and his lobby boy become entangled in the theft of a priceless painting."),
            new Movie(25, "Se7en", 1995, 8.6, 127, "David Fincher",
                    List.of("Crime", "Mystery", "Thriller"), "Two detectives hunt a serial killer who uses the seven deadly sins as motives."),
            new Movie(26, "The Notebook", 2004, 7.8, 123, "Nick Cassavetes",
                    List.of("Romance", "Drama"), "A poor yet passionate young man falls in love with a rich young woman."),
            new Movie(27, "The Conjuring", 2013, 7.5, 112, "James Wan",
                    List.of("Horror", "Mystery", "Thriller"), "Paranormal investigators help a family terrorized by a dark presence in their farmhouse."),
            new Movie(28, "Spider-Man: Into the Spider-Verse", 2018, 8.4, 117, "Bob Persichetti",
                    List.of("Animation", "Action", "Adventure", "Family"), "Teen Miles Morales becomes Spider-Man and meets counterparts from other dimensions."),
            new Movie(29, "3 Idiots", 2009, 8.4, 170, "Rajkumar Hirani",
                    List.of("Comedy", "Drama"), "Two friends search for their long-lost companion and recall their college days challenging the education system."),
            new Movie(30, "Dangal", 2016, 8.3, 161, "Nitesh Tiwari",
                    List.of("Action", "Drama", "Sport"), "A former wrestler trains his daughters to become world-class wrestlers.")
    );

    public List<Movie> findAll() {
        return movies.stream()
                .sorted(Comparator.comparingDouble(Movie::rating).reversed())
                .collect(Collectors.toList());
    }

    public Optional<Movie> findById(int id) {
        return movies.stream().filter(m -> m.id() == id).findFirst();
    }

    public List<String> allGenres() {
        return movies.stream()
                .flatMap(m -> m.genres().stream())
                .collect(Collectors.toCollection(TreeSet::new))
                .stream().toList();
    }

    public List<Movie> search(String query, String genre) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        boolean hasGenre = genre != null && !genre.isBlank();
        return findAll().stream()
                .filter(m -> q.isEmpty()
                        || m.title().toLowerCase(Locale.ROOT).contains(q)
                        || m.director().toLowerCase(Locale.ROOT).contains(q))
                .filter(m -> !hasGenre || m.genres().contains(genre))
                .collect(Collectors.toList());
    }
}
