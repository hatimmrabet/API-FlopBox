package lille.flopbox.api;

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

@Path("auth")
public class AuthResource {

    /**
     * Se connecter à notre Platforme
     * @param username username d'utilisateur du notre platforme
     * @param password password d'utilisateur du notre platforme
     * @return Token de connexion
     */
    @GET
    @Path("login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@HeaderParam("username") String username, @HeaderParam("password") String password){
        String encoded = Base64.getEncoder().encodeToString((username+":"+password).getBytes());
        if(FileManager.canAccess(encoded))
        {
            JsonObject js = Json.createObjectBuilder().add("Authorization","Basic "+encoded).build();
            return Response.status(200).entity(js.toString()).build();
        }
        return Response.status(Status.BAD_REQUEST).entity("username ou/et mot de passe sont incorrctes").build();
    }
}
