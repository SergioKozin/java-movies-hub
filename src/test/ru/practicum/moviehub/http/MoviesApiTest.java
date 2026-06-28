package ru.practicum.moviehub.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {

    @BeforeAll
    static void beforeAll() throws Exception {

        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {
            URI uri = URI.create("http://localhost:8080/movies");
            HttpRequest req = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Фильм1\",\"year\":1988}"))
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            client.send(req, responseBodyHandler);
        }
    }

    @BeforeEach
    void beforeEach() {

    }

    @AfterAll
    static void afterAll() {

    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        // Создайте HTTP-клиент,
        // укажите таймаут соединения (connectTimeout), равный 2 секундам
        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {
            URI uri = URI.create("http://localhost:8080/movies");
            // создайте объект GET-запроса на эндпоинт /movies
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();

            // Обработчик тела запроса
            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            // Отправьте запрос
            resp = client.send(req, responseBodyHandler);
        }
        // Допишите проверку кода ответа
        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        // Допишите проверку заголовка Content-Type
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=utf-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        // проверка, что был возвращён массив
        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    void getMovies_returnsMoviesArray() throws Exception {
        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {
            URI uri = URI.create("http://localhost:8080/movies");
            // создайте объект GET-запроса на эндпоинт /movies
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();

            // Обработчик тела запроса
            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            // Отправьте запрос
            resp = client.send(req, responseBodyHandler);
        }
        // Допишите проверку кода ответа
        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        // Допишите проверку заголовка Content-Type
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=utf-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        // проверка, что был возвращён массив
        String body = resp.body().trim();
        assertEquals("[{\"ID\":0,\"title\":\"Фильм1\",\"year\":1988}]", body,
                "Ожидается JSON-массив с фильмом");

    }

    @Test
    void postMovie_correctQuery_returnsMovie() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {
            URI uri = URI.create("http://localhost:8080/movies");


            HttpRequest req = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Фильм1\",\"year\":1988}"))
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201");


        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=utf-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body();
        assertEquals("{\"ID\":2,\"title\":\"Фильм1\",\"year\":1988}", body,
                "Ожидается JSON-объект с фильмом");
    }

    @Test
    void postMovie_emptyTitle_returnsError() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {
            URI uri = URI.create("http://localhost:8080/movies");

            HttpRequest req = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"\",\"year\":1988}"))
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

    }

    @Test
    void postMovie_titleMore100Symbols_returnsError() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {
            URI uri = URI.create("http://localhost:8080/movies");

            HttpRequest req = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"012345678901234567890123456789" +
                            "01234567890123456789012345678901234567890123456789012345678901234567890\",\"year\":1988}"))
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

    }

    @Test
    void postMovie_yearLess1888_returnsError() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {
            URI uri = URI.create("http://localhost:8080/movies");

            HttpRequest req = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Фильм1\",\"year\":1887}"))
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

    }

    @Test
    void postMovie_yearMore2027_returnsError() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {
            URI uri = URI.create("http://localhost:8080/movies");

            HttpRequest req = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Фильм1\",\"year\":2028}"))
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

    }

    @Test
    void postMovie_wrongContentType_returnsError() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {
            URI uri = URI.create("http://localhost:8080/movies");

            HttpRequest req = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Фильм1\",\"year\":2027}"))
                    .uri(uri)
                    .header("Content-type", "")
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(415, resp.statusCode(), "POST /movies должен вернуть 415");

    }

    @Test
    void postMovie_wrongJson_returnsError() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {
            URI uri = URI.create("http://localhost:8080/movies");

            HttpRequest req = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Фильм1\"}"))
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

    }

    @Test
    void getMovieByiD_returnsMovie() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {

            URI uri = URI.create("http://localhost:8080/movies/0");
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String body = resp.body().trim();
        assertEquals("{\"ID\":0,\"title\":\"Фильм1\",\"year\":1988}", body,
                "Ожидается JSON-объект с фильмом");

    }

    @Test
    void getMovieByiD_noMovieByiD5_returnsError() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {

            URI uri = URI.create("http://localhost:8080/movies/5");
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(404, resp.statusCode(), "GET /movies должен вернуть 404");
    }

    @Test
    void getMovieByiD_iDNotNumber_returnsError() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {

            URI uri = URI.create("http://localhost:8080/movies/aaa");
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(400, resp.statusCode(), "GET /movies/aaa должен вернуть 404");
    }

    @Test
    void deleteMovieByiD_correctQuery_returns204() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {


            URI uri = URI.create("http://localhost:8080/movies");
            HttpRequest req = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Фильм1\",\"year\":1988}"))
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            client.send(req, responseBodyHandler);

        }
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {
            URI uri = URI.create("http://localhost:8080/movies/1");
            HttpRequest req = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(204, resp.statusCode(), "DELETE /movies/1 должен вернуть 204");
    }

    @Test
    void deleteMovieByiD_noMovieByiD5_returnsError() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {

            URI uri = URI.create("http://localhost:8080/movies/5");
            HttpRequest req = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(404, resp.statusCode(), "DELETE /movies/5 должен вернуть 404");
    }

    @Test
    void deleteMovieByiD_iDNotNumber_returnsError() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {

            URI uri = URI.create("http://localhost:8080/movies/aaa");
            HttpRequest req = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(400, resp.statusCode(), "DELETE /movies/aaa должен вернуть 400");
    }

    @Test
    void getMoviesByYear_returnsMovies() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {

            URI uri = URI.create("http://localhost:8080/movies?year=1988");
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(200, resp.statusCode(), "GET /movies?year=1988 должен вернуть 200");

        String body = resp.body().trim();
        assertEquals("[{\"ID\":0,\"title\":\"Фильм1\",\"year\":1988}]", body,
                "Ожидается JSON-массив с фильмами");

    }

    @Test
    void getMoviesByYear_noMoviesForYear1890_returnsError() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {

            URI uri = URI.create("http://localhost:8080/movies?year=1890");
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(404, resp.statusCode(), "GET /movies?year=1890 должен вернуть 404");
    }

    @Test
    void getMoviesByYear_yearNotNumber_returnsError() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {

            URI uri = URI.create("http://localhost:8080/movies?year=aaaa");
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header("Content-type", "application/json; charset=utf-8")
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(400, resp.statusCode(), "GET /movies?year=aaaa должен вернуть 400");
    }
}