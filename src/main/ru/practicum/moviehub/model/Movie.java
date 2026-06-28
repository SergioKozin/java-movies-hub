package ru.practicum.moviehub.model;

public class Movie {


    private final int ID;
    private final String title;
    private final int year;

    public Movie(int ID, String title, int year) {
        this.ID = ID;
        this.title = title;
        this.year = year;
    }

    public int getID() {
        return ID;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }
}