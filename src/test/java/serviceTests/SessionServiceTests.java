package serviceTests;

import com.swarm.models.Developer;
import com.swarm.models.Session;
import com.swarm.services.SessionService;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {8080}, perTestSuite = true)
public class SessionServiceTests {

    private final ClientAndServer client;
    private final SessionService sessionService = new SessionService();

    public SessionServiceTests(ClientAndServer client) {
        this.client = client;
        setupSessionsByDeveloperRequest();
    }

    private void setupSessionsByDeveloperRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "{sessions(developerId:1){id,finished,description,task{title,done}}}");
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(HttpResponse.response()
                        .withBody("{\n" +
                                "  \"data\": {\n" +
                                "    \"sessions\": [\n" +
                                "      {\n" +
                                "        \"id\": 4,\n" +
                                "        \"finished\": null,\n" +
                                "        \"description\": \"first session\",\n" +
                                "        \"task\": {\n" +
                                "          \"title\": \"issue #6391\",\n" +
                                "          \"done\": false\n" +
                                "        }\n" +
                                "      },\n" +
                                "      {\n" +
                                "        \"id\": 176,\n" +
                                "        \"finished\": \"2020-07-13T16:16:23.712\",\n" +
                                "        \"description\": \"second session\",\n" +
                                "        \"task\": {\n" +
                                "          \"title\": \"issue #6391\",\n" +
                                "          \"done\": false\n" +
                                "        }\n" +
                                "      }]}}"));
    }

    @Test
    void sessionsByDeveloperTest() {
        Developer developer = new Developer();
        developer.setId(1);

        ArrayList<Session> sessions = sessionService.sessionsByDeveloper(developer);

        assertThat(sessions, hasSize(2));
        assertEquals(sessions.get(0).getDescription(), "first session");
        assertEquals(sessions.get(1).getTask().getTitle(), "issue #6391");
    }
}