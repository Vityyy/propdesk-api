package devs.group5.rms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import devs.group5.rms.repositories.ApartmentRepository;
import devs.group5.rms.repositories.OwnerRepository;
import devs.group5.rms.repositories.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class PropertyApartmentFlowIntegrationTests {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private ApartmentRepository apartmentRepository;
    @Autowired
    private PropertyRepository propertyRepository;
    @Autowired
    private OwnerRepository ownerRepository;
    @LocalServerPort
    private int port;

    @BeforeEach
    void cleanDatabase() {
        apartmentRepository.deleteAll();
        propertyRepository.deleteAll();
        ownerRepository.deleteAll();
    }

    @Test
    void registeringAnOwnerThenAllowsCreatingAPropertyAndAnApartmentLinkedToThatProperty() throws Exception {
        var suffix = UUID.randomUUID().toString();
        var ownerName = "owner-" + suffix;
        var password = "password123";

        // 1. Register a brand-new owner so the rest of the flow uses a real persisted user.
        var registerResponse = postJson(
                "/auth/register/owner",
                """
                        {
                          "name": "%s",
                          "password": "%s"
                        }
                        """.formatted(ownerName, password),
                null
        );

        assertThat(registerResponse.statusCode()).isEqualTo(200);

        JsonNode ownerJson = objectMapper.readTree(registerResponse.body());
        assertThat(ownerJson.get("name").asText()).isEqualTo(ownerName);
        var ownerId = UUID.fromString(ownerJson.get("id").asText());

        // 2. Log in with that owner and capture the access token needed for protected endpoints.
        var loginResponse = postJson(
                "/auth/login",
                """
                        {
                          "name": "%s",
                          "password": "%s"
                        }
                        """.formatted(ownerName, password),
                null
        );

        assertThat(loginResponse.statusCode()).isEqualTo(200);

        JsonNode tokenJson = objectMapper.readTree(loginResponse.body());
        var accessToken = tokenJson.get("access").asText();

        // 3. Create a property owned by the authenticated owner.
        var propertyResponse = postJson(
                "/properties",
                """
                        {
                          "propertyName": "Edificio Central",
                          "propertyAddress": "Av. Siempre Viva 742",
                          "pictureUrl": "",
                          "ownerId": "%s",
                          "apartmentRanges": []
                        }
                        """.formatted(ownerId),
                accessToken
        );

        assertThat(propertyResponse.statusCode()).isEqualTo(200);

        JsonNode propertyJson = objectMapper.readTree(propertyResponse.body());
        assertThat(propertyJson.get("name").asText()).isEqualTo("Edificio Central");
        assertThat(propertyJson.get("address").asText()).isEqualTo("Av. Siempre Viva 742");
        assertThat(propertyJson.get("ownerId").asText()).isEqualTo(ownerId.toString());
        var propertyId = UUID.fromString(propertyJson.get("id").asText());

        // 4. Verify that the property was really stored in the database with the expected owner.
        assertThat(propertyRepository.findById(propertyId))
                .get()
                .satisfies(property -> {
                    assertThat(property.getName()).isEqualTo("Edificio Central");
                    assertThat(property.getAddress()).isEqualTo("Av. Siempre Viva 742");
                    assertThat(property.getOwner().getId()).isEqualTo(ownerId);
                });

        // 5. Create an apartment associated with the property created in the previous step.
        // ApartmentController expects a LIST of ApartmentRequest in the request body and returns a list of ApartmentResponse.
        var apartmentRequestBody = """
                [
                  {
                    "number": 1,
                    "propertyId": "%s",
                    "amount_due": 1
                  }
                ]
                """.formatted(propertyId);

        var apartmentResponse = postJson(
                "/apartments",
                apartmentRequestBody,
                accessToken
        );

        assertThat(apartmentResponse.statusCode()).isEqualTo(200);

        JsonNode apartmentArray = objectMapper.readTree(apartmentResponse.body());
        // Ensure we got an array with at least one element and inspect the first element
        assertThat(apartmentArray.isArray()).isTrue();
        assertThat(apartmentArray.size()).isGreaterThan(0);

        JsonNode apartmentJson = apartmentArray.get(0);
        assertThat(apartmentJson.get("number").asInt()).isEqualTo(1);
        assertThat(apartmentJson.get("propertyId").asText()).isEqualTo(propertyId.toString());
        var apartmentId = UUID.fromString(apartmentJson.get("id").asText());

        // 6. Verify that the apartment was persisted and linked to the correct property.
        assertThat(apartmentRepository.findById(apartmentId))
                .get()
                .satisfies(apartment -> {
                    assertThat(apartment.getNumber()).isEqualTo(1);
                    assertThat(apartment.getProperty().getId()).isEqualTo(propertyId);
                });
    }

    // Sends authenticated JSON requests against the embedded test server.
    private HttpResponse<String> postJson(String path, String body, String accessToken)
            throws IOException, InterruptedException {
        var requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        if (accessToken != null) {
            requestBuilder.header("Authorization", "Bearer " + accessToken);
        }

        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
