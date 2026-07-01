package ru.practicum.moviehub.http;

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
    private final int HTTP_CODE_OK = 200;
    private final int HTTP_CODE_BAD_REQUEST = 400;
    private final int HTTP_CODE_NOT_FOUND = 404;
    private final int HTTP_CODE_UNPROCESSABLE_ENTITY = 422;
    private final String HEADER_CONTENT_TYPE = "Content-type";
    private final String MEDIA_TYPE = "application/json; charset=utf-8";

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
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();

            // Обработчик тела запроса
            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            // Отправьте запрос
            resp = client.send(req, responseBodyHandler);
        }
        // Допишите проверку кода ответа
        assertEquals(HTTP_CODE_OK, resp.statusCode(), "GET /movies должен вернуть 200");

        // Допишите проверку заголовка Content-Type
        String contentTypeHeaderValue =
                resp.headers().firstValue(HEADER_CONTENT_TYPE).orElse("");
        assertEquals(MEDIA_TYPE, contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        // проверка, что был возвращён массив
        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    void getMovies_returnsMoviesArray() throws Exception {

        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {
            URI uri = URI.create("http://localhost:8080/movies");
            HttpRequest req = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Фильм1\",\"year\":1988}"))
                    .uri(uri)
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            client.send(req, responseBodyHandler);
        }


        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {
            URI uri = URI.create("http://localhost:8080/movies");

            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            // Отправьте запрос
            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(HTTP_CODE_OK, resp.statusCode(), "GET /movies должен вернуть 200");


        String contentTypeHeaderValue =
                resp.headers().firstValue(HEADER_CONTENT_TYPE).orElse("");
        assertEquals(MEDIA_TYPE, contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");


        String body = resp.body().trim();
        assertTrue(body.contains("\"title\":\"Фильм1\",\"year\":1988"),
                "Ожидается JSON-объект с фильмом");

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
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        final int HTTP_CODE_CREATED = 201;
        assertEquals(HTTP_CODE_CREATED, resp.statusCode(), "POST /movies должен вернуть 201");


        String contentTypeHeaderValue =
                resp.headers().firstValue(HEADER_CONTENT_TYPE).orElse("");

        assertEquals(MEDIA_TYPE, contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body();

        assertTrue(body.contains("\"title\":\"Фильм1\",\"year\":1988"),
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
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(HTTP_CODE_UNPROCESSABLE_ENTITY, resp.statusCode(), "POST /movies должен вернуть 422");

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
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(HTTP_CODE_UNPROCESSABLE_ENTITY, resp.statusCode(), "POST /movies должен вернуть 422");

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
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(HTTP_CODE_UNPROCESSABLE_ENTITY, resp.statusCode(), "POST /movies должен вернуть 422");

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
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(HTTP_CODE_UNPROCESSABLE_ENTITY, resp.statusCode(), "POST /movies должен вернуть 422");

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
                    .header(HEADER_CONTENT_TYPE, "")
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        final int HTTP_CODE_UNSUPPORTED_MEDIA_TYPE = 415;
        assertEquals(HTTP_CODE_UNSUPPORTED_MEDIA_TYPE, resp.statusCode(), "POST /movies должен вернуть 415");

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
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(HTTP_CODE_UNPROCESSABLE_ENTITY, resp.statusCode(), "POST /movies должен вернуть 422");

    }

    @Test
    void getMovieByID_returnsMovie() throws Exception {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {
            URI uri = URI.create("http://localhost:8080/movies");
            HttpRequest req = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Фильм1\",\"year\":1988}"))
                    .uri(uri)
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            client.send(req, responseBodyHandler);
        }
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {
            URI uri = URI.create("http://localhost:8080/movies");
            HttpRequest req = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Фильм1\",\"year\":1988}"))
                    .uri(uri)
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            client.send(req, responseBodyHandler);
        }

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {

            URI uri = URI.create("http://localhost:8080/movies/1");
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(HTTP_CODE_OK, resp.statusCode(), "GET /movies должен вернуть 200");

        String body = resp.body().trim();
        assertTrue(body.contains("\"title\":\"Фильм1\",\"year\":1988"),
                "Ожидается JSON-объект с фильмом");

    }

    @Test
    void getMovieByID_noMovieByID5_returnsError() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {

            URI uri = URI.create("http://localhost:8080/movies/5");
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(HTTP_CODE_NOT_FOUND, resp.statusCode(), "GET /movies должен вернуть 404");
    }

    @Test
    void getMovieByID_iDNotNumber_returnsError() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {

            URI uri = URI.create("http://localhost:8080/movies/aaa");
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(HTTP_CODE_BAD_REQUEST, resp.statusCode(), "GET /movies/aaa должен вернуть 404");
    }

    @Test
    void deleteMovieByID_correctQuery_returns204() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {

            URI uri = URI.create("http://localhost:8080/movies");
            HttpRequest req = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Фильм1\",\"year\":1988}"))
                    .uri(uri)
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            client.send(req, responseBodyHandler);

        }
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {
            URI uri = URI.create("http://localhost:8080/movies/0");
            HttpRequest req = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(uri)
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        final int HTTP_CODE_NO_CONTENT = 204;
        assertEquals(HTTP_CODE_NO_CONTENT, resp.statusCode(), "DELETE /movies/1 должен вернуть 204");
    }

    @Test
    void deleteMovieByID_noMovieByiD5_returnsError() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {

            URI uri = URI.create("http://localhost:8080/movies/5");
            HttpRequest req = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(uri)
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(HTTP_CODE_NOT_FOUND, resp.statusCode(), "DELETE /movies/5 должен вернуть 404");
    }

    @Test
    void deleteMovieByID_iDNotNumber_returnsError() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {

            URI uri = URI.create("http://localhost:8080/movies/aaa");
            HttpRequest req = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(uri)
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(HTTP_CODE_BAD_REQUEST, resp.statusCode(), "DELETE /movies/aaa должен вернуть 400");
    }

    @Test
    void getMoviesByYear_returnsMovies() throws Exception {
        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {

            URI uri = URI.create("http://localhost:8080/movies");
            HttpRequest req = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Фильм1\",\"year\":1988}"))
                    .uri(uri)
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            client.send(req, responseBodyHandler);

        }

        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {

            URI uri = URI.create("http://localhost:8080/movies?year=1988");
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(HTTP_CODE_OK, resp.statusCode(), "GET /movies?year=1988 должен вернуть 200");

        String body = resp.body().trim();
        assertTrue(body.contains("\"title\":\"Фильм1\",\"year\":1988"),
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
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(HTTP_CODE_NOT_FOUND, resp.statusCode(), "GET /movies?year=1890 должен вернуть 404");
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
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();


            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        assertEquals(HTTP_CODE_BAD_REQUEST, resp.statusCode(), "GET /movies?year=aaaa должен вернуть 400");
    }

    @Test
    void methodNotAllowed_returnsError() throws Exception {

        HttpResponse<String> resp;
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()) {

            URI uri = URI.create("http://localhost:8080/movies");
            HttpRequest req = HttpRequest.newBuilder()
                    .HEAD()
                    .uri(uri)
                    .header(HEADER_CONTENT_TYPE, MEDIA_TYPE)
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

            resp = client.send(req, responseBodyHandler);
        }

        final int HTTP_CODE_METHOD_NOT_ALLOWED = 405;
        assertEquals(HTTP_CODE_METHOD_NOT_ALLOWED, resp.statusCode(), "HEAD /movies должен вернуть 400");
    }
}