package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
            httpServer.createContext("/movies",
                    new BaseHttpHandler() {
                        @Override
                        public void handle(HttpExchange httpExchange) throws IOException {
                            Headers headers;
                            String method = httpExchange.getRequestMethod();
                            Gson gson = new Gson();
                            String jsonString = "";
                            int responseCode;

                            switch (method) {
                                case "GET":
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
                                                    responseCode = 404;
                                                } else {
                                                    responseCode = 200;
                                                }

                                            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                                                jsonString = gson.toJson(
                                                        new ErrorResponse(
                                                                "Некорректный параметр запроса — 'year'.",
                                                                ""));
                                                responseCode = 400;
                                            }

                                        } else {
                                            if (moviesStore.isEmpty()) {
                                                jsonString = gson.toJson(new String[0]);
                                            } else {
                                                jsonString = gson.toJson(moviesStore.getMovies().values().toArray());
                                            }
                                            responseCode = 200;
                                        }

                                    } else {

                                        try {
                                            jsonString = gson.toJson(
                                                    moviesStore
                                                            .getMovies()
                                                            .get(Integer.parseInt(splitStrings[2]))
                                            );
                                            responseCode = 200;
                                            if (jsonString.equals("null")) {
                                                jsonString = gson.toJson(
                                                        new ErrorResponse(
                                                                "Фильм не найден.",
                                                                ""));
                                                responseCode = 404;
                                            }
                                        } catch (NumberFormatException e) {
                                            jsonString = gson.toJson(
                                                    new ErrorResponse(
                                                            "Некорректный ID.",
                                                            ""));
                                            responseCode = 400;
                                        }
                                    }
                                    break;
                                case "POST":
                                    headers = httpExchange.getRequestHeaders();
                                    List<String> contentTypeValues = headers.get("Content-type");
                                    if ((contentTypeValues == null)
                                            || (!contentTypeValues.getFirst().contains("application/json"))) {
                                        jsonString = gson.toJson(
                                                new ErrorResponse(
                                                        "Запрос с неправильным значением или отсутствие " +
                                                                "заголовка Content-Type.",
                                                        "Заголовок запроса Content-Type должен быть " +
                                                                "- application/json"));
                                        responseCode = 415;
                                    } else {
                                        InputStream inputStream = httpExchange.getRequestBody();
                                        String requestBody = new String(
                                                inputStream.readAllBytes(),
                                                StandardCharsets.UTF_8);
                                        JsonElement jsonElement = JsonParser.parseString(requestBody);
                                        if (jsonElement.isJsonObject()) {
                                            JsonObject jsonObject = jsonElement.getAsJsonObject();
                                            try {
                                                String title = jsonObject.get("title").getAsString();
                                                int year = jsonObject.get("year").getAsInt();

                                                if (title.length() <= 100
                                                        && !title.isEmpty()
                                                        && year >= 1888
                                                        && year <= 2027) {
                                                    int iD = moviesStore.addMovie(title, year);
                                                    jsonString = gson.toJson(moviesStore.getMovies().get(iD));
                                                    responseCode = 201;
                                                } else {
                                                    jsonString = gson.toJson(
                                                            new ErrorResponse("Ошибка валидации.",
                                                                    "Название фильма не должно быть пустым, " +
                                                                            "длина названия должна быть " +
                                                                            "меньше или рано 100 символов, " +
                                                                            "год фильма должен быть между" +
                                                                            " 1888 и 2027."));
                                                    responseCode = 422;
                                                }
                                            } catch (Exception e) {
                                                jsonString = gson.toJson(
                                                        new ErrorResponse(
                                                                "Неправильный формат запроса.",
                                                                "Проверьте название полей запроса."));
                                                responseCode = 422;
                                            }
                                        } else {
                                            jsonString = gson.toJson(
                                                    new ErrorResponse("Ошибка валидации.",
                                                            "Тело запроса - не JSON объект."));
                                            responseCode = 422;
                                        }
                                    }
                                    break;
                                case "DELETE":
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
                                                responseCode = 404;
                                            } else {
                                                responseCode = 204;
                                            }
                                        } catch (NumberFormatException e) {
                                            jsonString = gson.toJson(
                                                    new ErrorResponse(
                                                            "Некорректный ID.",
                                                            ""));
                                            responseCode = 400;
                                        }
                                    } else {
                                        jsonString = gson.toJson(
                                                new ErrorResponse(
                                                        "Некорректный запрос.",
                                                        "Запрос должен быть - DELETE /movies/{id}"));
                                        responseCode = 400;
                                    }
                                    break;
                                default:
                                    jsonString = gson.toJson(
                                            new ErrorResponse("Неподдерживаемый HTTP-метод.", ""));
                                    responseCode = 405;
                            }
                            headers = httpExchange.getResponseHeaders();
                            headers.set("Content-Type", "application/json; charset=utf-8");
                            httpExchange.sendResponseHeaders(responseCode, 0);
                            try (
                                    OutputStream outputStream = httpExchange.getResponseBody()) {
                                outputStream.write(jsonString.getBytes(StandardCharsets.UTF_8));
                            }
                        }
                    });
            httpServer.start();
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void stop() {
        httpServer.stop(1);
    }
}