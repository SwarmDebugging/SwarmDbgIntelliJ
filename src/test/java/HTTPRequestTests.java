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
@MockServerSettings(ports = {8080})
public class HTTPRequestTests {

    private final ClientAndServer client;
    private JSONObject response;

    public HTTPRequestTests(ClientAndServer client) {
        this.client = client;
        this.client.when(HttpRequest.request()
                .withMethod("POST")
                .withPath("/test")
                .withBody("{\"query\":\"test\"}"))
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
        httpRequest.setUrl("http://localhost:" + client.getLocalPort() + "/test");
        httpRequest.setQuery("test");
        response = httpRequest.post();
    }

    String getResponseBody() {
        return response.getString("body");
    }
}