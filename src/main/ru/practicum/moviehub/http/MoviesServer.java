package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {
    private final MoviesStore moviesStore;
    private HttpServer httpServer;
    private final int httpPort;

    public MoviesServer(MoviesStore moviesStore, int httpPort) {
        this.moviesStore = moviesStore;
        this.httpPort = httpPort;
    }

    public void start() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(httpPort), 0);
            httpServer.createContext("/movies", new BaseHttpHandler(moviesStore, new Gson()));
            httpServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        httpServer.stop(1);
    }
}