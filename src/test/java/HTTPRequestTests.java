import com.swarm.utils.HTTPRequest;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {8080}, perTestSuite = true)
public class HTTPRequestTests {

    private final ClientAndServer client;
    private JSONObject response;

    public HTTPRequestTests(ClientAndServer client) {
        this.client = client;
        setupSimplePostRequest();
        setupPostRequestWithVariables();
    }

    private void setupSimplePostRequest() {
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody("{\"query\":\"test\"}"))
                .respond(
                        HttpResponse.response()
                                .withBody("test successful")
                );
    }

    private void setupPostRequestWithVariables() {
        client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/graphql")
                .withBody("{\"variables\":{\"test\":123},\"query\":\"test\"}"))
                .respond(
                        HttpResponse.response()
                                .withBody("test successful")
                );
    }

    @Test
    void simpleUnirestPostTest() {
        sendPostRequest();

        assertEquals("test successful", getResponseBody());
    }

    void sendPostRequest(){
        HTTPRequest httpRequest = new HTTPRequest();
        httpRequest.setQuery("test");
        response = httpRequest.post();
    }

    @Test
    void simpleVariablesRequestTest() {
        sendPostRequestWithVariables();

        assertEquals("test successful", getResponseBody());
    }

    private void sendPostRequestWithVariables() {
        HTTPRequest httpRequest = new HTTPRequest();
        httpRequest.setQuery("test");
        httpRequest.addVariable("test", 123);
        response = httpRequest.post();
    }

    String getResponseBody() {
        return response.getString("body");
    }
}