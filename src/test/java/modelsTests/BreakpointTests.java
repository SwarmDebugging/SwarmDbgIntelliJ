package modelsTests;

import com.swarm.models.Breakpoint;
import com.swarm.models.Type;
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
public class BreakpointTests {

    private final ClientAndServer client;
    private final Breakpoint breakpoint = new Breakpoint();

    public BreakpointTests(ClientAndServer client) {
        this.client = client;
        setupBreakpointCreationRequest();
    }

    private void setupBreakpointCreationRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "mutation breakpointCreate($typeId:Long!,$lineNumber:Int!)" +
                "{breakpointCreate(breakpoint:{type:{id:$typeId},lineNumber:$lineNumber}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("typeId", 1);
        variables.put("lineNumber", 1);
        body.put("variables", variables);
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(HttpResponse.response()
                        .withBody("{\"data\":{\"breakpointCreate\":{\"id\":2}}}"));
    }

    @Test
    void createBreakpointTest() {
        sendBreakpointCreate();

        assertEquals(2, breakpoint.getId());
    }

    private void sendBreakpointCreate() {
        breakpoint.setLineNumber(1);

        Type type = new Type();
        type.setId(1);
        breakpoint.setType(type);

        breakpoint.create();
    }
}
