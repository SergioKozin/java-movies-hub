package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.HashMap;
import java.util.Map;

public class MoviesStore {
    private final Map<Integer, Movie> movies;
    private int ID;

    public MoviesStore() {
        this.movies = new HashMap<>();
        this.ID = 0;
    }

    public int addMovie(String title, int year) {
        movies.put(ID, new Movie(ID, title, year));
        ID++;
        return ID - 1;
    }

    public boolean isEmpty() {
        return movies.isEmpty();
    }

    public Map<Integer, Movie> getMovies() {
        return movies;
    }
}