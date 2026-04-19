package devs.group5.rms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import devs.group5.rms.repositories.AdminRepository;
import devs.group5.rms.repositories.ExpenseRepository;
import devs.group5.rms.repositories.OwnerRepository;
import devs.group5.rms.repositories.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Verifies that owners can authorize admins and that admins gain access to owner-managed actions.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OwnerAdminAssociationIntegrationTests {
    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private AdminRepository adminRepository;

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanDatabase() {
        expenseRepository.deleteAll();
        propertyRepository.deleteAll();
        ownerRepository.deleteAll();
        adminRepository.deleteAll();
    }

    @Test
    void ownerCanAssociateWithAdminAndAdminCanManageOwnerProperty() throws Exception {
        var suffix = UUID.randomUUID().toString();
        var ownerName = "owner-" + suffix;
        var adminName = "admin-" + suffix;
        var password = "password123";

        var ownerId = registerOwner(ownerName, password);
        var adminId = registerAdmin(adminName, password);
        var ownerToken = login(ownerName, password);

        var propertyResponse = postJson(
                "/properties",
                """
                        {
                          "name": "Torre Norte",
                          "address": "Calle 123",
                          "ownerId": "%s"
                        }
                        """.formatted(ownerId),
                ownerToken
        );

        assertThat(propertyResponse.statusCode()).isEqualTo(200);
        var propertyId = UUID.fromString(objectMapper.readTree(propertyResponse.body()).get("id").asText());

        var associationResponse = postJson(
                "/owners/me/admin",
                """
                        {
                          "adminId": "%s",
                          "adminCut": 12.5
                        }
                        """.formatted(adminId),
                ownerToken
        );

        // This request is the real proof that the owner can choose a specific admin.
        assertThat(associationResponse.statusCode()).isEqualTo(200);

        JsonNode associationJson = objectMapper.readTree(associationResponse.body());
        assertThat(associationJson.get("ownerId").asText()).isEqualTo(ownerId.toString());
        assertThat(associationJson.get("adminId").asText()).isEqualTo(adminId.toString());
        assertThat(associationJson.get("adminCut").decimalValue()).isEqualByComparingTo(new BigDecimal("12.5"));

        var adminToken = login(adminName, password);

        var expenseResponse = postJson(
                "/expenses",
                """
                        {
                          "category": "Maintenance",
                          "description": "Lobby lights",
                          "amount": 1250.00,
                          "date": "2026-04-19",
                          "propertyId": "%s"
                        }
                        """.formatted(propertyId),
                adminToken
        );

        assertThat(expenseResponse.statusCode()).isEqualTo(200);

        var expenseJson = objectMapper.readTree(expenseResponse.body());
        var expenseId = UUID.fromString(expenseJson.get("id").asText());

        assertThat(ownerRepository.findById(ownerId))
                .get()
                .satisfies(owner -> {
                    // This assertion proves the chosen admin was actually persisted for the owner.
                    assertThat(owner.getAdmin()).isNotNull();
                    assertThat(owner.getAdmin().getId()).isEqualTo(adminId);
                    assertThat(owner.getAdminCut()).isEqualByComparingTo(new BigDecimal("12.5"));
                });

        assertThat(expenseRepository.findById(expenseId))
                .get()
                .satisfies(expense -> {
                    assertThat(expense.getProperty().getId()).isEqualTo(propertyId);
                    assertThat(expense.getDescription()).isEqualTo("Lobby lights");
                });
    }

    // Registers an owner account and returns the created user id.
    private UUID registerOwner(String name, String password) throws IOException, InterruptedException {
        var response = postJson(
                "/auth/register/owner",
                """
                        {
                          "name": "%s",
                          "password": "%s"
                        }
                        """.formatted(name, password),
                null
        );

        assertThat(response.statusCode()).isEqualTo(200);
        return UUID.fromString(objectMapper.readTree(response.body()).get("id").asText());
    }

    // Registers an admin account and returns the created user id.
    private UUID registerAdmin(String name, String password) throws IOException, InterruptedException {
        var response = postJson(
                "/auth/register/admin",
                """
                        {
                          "name": "%s",
                          "password": "%s"
                        }
                        """.formatted(name, password),
                null
        );

        assertThat(response.statusCode()).isEqualTo(200);
        return UUID.fromString(objectMapper.readTree(response.body()).get("id").asText());
    }

    // Logs in a user and returns the access token for authenticated requests.
    private String login(String name, String password) throws IOException, InterruptedException {
        var response = postJson(
                "/auth/login",
                """
                        {
                          "name": "%s",
                          "password": "%s"
                        }
                        """.formatted(name, password),
                null
        );

        assertThat(response.statusCode()).isEqualTo(200);
        return objectMapper.readTree(response.body()).get("access").asText();
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
