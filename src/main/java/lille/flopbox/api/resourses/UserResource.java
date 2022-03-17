package lille.flopbox.api.resourses;

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

    /**
     * Creation d'un nouveau utilisateur
     * 
     * @param username : username d'utilisateur
     * @param password : le mot de passe pour se connecter à la platforme
     * @return une Reponse 201 s'il est crée, 400 sinon.
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewUser(@FormParam("username") String username, @FormParam("password") String password) {
        User newUser = new User(username, password);
        if (UsersList.getInstance().addUser(newUser)) {
            return Response.status(Status.CREATED).entity(newUser.getUserJson()).build();
        }
        return Response.status(Status.BAD_REQUEST).entity("username exist already.").build();
    }
}
