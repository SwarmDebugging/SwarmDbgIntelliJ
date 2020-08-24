package serviceTests;

import com.swarm.models.Breakpoint;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {8080}, perTestSuite = true)
public class RecommendationServiceTests {

    private final ClientAndServer client;
    private final RecommendationService recommendationService = new RecommendationService();

    public RecommendationServiceTests(ClientAndServer client) {
        this.client = client;
        setupGetRecommendedMethodsRequest();
        setupGetBreakpointsBySessionRequest();
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

    private void setupGetBreakpointsBySessionRequest() {
        JSONObject body = new JSONObject();
        body.put("query", "{breakpoints(sessionId:1){id,lineNumber,type{id,fullName,fullPath}}}");
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody(body.toString()))
                .respond(HttpResponse.response()
                        .withBody("{\n" +
                                "  \"data\": {\n" +
                                "    \"breakpoints\": [\n" +
                                "      {\n" +
                                "        \"id\": 9,\n" +
                                "        \"lineNumber\": 177,\n" +
                                "        \"type\": {\n" +
                                "          \"id\": 6,\n" +
                                "          \"fullName\": \"org.jabref.gui.preview.PreviewPanel.java\",\n" +
                                "          \"fullPath\": \"/home/vincent/Documents/jabref/src/main/java/org/jabref/gui/preview/PreviewPanel.java\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      {\n" +
                                "        \"id\": 77,\n" +
                                "        \"lineNumber\": 918,\n" +
                                "        \"type\": {\n" +
                                "          \"id\": 74,\n" +
                                "          \"fullName\": \"javafx.scene.web.WebEngine.java\",\n" +
                                "          \"fullPath\": \"/home/vincent/.gradle/caches/modules-2/files-2.1/org.openjfx/javafx-web/14/c729e901124c8c36afe584b3fd4b40f650b1dbfe/javafx-web-14-sources.jar!/javafx/scene/web/WebEngine.java\"\n" +
                                "        }\n" +
                                "      }]}}"));
    }

    @Test
    void getRecommendedMethodsTest() {
        ArrayList<Method> recommendedMethods = recommendationService.getRecommendedMethods(1);

        assertThat(recommendedMethods, hasSize(1));
    }

    @Test
    void getBreakpointsBySessionIdTest() {
        ArrayList<Breakpoint> breakpoints = recommendationService.getBreakpointsBySessionId(1);

        assertThat(breakpoints, hasSize(2));
        assertEquals(breakpoints.get(0).getType().getFullName(), "org.jabref.gui.preview.PreviewPanel.java");
        assertEquals(breakpoints.get(1).getLineNumber(), 918);
    }


}
