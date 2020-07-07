package modelsTests;

import com.swarm.models.Invocation;
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
@MockServerSettings(ports = {8080}, perTestSuite = true)
public class InvocationTests {

    private final ClientAndServer client;
    private final Invocation invocation = new Invocation();

    public InvocationTests(ClientAndServer client) {
        this.client = client;
        setupInvocationCreationRequest();
    }

    private void setupInvocationCreationRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "mutation invocationCreate($sessionId:Long!,$invokingId:Long!,$invokedId:Long!)" +
                "{invocationCreate(invocation:{session:{id:$sessionId},invoking:{id:$invokingId},invoked:{id:$invokedId},virtual:false}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("sessionId", 1);
        variables.put("invokingId", 1);
        variables.put("invokedId", 1);
        body.put("variables", variables);
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(HttpResponse.response()
                        .withBody("{\"data\":{\"invocationCreate\":{\"id\":2}}}"));
    }

    @Test
    void createInvocationTest() {
        sendInvocationCreate();

        assertEquals(2, invocation.getId());
    }

    private void sendInvocationCreate() {
        Session session = new Session();
        session.setId(1);
        invocation.setSession(session);

        Method invoking = new Method();
        invoking.setId(1);
        invocation.setInvoking(invoking);

        Method invoked = new Method();
        invoked.setId(1);
        invocation.setInvoked(invoked);

        invocation.create();
    }
}
