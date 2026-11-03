package com.example.movies.model;

import java.util.List;

public record Movie(
        int id,
        String title,
        int year,
        double rating,
        int runtimeMinutes,
        String director,
        List<String> genres,
        String overview) {

    public String genresLabel() {
        return String.join(" · ", genres);
    }
}
