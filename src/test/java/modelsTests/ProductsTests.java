package modelsTests;

import com.swarm.models.Developer;
import com.swarm.models.Product;
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
public class ProductsTests {

    private final ClientAndServer client;

    private final Product product = new Product();

    public ProductsTests(ClientAndServer client) {
        this.client = client;
        setupProductCreationRequest();
        setupTaskCreationRequest();
        setupSessionCreationRequest();
    }

    private void setupProductCreationRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "mutation productCreate($name: String!){productCreate(product:{name:$name}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("name", "test");
        body.put("variables", variables);
        client.when(HttpRequest.request()
        .withMethod("POST")
        .withPath("/graphql")
        .withBody(body.toString()))
                .respond(HttpResponse.response()
                .withBody("{\"data\":{\"productCreate\":{\"id\":1}}}"));
    }
    private void setupTaskCreationRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "mutation taskCreate($title:String!,$done:Boolean!,$productId:Long!)" +
                "{taskCreate(task:{title:$title,done:$done,product:{id:$productId}}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("title", "productCreation");
        variables.put("done", true);
        variables.put("productId", 1);
        body.put("variables", variables);
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(HttpResponse.response()
                        .withBody("{\"data\":{\"taskCreate\":{\"id\":2}}}"));
    }

    private void setupSessionCreationRequest() {
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


    //TODO: add more assertions
    @Test
    void createProductTest() {
        sendProductCreate();

        assertEquals(1, product.getId());
    }

    private void sendProductCreate() {
        product.setName("test");
        Developer developer = new Developer();
        developer.setId(1);
        product.setDeveloper(developer);
        product.create();
    }

}
