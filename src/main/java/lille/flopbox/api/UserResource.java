package lille.flopbox.api;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("users")
public class UserResource {
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewUser(@FormParam("username") String username, @FormParam("password") String password)
    {
        User newUser = new User(username, password);
        if(UsersList.getInstance().addUser(newUser))
        {
            return Response.status(Status.CREATED).entity(newUser.getUserJson().asJsonArray()).build();
        }
        return Response.status(406).entity("username exist already.").build();
    }
}
