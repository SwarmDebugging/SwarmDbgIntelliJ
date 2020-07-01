package modelsTests;

import com.swarm.models.Event;
import com.swarm.models.Method;
import com.swarm.models.Session;
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
public class EventTests {

    private final ClientAndServer client;
    private final Event event = new Event();

    public EventTests(ClientAndServer client) {
        this.client = client;
        setupEventCreationRequest();
    }

    private void setupEventCreationRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "mutation eventCreate($sessionId:Long!,$lineNumber:Int!,$eventKind:String!,$methodId:Long!)" +
                "{eventCreate(event:{session:{id:$sessionId},lineNumber:$lineNumber,kind:$eventKind,method:{id:$methodId}}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("sessionId", 1);
        variables.put("lineNumber", 1);
        variables.put("eventKind", "eventKind");
        variables.put("methodId", 1);
        body.put("variables", variables);
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(HttpResponse.response()
                        .withBody("{\"data\":{\"eventCreate\":{\"id\":2}}}"));
    }

    @Test
    void createEventTest() {
        sendEventCreate();

        assertEquals(2, event.getId());
    }

    private void sendEventCreate() {
        Session session = new Session();
        session.setId(1);
        event.setSession(session);

        Method method = new Method();
        method.setId(1);
        event.setMethod(method);

        event.setLineNumber(1);
        event.setKind("eventKind");

        event.create();
    }
}
