package lille.flopbox.api;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.simple.parser.ParseException;

@Path("alias")
public class AliasResource {

    @GET
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAlias(@HeaderParam("Authorization") String authHeader) throws ParseException
    {
        String username = FileManager.getUsername(authHeader);
        return Response.status(Status.OK).entity(FileManager.getServeursByUsername(username).toJSONString()).build();
    }
    
}
