package serviceTests;

import com.swarm.models.Method;
import com.swarm.services.RecommendationService;
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
public class RecommendationServiceTests {

    private final ClientAndServer client;
    private final RecommendationService recommendationService = new RecommendationService();

    public RecommendationServiceTests(ClientAndServer client) {
        this.client = client;
        setupGetRecommendedMethodsRequest();
    }

    private void setupGetRecommendedMethodsRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "{methodsUsedInTask(taskId:1){id,name,type{id,name,fullName,fullPath}}}");
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(HttpResponse.response()
                        .withBody("{\"data\":{\"methodsUsedInTask\":[{\"id\":2,\"name\":\"methodName\",\"type\":{\"id\":3,\"name\":\"Type\",\"fullName\":\"full Name\",\"fullPath\":\"full path\"}}]}}"));
    }

    @Test
    void getRecommendedMethodsTest() {
        ArrayList<Method> recommendedMethods = recommendationService.getRecommendedMethods(1);

        assertThat(recommendedMethods, hasSize(1));
    }


}
