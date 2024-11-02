package my.wikicasa.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static my.wikicasa.web.ValidationMessages.*;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {TestDatabaseConfig.class})
@ActiveProfiles("test")
@Testcontainers
public class RealEstateControllerIT {

    public static final String BASE_API = "/api/realestate";
    public static final String GET_API = BASE_API + "/{id}";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RealEstateRepository realEstateRepository;

    // --- HELPER METHODS ---

    private RealEstate createRealEstate() {
        return new RealEstate("testName", "testAddress", 99_999., 4, 1, 89.);
    }

    private List<RealEstate> createRealEstates() {
        List<RealEstate> realEstates = List.of(
                new RealEstate("testName1", "testAddress1", 5_000.25, 3, 1, 70.),
                new RealEstate("testName2", "testAddress2", 15_000.50, 6, 2, 120.),
                new RealEstate("testName3", "testAddress3", 25_000.75, 9, 3, 170.)
        );

        String sql = "INSERT INTO real_estate (name, address, price, rooms, bathrooms, sq_meters) VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, realEstates, realEstates.size(), (ps, realEstate) -> {
            ps.setString(1, realEstate.getName());
            ps.setString(2, realEstate.getAddress());
            ps.setDouble(3, realEstate.getPrice());
            ps.setInt(4, realEstate.getRooms());
            ps.setInt(5, realEstate.getBathrooms());
            ps.setDouble(6, realEstate.getSqMeters());
        });

        return realEstates;
    }

    private void deleteRealEstateFromResponse(Response response) {
        String name = response.jsonPath().getString("name");
        String address = response.jsonPath().getString("address");
        Double price = response.jsonPath().getDouble("price");
        realEstateRepository.deleteByNameAddressPrice(name, address, price);
    }

    private void deleteRealEstatesFromResponse(Response response) {
        List<RealEstate> realEstates = response.jsonPath().getList("", RealEstate.class);

        for (RealEstate realEstate : realEstates) {
            realEstateRepository.deleteByNameAddressPrice(realEstate.getName(), realEstate.getAddress(), realEstate.getPrice());
        }
    }

    private RequestSpecification prepareGetRequest() {
        return given();
    }

    private RequestSpecification preparePostRequest(RealEstate realEstate) {
        return given().contentType(ContentType.JSON).body(realEstate);
    }

    private Response performGetRequest(RequestSpecification request, String api, Object... params) {
        return request.when().get(api, params);
    }

    private Response performPostRequest(RequestSpecification request, String api) {
        return request.when().post(api);
    }

    private void verifyResponse(Response response, int expectedStatus, String expectedBody) {
        response.then().assertThat()
                .statusCode(expectedStatus)
                .body(equalTo(expectedBody));
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

        Response response = performPostRequest(request, BASE_API);

        verifyResponse(response, HttpStatus.CREATED.value(), realEstate.toJson());

        deleteRealEstateFromResponse(response);
    }

    @Test
    public void shouldGetAllRealEstates() {
        List<RealEstate> realEstates = createRealEstates();

        RequestSpecification request = prepareGetRequest();

        Response response = performGetRequest(request, BASE_API);

        verifyResponse(response, HttpStatus.OK.value(), RealEstate.toJson(realEstates));

        deleteRealEstatesFromResponse(response);
    }

    @Test
    public void shouldGetRealEstateByIdWhenIdIsValid() {
        RealEstate realEstate = createRealEstate();
        realEstate = realEstateRepository.save(realEstate);

        RequestSpecification request = prepareGetRequest();

        Response response = performGetRequest(request, GET_API, realEstate.getId());

        verifyResponse(response, HttpStatus.OK.value(), realEstate.toJson());

        deleteRealEstateFromResponse(response);
    }

    @Nested
    public class CreateRealEstateValidationTests {

        private void validateRealEstateCreation(RealEstate realEstate, String field, String expectedMessage) {
            RequestSpecification request = preparePostRequest(realEstate);

            Response response = performPostRequest(request, BASE_API);

            ObjectMapper mapper = new ObjectMapper();
            String expectedBody = null;
            try {
                expectedBody = mapper.writeValueAsString(Map.of(field, expectedMessage));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            verifyResponse(response, HttpStatus.BAD_REQUEST.value(), expectedBody);
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
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", 99_999., 4, -4, 89.);

            validateRealEstateCreation(invalidRealEstate, "bathrooms", BATHROOMS_CANNOT_BE_NEGATIVE);
        }

        @Test
        public void shouldReturnBadRequestWhenSqMetersIsNull() {
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", 99_999., 4, 1, null);

            validateRealEstateCreation(invalidRealEstate, "sqMeters", SQMETERS_CANNOT_BE_NULL);
        }

        @Test
        public void shouldReturnBadRequestWhenSqMetersIsNegative() {
            RealEstate invalidRealEstate = new RealEstate("testName", "testAddress", 99_999., 4, 1, -22.);

            validateRealEstateCreation(invalidRealEstate, "sqMeters", SQMTERS_CANNOT_BE_NEGATIVE);
        }
    }

}
