package ToolWindowTests;

import com.intellij.testFramework.LightIdeaTestCase;
import com.swarm.models.Developer;
import com.swarm.toolWindow.ProductToolWindow;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {8080})
public class ProductToolWindowTests extends LightIdeaTestCase {

    private final ClientAndServer client;
    private final ProductToolWindow productToolWindow;

    public ProductToolWindowTests(ClientAndServer client) {
        this.client = client;
        setupAllProductsRequest();
        setupAllTasksRequest();
        Developer developer = new Developer();
        productToolWindow = new ProductToolWindow(null, getProject(), developer);
    }

    private void setupAllProductsRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "{allProducts{id,name}}");
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(HttpResponse.response()
                        .withBody("{\"data\":{\"allProducts\":[{\"id\":1,\"name\":\"product1\"},{\"id\":2,\"name\":\"product2\"}]}"));
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
    void addProductsToProductListTest() {
        //how to test it?
    }
}
