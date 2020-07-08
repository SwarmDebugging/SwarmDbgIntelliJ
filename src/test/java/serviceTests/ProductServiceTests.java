package serviceTests;

import com.swarm.models.Product;
import com.swarm.services.ProductService;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {8080}, perTestSuite = true)
public class ProductServiceTests {

    private final ClientAndServer client;
    private final ProductService productService = new ProductService();

    public ProductServiceTests(ClientAndServer client) {
        this.client = client;
        setupAllProductsRequest();
        setupAllTasksRequest();
    }

    private void setupAllProductsRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "{allProducts{id,name}}");
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(HttpResponse.response()
                        .withBody("{\"data\":{\"allProducts\":[{\"id\":1,\"name\":\"product1\"},{\"id\":2,\"name\":\"product2\"}]}}"));
    }

    private void setupAllTasksRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "{tasks{product{id,name},id,title,done}}");
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(HttpResponse.response()
                        .withBody("{\"data\":{\"tasks\":[{\"product\":{\"id\":2,\"name\":\"product2\"},\"id\":3,\"title\":\"title3\",\"done\":false}]}}"));
    }

    @Test
    void getAllProductsTest() {
        ArrayList<Product> products = productService.getAllProducts();

        assertThat(products, hasSize(2));
        assertEquals(products.get(0).getTasks().get(0).getId(), 3);
        assertEquals(products.get(0).getId(), 2);
        assertEquals(products.get(1).getId(), 1);
    }

}
