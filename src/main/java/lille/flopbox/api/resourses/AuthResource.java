package lille.flopbox.api.resourses;

import java.util.Base64;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lille.flopbox.api.FileManager;

@Path("auth")
public class AuthResource {

    /**
     * Connection à la platforme
     * 
     * @param username : username d'utilisateur
     * @param password : son mot de passe
     * @return Reponse 200 si les données sont correctes, 400 sinon.
     */
    @GET
    @Path("login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@HeaderParam("username") String username, @HeaderParam("password") String password) {
        String encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        if (FileManager.canAccess(encoded)) {
            JsonObject js = Json.createObjectBuilder().add("Authorization", "Basic " + encoded).build();
            return Response.status(200).entity(js.toString()).build();
        }
        return Response.status(Status.BAD_REQUEST).entity("username ou/et mot de passe sont incorrctes").build();
    }
}
