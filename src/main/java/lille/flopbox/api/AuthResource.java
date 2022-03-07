package lille.flopbox.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.inject.hk2.RequestContext;

@Path("auth")
public class AuthResource {

    /**
     * Se connecter à notre Platforme
     * @param username username d'utilisateur du notre platforme
     * @param password password d'utilisateur du notre platforme
     * @return Token de connexion
     * @throws IOException
     * @throws FileNotFoundException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@HeaderParam("username") String username, @HeaderParam("password") String password){
        ArrayList<String> content;
        try {
            content = FileManager.getFileContent("passwd.txt");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File password.txt not found");
        } catch (IOException e) {
            throw new RuntimeException("IOExecption : "+e.getMessage());
        }

        for(String elem : content)
        {
            if(elem.equals(username+":"+password))
            {
                return Response.status(200).entity(Base64.getEncoder().encodeToString(elem.getBytes())).build();
            }
        }
        return Response.status(401).entity("username ou/et mot de passe sont incorrctes").build();
    }
}
