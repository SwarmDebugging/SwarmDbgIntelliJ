package serviceTests;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
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
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {8080}, perTestSuite = true)
public class ProductServiceTests extends BasePlatformTestCase {

    private final ClientAndServer client;
    private final ProductService productService;

    public ProductServiceTests(ClientAndServer client) {
        try {
            setUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.client = client;
        setupAllProductsRequest();
        setupAllTasksRequest();
        setupAllSessionsRequest();
        setupProductByDeveloperRequest();

        productService = new ProductService(getProject());
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
        body.put("query", "{tasks{id,title,done,product{id}}}");
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(HttpResponse.response()
                        .withBody("{\"data\":{\"tasks\":[{\"product\":{\"id\":2},\"id\":3,\"title\":\"title3\",\"done\":false},{\"product\":{\"id\":2},\"id\":5,\"title\":\"title5\",\"done\":false}," +
                                "{\"product\":{\"id\":2},\"id\":7,\"title\":\"title3\",\"done\":false}]}}"));
    }

    private void setupAllSessionsRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "{sessions{id,description,finished,task{id,title,done,product{id,name}}}}");
        client.when(HttpRequest.request()
        .withMethod("POST")
        .withPath("/graphql")
        .withBody(body.toString()))
                .respond(HttpResponse.response()
                .withBody("{\"data\":{\"sessions\":[{\"id\":4,\"description\":\"test sessions\",\"finished\":null,\"task\":{\"id\":3,\"title\":\"title3\",\"done\":false,\"product\":{\"id\":2,\"name\":\"product2\"}}}," +
                        "{\"id\":10,\"description\":\"test sessions\",\"finished\":null,\"task\":{\"id\":3,\"title\":\"title3\",\"done\":false,\"product\":{\"id\":2,\"name\":\"product2\"}}}]}}"));
    }

    private void setupProductByDeveloperRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "{sessions(developerId:1){id,description,finished,task{id,title,done,product{id,name}}}}");
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(HttpResponse.response()
                        .withBody("{\"data\":{\"sessions\":[{\"id\":4,\"description\":\"test sessions\",\"finished\":null,\"task\":{\"id\":3,\"title\":\"title3\",\"done\":false,\"product\":{\"id\":2,\"name\":\"product2\"}}}]}}"));
    }

    @Test
    void getAllProductsTest() {
        ArrayList<Product> products = productService.getAllProducts();

        assertThat(products, hasSize(2));
        assertThat(products.get(0).getTasks(), hasSize(3));
        assertThat(products.get(0).getTasks().get(0).getSessions(), hasSize(2));
    }

    @Test
    void getProductsByDeveloperTest() {
        ArrayList<Product> products = productService.getProductsByDeveloper(1);

        assertThat(products, hasSize(1));
        assertThat(products.get(0).getTasks(), hasSize(1));
        assertThat(products.get(0).getTasks().get(0).getSessions(), hasSize(1));
    }
}