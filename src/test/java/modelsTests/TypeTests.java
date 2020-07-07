package modelsTests;

import com.swarm.models.Session;
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
public class TypeTests {

    private final ClientAndServer client;
    private final Type type = new Type();

    public TypeTests(ClientAndServer client) {
        this.client = client;
        setupTypeCreationRequest();
    }

    private void setupTypeCreationRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "mutation typeCreate($sessionId:Long!,$name:String!,$fullPath:String!,$fullName:String!,$source:String){" +
                "typeCreate(typeWrapper:{type:{session:{id:$sessionId},name:$name,fullPath:$fullPath,fullName:$fullName},source:$source}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("sessionId", 1);
        variables.put("name", "name");
        variables.put("fullPath", "fullPath");
        variables.put("fullName", "fullName");
        variables.put("source", "sourceCode");
        body.put("variables", variables);
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(HttpResponse.response()
                        .withBody("{\"data\":{\"typeCreate\":{\"id\":2}}}"));
    }

    @Test
    void createTypeTest() {
        sendTypeCreate();

        assertEquals(2, type.getId());
    }

    private void sendTypeCreate() {
        type.setFullName("fullName");
        type.setFullPath("fullPath");
        type.setName("name");
        type.setSourceCode("sourceCode");

        Session session = new Session();
        session.setId(1);
        type.setSession(session);
        type.create();
    }

}
