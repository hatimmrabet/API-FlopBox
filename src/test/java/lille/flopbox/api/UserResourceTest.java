package lille.flopbox.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserResourceTest {
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
     * Test to check successful creation of user.
     */
    @Test
    public void testSuccessCreationUser() {
        Form formulaire = new Form();
        formulaire.asMap().add("username", "hahah");
        formulaire.asMap().add("password", "12345");
        Response responseMsg = target.path("users").request().post(Entity.form(formulaire));
        assertEquals(201, responseMsg.getStatus());
    }

    /**
     * Tester la failure de creation d'un user s'il existe auparavant.
     */
    @Test
    public void testFailedCreationUser() {
        Form formulaire = new Form();
        formulaire.asMap().add("username", "hatim");
        formulaire.asMap().add("password", "12345");
        Response responseMsg = target.path("users").request().post(Entity.form(formulaire));
        assertEquals(400, responseMsg.getStatus());
        assertNotNull(responseMsg.getEntity());
    }
}
