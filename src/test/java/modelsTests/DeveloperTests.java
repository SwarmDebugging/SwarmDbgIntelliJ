package modelsTests;

import com.intellij.testFramework.LightIdeaTestCase;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.swarm.models.Developer;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {8080})
public class DeveloperTests extends BasePlatformTestCase {

    private final ClientAndServer client;
    private final Developer developer = new Developer();

    public DeveloperTests(ClientAndServer client) {
        try {
            setUp();
        } catch (Exception e) {
            e.toString();
        }
        this.client = client;
        setupLoginRequest();
        setupRegisterRequest();
        setupBadLoginRequest();
        setupDeveloperAlreadyExistsRequest();
    }

    private void setupLoginRequest() {
        JSONObject body = new JSONObject();
        body.put("query","{developer(username:\"test\"){id}}");
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(
                        HttpResponse.response()
                                .withBody("{\"data\":{\"developer\":{\"id\":1}}}")
                );
    }

    private void setupRegisterRequest() {
        JSONObject body = new JSONObject();
        body.put("query","mutation developerCreate($username: String!){developerCreate(developer:{username:$username}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("username", "test");
        body.put("variables", variables);
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(
                        HttpResponse.response()
                                .withBody("{\"data\":{\"developerCreate\":{\"id\":1}}}")
                );
    }

    private void setupBadLoginRequest() {
        JSONObject body = new JSONObject();
        body.put("query","{developer(username:\"nonexistent\"){id}}");
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(
                        HttpResponse.response()
                                .withBody("{\"data\":{\"developer\":null}}")
                );
    }

    private void setupDeveloperAlreadyExistsRequest() {
        JSONObject body = new JSONObject();
        body.put("query","mutation developerCreate($username: String!){developerCreate(developer:{username:$username}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("username", "exists");
        body.put("variables", variables);
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(
                        HttpResponse.response()
                                .withBody("{\"data\":{\"developerCreate\":null}}")
                );
    }

    @Test
    void registerDeveloperTest() {
        sendRegisterRequest();

        assertEquals(1, developer.getId());
    }

    private void sendRegisterRequest() {
        developer.setUsername("test");
        developer.registerNewDeveloper();
    }

    @Test
    void loginDeveloperTest() {
        sendLoginRequest();

        assertEquals(1, developer.getId());
    }

    private void sendLoginRequest() {
        developer.setUsername("test");
        developer.login();
    }

    @Test
    void badLoginTest() {
        assertDoesNotThrow(this::sendBadLogin);

        assertEquals(0, developer.getId());
    }

    private void sendBadLogin() {
        developer.setUsername("nonexistent");
        developer.login();
    }

    @Test
    void developerAlreadyExistsTest() {
        assertDoesNotThrow(this::sendExistingDeveloper);

        assertEquals(0, developer.getId());
    }

    private void sendExistingDeveloper() {
        developer.setUsername("exists");
        developer.registerNewDeveloper();
    }

}
