package lille.flopbox.api.resourses;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
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
        return Response.status(Status.OK).entity(u.getServeurs().get(alias)).build();
    }

    @POST
    @Secured
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addServeur(@HeaderParam("Authorization") String authHeader, @FormParam("alias") String alias, @FormParam("serveur") String serveur)
    {
        if(alias ==  null || serveur == null)
            return Response.status(Status.BAD_REQUEST).entity(Json.createObjectBuilder().add("error","missing fields.").build()).build();

        String username = FileManager.getUsernameFromAuth(authHeader);
        User u = UsersList.getInstance().getUserByUsername(username);
        if(u.getServeurs().containsKey(alias))
        {
            JsonObject msg = Json.createObjectBuilder().add("error","alias exist already. To modify it's value, please use PUT.").build();
            return Response.status(Status.NOT_ACCEPTABLE).entity(msg).build();
        }
        else
        {
            u.addServeur(alias, serveur);
            return Response.status(Status.CREATED).entity(UsersList.getInstance().getServeursByUsername(username)).build();
        }
    }
}
