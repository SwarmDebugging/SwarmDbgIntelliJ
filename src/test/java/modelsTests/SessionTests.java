package modelsTests;

import com.swarm.States;
import com.swarm.models.Developer;
import com.swarm.models.Session;
import com.swarm.models.Task;
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
public class SessionTests {

    private final ClientAndServer client;
    private final Session session = new Session();

    public SessionTests(ClientAndServer client) {
        this.client = client;
        setupSessionStartRequest();
        setupSessionStopRequest();
    }

    private void setupSessionStartRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "mutation sessionStart($description:String!,$developerId:Long!,$taskId:Long!)" +
                "{sessionStart(session:{description:$description,developer:{id:$developerId},task:{id:$taskId,done:false}}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("developerId", 1);
        variables.put("taskId", 2);
        variables.put("description", "description");
        body.put("variables", variables);
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(HttpResponse.response()
                        .withBody("{\"data\":{\"sessionStart\":{\"id\":3}}}"));
    }

    private void setupSessionStopRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "mutation sessionStop($sessionId:Long!)" +
                "{sessionStop(id:$sessionId){id}}");
        JSONObject variables = new JSONObject();
        variables.put("sessionId", 1);
        body.put("variables", variables);
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(HttpResponse.response()
                        .withBody("{\"data\":{\"sessionStop\":{\"id\":2}}}"));
    }

    @Test
    void startSessionTest() {
        sendStartSession();

        assertEquals(session, States.currentSession);
        assertEquals(3, session.getId());
    }

    private void sendStartSession() {
        Developer developer = new Developer();
        developer.setId(1);

        Task task = new Task();
        task.setId(2);

        session.setDeveloper(developer);
        session.setTask(task);
        session.setDescription("description");
        session.start();
    }

    @Test
    void stopSessionTest() {
        sendStopSession();

        assertEquals(0, States.currentSession.getId());
        assertEquals(2, session.getId());
    }

    private void sendStopSession() {
        session.setId(1);
        session.stop();
    }
}
