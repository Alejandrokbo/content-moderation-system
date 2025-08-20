package org.ravenpack;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class BasicTranslationTest {

    @Test
    void translate_hola_to_hello() {
        RestAssured.given()
                .queryParam("q", "hola")
                .when()
                .get("/dev/translate")
                .then()
                .statusCode(200)
                .body(equalTo("hello"));
    }

    @Test
    void translate_buenos_dias_to_good_morning() {
        RestAssured.given()
                .queryParam("q", "buenos días")
                .when()
                .get("/dev/translate")
                .then()
                .statusCode(200)
                .body(equalTo("good morning"));
    }

    @Test
    void translate_multiple_words() {
        RestAssured.given()
                .queryParam("q", "hola mundo")
                .when()
                .get("/dev/translate")
                .then()
                .statusCode(200)
                .body(equalTo("hello world"));
    }

    @Test
    void translate_mixed_case() {
        RestAssured.given()
                .queryParam("q", "Hola")
                .when()
                .get("/dev/translate")
                .then()
                .statusCode(200)
                .body(equalTo("Hello"));
    }

    @Test
    void translate_uppercase() {
        RestAssured.given()
                .queryParam("q", "HOLA")
                .when()
                .get("/dev/translate")
                .then()
                .statusCode(200)
                .body(equalTo("HELLO"));
    }

    @Test
    void translate_unknown_words_remain_unchanged() {
        RestAssured.given()
                .queryParam("q", "palabra desconocida")
                .when()
                .get("/dev/translate")
                .then()
                .statusCode(200)
                .body(equalTo("palabra desconocida"));
    }

    @Test
    void translate_common_phrase() {
        RestAssured.given()
                .queryParam("q", "me gusta el gato")
                .when()
                .get("/dev/translate")
                .then()
                .statusCode(200)
                .body(equalTo("I like the cat"));
    }
}
