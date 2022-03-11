package lille.flopbox.api.resourses;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lille.flopbox.api.FileManager;
import lille.flopbox.api.User;
import lille.flopbox.api.UsersList;
import lille.flopbox.api.auth.Secured;

@Path("alias")
public class AliasResource {

    @GET
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAlias(@HeaderParam("Authorization") String authHeader)
    {
        String username = FileManager.getUsernameFromAuth(authHeader);
        return Response.status(Status.OK).entity(UsersList.getInstance().getServeursByUsername(username)).build();
    }
    
    @Path("{alias}")
    @GET
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServeurByAlias(@HeaderParam("Authorization") String authHeader, @PathParam("alias") String alias)
    {
        String username = FileManager.getUsernameFromAuth(authHeader);
        User u = UsersList.getInstance().getUserByUsername(username);
        return Response.status(Status.OK).entity(u.serveurs.get(alias)).build();
    }

}
