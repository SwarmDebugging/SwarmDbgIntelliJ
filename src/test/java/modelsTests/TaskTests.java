package modelsTests;

import com.swarm.models.Product;
import com.swarm.models.Task;
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
public class TaskTests {

    private final ClientAndServer client;
    private final Task task = new Task();

    public TaskTests(ClientAndServer client) {
        this.client = client;
        setupTaskCreationRequest();
    }

    private void setupTaskCreationRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "mutation taskCreate($title:String!,$done:Boolean!,$productId:Long!)" +
                "{taskCreate(task:{title:$title,done:$done,product:{id:$productId}}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("title", "testTitle");
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

    @Test
    void createTaskTest() {
        sendTaskCreate();

        assertEquals(2, task.getId());
    }

    private void sendTaskCreate() {
        task.setTitle("testTitle");
        task.setDone(true);

        Product product = new Product();
        product.setId(1);

        task.setProduct(product);
        task.create();
    }


}
