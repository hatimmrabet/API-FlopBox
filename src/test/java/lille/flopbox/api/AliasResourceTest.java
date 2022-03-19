package lille.flopbox.api;

import static org.junit.Assert.assertEquals;

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

public class AliasResourceTest {
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
        Response responseMsg = target.path("alias").request().get(Response.class);
        assertEquals(401, responseMsg.getStatus());
    }

    /**
     * Test with incorrect auth header
     */
    @Test
    public void testWithWrongAuthHeader() {
        Response responseMsg = target.path("alias").request().header("Authorization","Basic kldkdsvsfvds").get(Response.class);
        assertEquals(401, responseMsg.getStatus());
    }

    /**
     * Test with correct auth header
     */
    @Test
    public void testWithCorrectAuthHeader() {
        Response responseMsg = target.path("alias").request().header("Authorization","Basic aGF0aW06MTIzNDU=").get(Response.class);
        assertEquals(200, responseMsg.getStatus());
    }

    /**
     * Test ajout d'un nouveau alias
     */
    @Test
    public void testCreateAlias() {
        Form formulaire = new Form();
        formulaire.asMap().add("alias", "testServeur");
        formulaire.asMap().add("serveur", "serveur.ftp");
        Response responseMsg = target.path("alias").request().header("Authorization","Basic aGF0aW06MTIzNDU=").post(Entity.form(formulaire));
        assertEquals(201, responseMsg.getStatus());
        responseMsg = target.path("alias/testServeur").request().header("Authorization","Basic aGF0aW06MTIzNDU=").get();
        assertEquals(200, responseMsg.getStatus());
    }

    /**
     * Test modification d'un alias
     */
    @Test
    public void testModifAlias() {
        Form newform = new Form();
        newform.asMap().add("serveur","new.serveur.ftp");
        Response responseMsg = target.path("alias/testServeur").request().header("Authorization","Basic aGF0aW06MTIzNDU=").put(Entity.form(newform));
        assertEquals(200, responseMsg.getStatus());
    }
    

    /**
     * Test delete d'un alias
     */
    @Test
    public void testDeleteAlias() {
        Response responseMsg = target.path("alias/testServeur").request().header("Authorization","Basic aGF0aW06MTIzNDU=").delete();
        assertEquals(404, responseMsg.getStatus());
        Form formulaire = new Form();
        formulaire.asMap().add("alias", "testServeur");
        formulaire.asMap().add("serveur", "serveur.ftp");
        responseMsg = target.path("alias").request().header("Authorization","Basic aGF0aW06MTIzNDU=").post(Entity.form(formulaire));
        assertEquals(201, responseMsg.getStatus());
        responseMsg = target.path("alias/testServeur").request().header("Authorization","Basic aGF0aW06MTIzNDU=").delete();
        assertEquals(200, responseMsg.getStatus());
    }

}
