package lille.flopbox.api.resourses;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lille.flopbox.api.User;
import lille.flopbox.api.UsersList;

@Path("users")
public class UserResource {
    
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewUser(@FormParam("username") String username, @FormParam("password") String password)
    {
        User newUser = new User(username, password);
        if(UsersList.getInstance().addUser(newUser))
        {
            return Response.status(Status.CREATED).entity(newUser.getUserJson()).build();
        }
        JsonObject error_msg = Json.createObjectBuilder().add("error","username exist already.").build();
        return Response.status(406).entity(error_msg).build();
    }
}
