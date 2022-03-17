package lille.flopbox.api;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class AuthResourceTest {

    private HttpServer server;
    private WebTarget target;

    @Before
    public void setUp() throws Exception {
        // start the server
        server = Main.startServer();
        // create the client
        Client c = ClientBuilder.newClient();

        // uncomment the following line if you want to enable
        // support for JSON in the client (you also have to uncomment
        // dependency on jersey-media-json module in pom.xml and Main.startServer())
        // --
        // c.configuration().enable(new
        // org.glassfish.jersey.media.json.JsonJaxbFeature());

        target = c.target(Main.BASE_URI);
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    /**
     * Test to check successful login.
     */
    @Test
    public void testSuccessLogin() {
        Response responseMsg = target.path("auth/login").request().header("username", "hatim")
                .header("password", "12345").get(Response.class);
        assertEquals(200, responseMsg.getStatus());
    }

    /**
     * Test to check failed login.
     */
    @Test
    public void testFailedLogin() {
        Response responseMsg = target.path("auth/login").request().header("username", "hatim")
                .header("password", "----").get(Response.class);
        assertEquals(401, responseMsg.getStatus());
    }
}
