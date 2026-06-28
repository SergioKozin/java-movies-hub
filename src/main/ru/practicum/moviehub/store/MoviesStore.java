package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.HashMap;
import java.util.Map;

public class MoviesStore {
    private final Map<Integer, Movie> movies;
    private int iD;

    public MoviesStore() {
        this.movies = new HashMap<>();
        this.iD = 0;
    }

    public int addMovie(String title, int year) {
        movies.put(iD, new Movie(iD, title, year));
        iD++;
        return iD - 1;
    }

    public boolean isEmpty() {
        return movies.isEmpty();
    }

    public Map<Integer, Movie> getMovies() {
        return movies;
    }
}