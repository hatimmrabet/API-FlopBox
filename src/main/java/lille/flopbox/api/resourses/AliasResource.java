package lille.flopbox.api.resourses;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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

    /**
     * Tous les serveurs d'utilisateur
     * @param authHeader
     * @return
     */
    @GET
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAlias(@HeaderParam("Authorization") String authHeader)
    {
        String username = FileManager.getUsernameFromAuth(authHeader);
        return Response.status(Status.OK).entity(UsersList.getInstance().getServeursByUsername(username)).build();
    }
    
    /**
     * avoir le nom du serveur à partir de son alias
     * @param authHeader
     * @param alias
     * @return
     */
    @Path("{alias}")
    @GET
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServeurByAlias(@HeaderParam("Authorization") String authHeader, @PathParam("alias") String alias)
    {
        String username = FileManager.getUsernameFromAuth(authHeader);
        User u = UsersList.getInstance().getUserByUsername(username);
        if(u.getServeurs().containsKey(alias))
            return Response.status(Status.OK).entity(u.getServeurs().get(alias)).build();
        else
            return Response.status(Status.NOT_FOUND).entity(alias+" alias not found.").build();
    }

    /**
     * Ajouter un serveur à ma liste des serveur s'il n'existe pas
     * @param authHeader
     * @param alias
     * @param serveur
     * @return
     */
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
        // verifier l'existance du serveur
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

    /**
     * modifier un alias qui existe deja ou creer un nouveau sinon
     * @param authHeader
     * @param alias
     * @param serveur
     * @return
     */
    @PUT
    @Secured
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifierServeur(@HeaderParam("Authorization") String authHeader, @FormParam("alias") String alias, @FormParam("serveur") String serveur)
    {
        if(alias ==  null || serveur == null)
            return Response.status(Status.BAD_REQUEST).entity(Json.createObjectBuilder().add("error","missing fields.").build()).build();

        String username = FileManager.getUsernameFromAuth(authHeader);
        User u = UsersList.getInstance().getUserByUsername(username);
        // verifier l'existance du serveur
        if(u.getServeurs().containsKey(alias))
        {
            u.addServeur(alias, serveur);
            return Response.status(Status.OK).entity("Modified.").build();
        }
        else
        {
            u.addServeur(alias, serveur);
            return Response.status(Status.CREATED).entity(UsersList.getInstance().getServeursByUsername(username)).build();
        }
    }

    @DELETE
    @Path("{alias}")
    @Secured
    @Produces(MediaType.TEXT_HTML)
    public Response modifierServeur(@HeaderParam("Authorization") String authHeader,  @PathParam("alias") String alias)
    {
        
        String username = FileManager.getUsernameFromAuth(authHeader);
        User u = UsersList.getInstance().getUserByUsername(username);
        if(u.getServeurs().containsKey(alias))
        {
            u.getServeurs().remove(alias);
            return Response.status(Status.OK).entity("Deleted.").build();
        } else {
            return Response.status(Status.NOT_FOUND).entity(alias + " alias not Found.").build();
        }
    }
}
