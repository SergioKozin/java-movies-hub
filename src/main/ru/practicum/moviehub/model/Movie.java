package ru.practicum.moviehub.model;

public class Movie {


    private final int iD;
    private final String title;
    private final int year;

    public Movie(int iD, String title, int year) {
        this.iD = iD;
        this.title = title;
        this.year = year;
    }

    public int getID() {
        return iD;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }
}