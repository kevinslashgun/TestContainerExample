package my.wikicasa.web;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import my.wikicasa.web.config.TestDatabaseConfig;
import my.wikicasa.web.entity.RealEstate;
import my.wikicasa.web.repository.RealEstateRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static my.wikicasa.web.ValidationMessages.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {TestDatabaseConfig.class})
@ActiveProfiles("test")
@Testcontainers
public class RealEstateControllerIT {

    public static final String BASE_API = "/api/realestate";
    public static final String GET_API = BASE_API + "/{id}";
    public static final String PUT_API = BASE_API + "/{id}";
    public static final String DELETE_API = BASE_API + "/{id}";

    @LocalServerPort
    private int port;

    @Autowired
    private RealEstateRepository realEstateRepository;

    // --- HELPER METHODS ---

    private RealEstate createRealEstate() {
        return new RealEstate("testName", "testAddress", 99_999., 4, 1, 89.);
    }

    private RealEstate createRealEstateByAPI() {
        RealEstate realEstate = createRealEstate();
        RequestSpecification requestSpecification = preparePostRequest(realEstate);
        Response response = performPostRequest(requestSpecification);
        Long generatedId = response.jsonPath().getLong("id");
        realEstate.setId(generatedId);
        return realEstate;
    }

    private List<RealEstate> createRealEstatesByAPI() {
        List<RealEstate> realEstates = List.of(
                new RealEstate("testName1", "testAddress1", 5_000.25, 3, 1, 70.),
                new RealEstate("testName2", "testAddress2", 15_000.50, 6, 2, 120.),
                new RealEstate("testName3", "testAddress3", 25_000.75, 9, 3, 170.)
        );

        realEstates.forEach(realEstate -> {
            RequestSpecification postRequest = preparePostRequest(realEstate);
            Response postResponse = performPostRequest(postRequest);
            Long generatedId = postResponse.jsonPath().getLong("id");
            realEstate.setId(generatedId);
        });

        return realEstates;
    }

    private void deleteRealEstateFromResponse(Response response) {
        realEstateRepository.deleteById(response.jsonPath().getLong("id"));
    }

    private void deleteRealEstatesFromResponse(Response response) {
        List<RealEstate> realEstates = response.jsonPath().getList("", RealEstate.class);

        for (RealEstate realEstate : realEstates) {
            realEstateRepository.deleteById(realEstate.getId());
        }
    }

    private RequestSpecification prepareGetRequest() {
        return given();
    }

    private RequestSpecification preparePostRequest(RealEstate realEstate) {
        return given().contentType(ContentType.JSON).body(realEstate);
    }

    private RequestSpecification preparePutRequest(Map<String, Object> updates) {
        return given().contentType(ContentType.JSON).body(updates);
    }

    private RequestSpecification prepareDeleteRequest() {
        return given();
    }

    private Response performGetRequest(RequestSpecification request, String api, Object... params) {
        return request.when().get(api, params);
    }

    private Response performPostRequest(RequestSpecification request) {
        return request.when().post(RealEstateControllerIT.BASE_API);
    }

    private Response performPutRequest(RequestSpecification request, Object... params) {
        return request.when().put(RealEstateControllerIT.PUT_API, params);
    }

    private Response performDeleteRequest(RequestSpecification request, Object... params) {
        return request.when().delete(RealEstateControllerIT.DELETE_API, params);
    }

    private void verifyResponse(Response response, int expectedStatus, String expectedBody) {
        response.then().assertThat()
                .statusCode(expectedStatus);
        if (expectedBody != null) {
            response.then().body(containsString(expectedBody));
        }
    }

    // --- TEST METHODS ---

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
        RealEstate realEstate = createRealEstate();
        RequestSpecification request = preparePostRequest(realEstate);
        Response response = performPostRequest(request);
        realEstate.setId(response.jsonPath().getLong("id"));
        verifyResponse(response, HttpStatus.CREATED.value(), realEstate.toJson());
        deleteRealEstateFromResponse(response);
    }

    @Test
    public void shouldReturnConflictWhenRealEstateAlreadyExists() {
        RealEstate realEstate = createRealEstateByAPI();
        RequestSpecification request = preparePostRequest(realEstate);
        Response response = performPostRequest(request);
        verifyResponse(response, HttpStatus.CONFLICT.value(), "RealEstate already exists");
        realEstateRepository.deleteById(realEstate.getId());
    }

    @Test
    public void shouldGetAllRealEstates() {
        List<RealEstate> realEstates = createRealEstatesByAPI();
        RequestSpecification request = prepareGetRequest();
        Response response = performGetRequest(request, BASE_API);
        verifyResponse(response, HttpStatus.OK.value(), RealEstate.toJson(realEstates));
        deleteRealEstatesFromResponse(response);
    }

    @Test
    public void shouldGetRealEstateByIdWhenIdIsValid() {
        RealEstate realEstate = createRealEstateByAPI();
        RequestSpecification request = prepareGetRequest();
        Response response = performGetRequest(request, GET_API, realEstate.getId());
        verifyResponse(response, HttpStatus.OK.value(), realEstate.toJson());
        deleteRealEstateFromResponse(response);
    }

    @Test
    public void shouldReturnNotFoundWhenIdIsInvalid() {
        RequestSpecification request = prepareGetRequest();
        Response response = performGetRequest(request, GET_API, 1);
        verifyResponse(response, HttpStatus.NOT_FOUND.value(), "RealEstate with ID: 1 not found");
    }

    @Test
    public void shouldUpdateRealEstateWhenPriceIsValid() {
        RealEstate realEstate = createRealEstateByAPI();
        Double newPrice = 10_000.99;
        Map<String, Object> update = Map.of("price", newPrice);
        realEstate.setPrice(newPrice);
        RequestSpecification request = preparePutRequest(update);
        Response response = performPutRequest(request, realEstate.getId());
        verifyResponse(response, HttpStatus.OK.value(), realEstate.toJson());
        realEstateRepository.deleteById(realEstate.getId());
    }

    @Test
    public void shouldDeleteRealEstateWhenIdIsValid() {
        RealEstate realEstate = createRealEstateByAPI();
        Long id = realEstate.getId();
        RequestSpecification request = prepareDeleteRequest();
        Response response = performDeleteRequest(request, id);
        verifyResponse(response, HttpStatus.NO_CONTENT.value(), null);
    }

    @Nested
    public class CreateRealEstateValidationTests {

        private void validateRealEstateCreation(RealEstate realEstate, String expectedMessage) {
            RequestSpecification request = preparePostRequest(realEstate);
            Response response = performPostRequest(request);
            verifyResponse(response, HttpStatus.BAD_REQUEST.value(), expectedMessage);
        }

        @Test
        public void shouldReturnBadRequestWhenNameIsBlank() {
            RealEstate invalidRealEstate = new RealEstate("", "testAddress", 99_999., 4, 1, 89.);
            validateRealEstateCreation(invalidRealEstate, NAME_CANNOT_BE_BLANK);
        }

        @Test
        public void shouldReturnBadRequestWhenAddressIsBlank() {
            RealEstate invalidRealEstate = new RealEstate("testName", "", 99_999., 4, 1, 89.);
            validateRealEstateCreation(invalidRealEstate, ADDRESS_CANNOT_BE_BLANK);
        }

        @Test
        public void shouldReturnBadRequestWhenPriceIsNull() {
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", null, 4, 1, 89.);
            validateRealEstateCreation(invalidRealEstate, PRICE_CANNOT_BE_NULL);
        }

        @Test
        public void shouldReturnBadRequestWhenPriceIsNegative() {
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", -10_000., 4, 1, 89.);
            validateRealEstateCreation(invalidRealEstate, PRICE_CANNOT_BE_NEGATIVE);
        }

        @Test
        public void shouldReturnBadRequestWhenRoomsIsNull() {
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", 99_999., null, 1, 89.);
            validateRealEstateCreation(invalidRealEstate, ROOMS_CANNOT_BE_NULL);
        }

        @Test
        public void shouldReturnBadRequestWhenRoomsIsNegative() {
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", 99_999., -10, 1, 89.);
            validateRealEstateCreation(invalidRealEstate, ROOMS_CANNOT_BE_NEGATIVE);
        }

        @Test
        public void shouldReturnBadRequestWhenBathroomsIsNull() {
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", 99_999., 4, null, 89.);
            validateRealEstateCreation(invalidRealEstate, BATHROOMS_CANNOT_BE_NULL);
        }

        @Test
        public void shouldReturnBadRequestWhenBathroomsIsNegative() {
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", 99_999., 4, -4, 89.);
            validateRealEstateCreation(invalidRealEstate, BATHROOMS_CANNOT_BE_NEGATIVE);
        }

        @Test
        public void shouldReturnBadRequestWhenSqMetersIsNull() {
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", 99_999., 4, 1, null);
            validateRealEstateCreation(invalidRealEstate, SQMETERS_CANNOT_BE_NULL);
        }

        @Test
        public void shouldReturnBadRequestWhenSqMetersIsNegative() {
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", 99_999., 4, 1, -22.);
            validateRealEstateCreation(invalidRealEstate, SQMTERS_CANNOT_BE_NEGATIVE);
        }
    }

}
