package lille.flopbox.api;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;


@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String auth = requestContext.getHeaderString("Authorization");
        System.out.println(auth);
        if(!FileManager.canAccess(auth))
        {
            requestContext.abortWith(Response.status(Status.UNAUTHORIZED).entity("Protected endpoint: You can not acces it.").build());
        }
    }
}
