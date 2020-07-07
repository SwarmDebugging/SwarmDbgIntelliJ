package modelsTests;

import com.swarm.models.Method;
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
@MockServerSettings(ports = {8080}, perTestSuite = true)
public class MethodTests {

    private final ClientAndServer client;
    private final Method method = new Method();

    public MethodTests(ClientAndServer client) {
        this.client = client;
        setupMethodCreationRequest();
    }

    private void setupMethodCreationRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "mutation methodCreate($typeId:Long!,$signature:String!,$name:String!)" +
                "{methodCreate(method:{type:{id:$typeId},signature:$signature,name:$name}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("typeId", 1);
        variables.put("signature", "signature");
        variables.put("name", "name");
        body.put("variables", variables);
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(HttpResponse.response()
                        .withBody("{\"data\":{\"methodCreate\":{\"id\":2}}}"));
    }

    @Test
    void createMethodTest() {
        sendMethodCreate();

        assertEquals(2, method.getId());
    }

    private void sendMethodCreate() {
        Type type = new Type();
        type.setId(1);
        method.setType(type);

        method.setName("name");
        method.setSignature("signature");

        method.create();
    }


}
