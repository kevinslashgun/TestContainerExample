package my.wikicasa.web;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import my.wikicasa.web.config.TestDatabaseConfig;
import my.wikicasa.web.entity.RealEstate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static my.wikicasa.web.ValidationMessages.*;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {TestDatabaseConfig.class})
@ActiveProfiles("test")
@Testcontainers
public class RealEstateControllerIT {

    public static final String CREATE_REALESTATE_API = "/api/realestate";

    @LocalServerPort
    private int port;

    @BeforeAll
    public static void beforeAll() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @BeforeEach
    public void beforeEach() {
        RestAssured.port = port;
    }

    @Test
    public void shouldCreateRealEstateWhenDataIsValid() {
        RealEstate realEstate = new RealEstate("testName", "testAddress", 99_999., 4, 1, 89.);

        given().contentType(ContentType.JSON).body(realEstate)
                .when().post(CREATE_REALESTATE_API)
                .then().assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .body(equalTo(realEstate.toJson()));
    }

    @Nested
    public class CreateRealEstateValidationTests {

        private void validateRealEstateCreation(RealEstate realEstate, String field, String expectedMessage) {
            given().contentType(ContentType.JSON).body(realEstate)
                    .when().post(CREATE_REALESTATE_API)
                    .then().assertThat()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body(field, equalTo(expectedMessage));
        }

        @Test
        public void shouldReturnBadRequestWhenNameIsBlank() {
            RealEstate invalidRealEstate = new RealEstate("", "testAddress", 99_999., 4, 1, 89.);

            validateRealEstateCreation(invalidRealEstate, "name", NAME_CANNOT_BE_BLANK);
        }

        @Test
        public void shouldReturnBadRequestWhenAddressIsBlank() {
            RealEstate invalidRealEstate = new RealEstate("testName", "", 99_999., 4, 1, 89.);

            validateRealEstateCreation(invalidRealEstate, "address", ADDRESS_CANNOT_BE_BLANK);
        }

        @Test
        public void shouldReturnBadRequestWhenPriceIsNull() {
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", null, 4, 1, 89.);

            validateRealEstateCreation(invalidRealEstate, "price", PRICE_CANNOT_BE_NULL);
        }

        @Test
        public void shouldReturnBadRequestWhenPriceIsNegative() {
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", -10_000., 4, 1, 89.);

            validateRealEstateCreation(invalidRealEstate, "price", PRICE_CANNOT_BE_NEGATIVE);
        }

        @Test
        public void shouldReturnBadRequestWhenRoomsIsNull() {
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", 99_999., null, 1, 89.);

            validateRealEstateCreation(invalidRealEstate, "rooms", ROOMS_CANNOT_BE_NULL);
        }

        @Test
        public void shouldReturnBadRequestWhenRoomsIsNegative() {
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", 99_999., -10, 1, 89.);

            validateRealEstateCreation(invalidRealEstate, "rooms", ROOMS_CANNOT_BE_NEGATIVE);
        }

        @Test
        public void shouldReturnBadRequestWhenBathroomsIsNull() {
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", 99_999., 4, null, 89.);

            validateRealEstateCreation(invalidRealEstate, "bathrooms", BATHROOMS_CANNOT_BE_NULL);
        }

        @Test
        public void shouldReturnBadRequestWhenBathroomsIsNegative() {
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", -10_000., 4, -4, 89.);

            validateRealEstateCreation(invalidRealEstate, "bathrooms", BATHROOMS_CANNOT_BE_NEGATIVE);
        }

        @Test
        public void shouldReturnBadRequestWhenSqMetersIsNull() {
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", 99_999., 4, 1, null);

            validateRealEstateCreation(invalidRealEstate, "sqMeters", SQMETERS_CANNOT_BE_NULL);
        }

        @Test
        public void shouldReturnBadRequestWhenSqMetersIsNegative() {
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", -10_000., 4, 1, -22.);

            validateRealEstateCreation(invalidRealEstate, "sqMeters", SQMTERS_CANNOT_BE_NEGATIVE);
        }
    }

}
