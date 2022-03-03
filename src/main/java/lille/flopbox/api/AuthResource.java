package lille.flopbox.api;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("auth")
public class AuthResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String login(@HeaderParam("username") String username, @HeaderParam("password") String password) {

        return "Log in !";
    }
}
