package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BaseHttpHandler implements HttpHandler {
    private final MoviesStore moviesStore;
    private final Gson gson;
    private final static int HTTP_CODE_OK = 200;
    private final static int HTTP_CODE_CREATED = 201;
    private final static int HTTP_CODE_NO_CONTENT = 204;
    private final static int HTTP_CODE_BAD_REQUEST = 400;
    private final static int HTTP_CODE_NOT_FOUND = 404;
    private final static int HTTP_CODE_METHOD_NOT_ALLOWED = 405;
    private final static int HTTP_CODE_UNSUPPORTED_MEDIA_TYPE = 415;
    private final static int HTTP_CODE_UNPROCESSABLE_ENTITY = 422;
    private final static int MAX_TITLE_LENGTH = 100;
    private final static int MIN_YEAR = 1888;
    private final static int MAX_YEAR = 2027;
    private final static String HEADER_CONTENT_TYPE = "Content-type";
    private final static String MEDIA_TYPE = "application/json; charset=utf-8";
    private int responseCode = 0;
    String jsonString;

    public BaseHttpHandler(MoviesStore moviesStore, Gson gson) {
        this.moviesStore = moviesStore;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();

        switch (method) {
            case "GET":
                getMethodHandler(httpExchange);
                break;
            case "POST":
                postMethodHandler(httpExchange);
                break;
            case "DELETE":
                deleteMethodHandler(httpExchange);
                break;
            default:
                jsonString = gson.toJson(
                        new ErrorResponse("Неподдерживаемый HTTP-метод.", ""));
                responseCode = HTTP_CODE_METHOD_NOT_ALLOWED;
        }
        Headers headers = httpExchange.getResponseHeaders();
        headers.set(HEADER_CONTENT_TYPE, MEDIA_TYPE);
        httpExchange.sendResponseHeaders(responseCode, 0);
        try (OutputStream outputStream = httpExchange.getResponseBody()) {
            outputStream.write(jsonString.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void getMethodHandler(HttpExchange httpExchange) {
        URI requestURI = httpExchange.getRequestURI();
        String path = requestURI.getPath();
        String[] splitStrings = path.split("/");
        if (splitStrings.length <= 2) {

            if (requestURI.getQuery() != null && requestURI.getQuery().contains("year=")) {
                String[] parametersStrings = requestURI.getQuery().split("=");
                try {
                    int parameterYear = Integer
                            .parseInt(parametersStrings[1]);

                    jsonString = gson.toJson(
                            moviesStore.getMovies()
                                    .values()
                                    .stream()
                                    .filter(movie -> movie.getYear()
                                            == parameterYear).toArray());

                    if (jsonString.equals("[]")) {
                        jsonString = gson.toJson(
                                new ErrorResponse(
                                        "Фильмы не найдены.",
                                        ""));
                        responseCode = HTTP_CODE_NOT_FOUND;
                    } else {
                        responseCode = HTTP_CODE_OK;
                    }

                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    jsonString = gson.toJson(
                            new ErrorResponse(
                                    "Некорректный параметр запроса — 'year'.",
                                    ""));
                    responseCode = HTTP_CODE_BAD_REQUEST;
                }
            } else {
                if (moviesStore.isEmpty()) {
                    jsonString = gson.toJson(new String[0]);
                } else {
                    jsonString = gson.toJson(moviesStore.getMovies().values().stream().toList());
                }
                responseCode = HTTP_CODE_OK;
            }
        } else {
            try {
                jsonString = gson.toJson(
                        moviesStore
                                .getMovies()
                                .get(Integer.parseInt(splitStrings[2]))
                );
                responseCode = HTTP_CODE_OK;
                if (jsonString.equals("null")) {
                    jsonString = gson.toJson(
                            new ErrorResponse(
                                    "Фильм не найден.",
                                    ""));
                    responseCode = HTTP_CODE_NOT_FOUND;
                }
            } catch (NumberFormatException e) {
                jsonString = gson.toJson(
                        new ErrorResponse(
                                "Некорректный ID.",
                                ""));
                responseCode = HTTP_CODE_BAD_REQUEST;
            }
        }
    }

    public void postMethodHandler(HttpExchange httpExchange) throws IOException {
        Headers headers = httpExchange.getRequestHeaders();
        List<String> contentTypeValues = headers.get(HEADER_CONTENT_TYPE);
        if ((contentTypeValues == null)
                || (!contentTypeValues.getFirst().contains(MEDIA_TYPE))) {
            jsonString = gson.toJson(
                    new ErrorResponse(
                            "Запрос с неправильным значением или отсутствие " +
                                    "заголовка " + HEADER_CONTENT_TYPE,
                            "Заголовок запроса" + HEADER_CONTENT_TYPE + " должен быть " +
                                    MEDIA_TYPE));
            responseCode = HTTP_CODE_UNSUPPORTED_MEDIA_TYPE;
        } else {
            InputStream inputStream = httpExchange.getRequestBody();
            String requestBody = new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8);

            Movie movie = gson.fromJson(requestBody, Movie.class);
            String title = movie.getTitle();
            int year = movie.getYear();
            if (title.length() <= MAX_TITLE_LENGTH
                    && !title.isEmpty()
                    && year >= MIN_YEAR
                    && year <= MAX_YEAR) {
                int iD = moviesStore.addMovie(title, year);
                jsonString = gson.toJson(moviesStore.getMovies().get(iD));
                responseCode = HTTP_CODE_CREATED;
            } else {
                jsonString = gson.toJson(
                        new ErrorResponse("Ошибка валидации.",
                                "Название фильма не должно быть пустым, " +
                                        "длина названия должна быть " +
                                        "меньше или равно 100 символов, " +
                                        "год фильма должен быть между" +
                                        " 1888 и 2027."));
                responseCode = HTTP_CODE_UNPROCESSABLE_ENTITY;
            }
        }
    }

    public void deleteMethodHandler(HttpExchange httpExchange) {
        URI requestDeleteURI = httpExchange.getRequestURI();
        String pathDelete = requestDeleteURI.getPath();
        String[] splitStringsDelete = pathDelete.split("/");
        if (splitStringsDelete.length >= 3) {
            try {
                if (moviesStore
                        .getMovies()
                        .remove(Integer
                                .parseInt(splitStringsDelete[2])) == null) {
                    jsonString = gson.toJson(
                            new ErrorResponse(
                                    "Фильм не найден.",
                                    ""));
                    responseCode = HTTP_CODE_NOT_FOUND;
                } else {
                    responseCode = HTTP_CODE_NO_CONTENT;
                }
            } catch (NumberFormatException e) {
                jsonString = gson.toJson(
                        new ErrorResponse(
                                "Некорректный ID.",
                                ""));
                responseCode = HTTP_CODE_BAD_REQUEST;
            }
        } else {
            jsonString = gson.toJson(
                    new ErrorResponse(
                            "Некорректный запрос.",
                            "Запрос должен быть - DELETE /movies/{id}"));
            responseCode = HTTP_CODE_BAD_REQUEST;
        }
    }
}