package lille.flopbox.api;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServeurFTPResourceTest {
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
     * Test to enter protected end point
     */
    @Test
    public void testWithMissingAuthHeader() {
        Response responseMsg = target.path("lille/files-details").request().get(Response.class);
        assertEquals(401, responseMsg.getStatus());
    }

  
    /**
     * Test to enter list files without passing usernme and password
     */
    @Test
    public void testWithMissingServerLogin() {
        Response responseMsg = target.path("lille/files-details").request().header("Authorization","Basic aGF0aW06MTIzNDU=").get(Response.class);
        assertEquals(400, responseMsg.getStatus());
    }

    /**
     * Test to enter list files without passing usernme and password
     */
    @Test
    public void testWithCorrectData() {
        Response responseMsg = target.path("lille/files-details").request().header("Authorization","Basic aGF0aW06MTIzNDU=")
                .header("username", "mrabetelkhom").header("password", "Hatimtim123").get(Response.class);
        assertEquals(200, responseMsg.getStatus());
    }
}
