package modelsTests;

import com.swarm.models.Developer;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {8080})
public class DeveloperTests {

    private final ClientAndServer client;
    //TODO: check if reset between tests
    private final Developer developer = new Developer();

    public DeveloperTests(ClientAndServer client) {
        this.client = client;
        setupLoginRequest();
        setupRegisterRequest();
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


}
