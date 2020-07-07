package modelsTests;

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
@MockServerSettings(ports = {8080}, perTestSuite = true)
public class ProductsTests {

    private final ClientAndServer client;

    private final Product product = new Product();

    public ProductsTests(ClientAndServer client) {
        this.client = client;
        setupProductCreationRequest();
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

    @Test
    void createProductTest() {
        sendProductCreate();

        assertEquals(1, product.getId());
    }

    private void sendProductCreate() {
        product.setName("test");
        product.create();
    }

}
