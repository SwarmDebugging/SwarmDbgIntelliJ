package modelsTests;

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
        setupSessionCreationForProductCreationRequest();
    }

    private void setupSessionCreationForProductCreationRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "mutation sessionCreate($developerId:Long!,$taskId:Long!,$done:Boolean!)" +
                "{sessionCreate(session:{developer:{id:$developerId},task:{id:$taskId,done:$done}}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("developerId", 1);
        variables.put("taskId", 2);
        variables.put("done", true);
        body.put("variables", variables);
        client.when(HttpRequest.request()
        .withMethod("POST")
        .withPath("/graphql")
        .withBody(body.toString()))
                .respond(HttpResponse.response()
                .withBody("{\"data\":{\"sessionCreate\":{\"id\":3}}}"));
    }

    @Test
    void createSessionForProductCreation() {
        sendSessionCreate();

        assertEquals(3, session.getId());
    }

    private void sendSessionCreate() {
        Developer developer = new Developer();
        developer.setId(1);

        Task task = new Task();
        task.setId(2);
        task.setDone(true);

        session.setDeveloper(developer);
        session.setTask(task);

        session.createForNewProductLink();
    }

}
