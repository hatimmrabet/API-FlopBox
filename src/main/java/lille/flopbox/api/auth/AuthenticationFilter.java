package lille.flopbox.api.auth;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import lille.flopbox.api.FileManager;

/**
 * Filtre de Connection, protection des end points privés.
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String auth = requestContext.getHeaderString("Authorization");
        if (!FileManager.canAccess(auth)) {
            requestContext.abortWith(
                    Response.status(Status.UNAUTHORIZED).entity("Protected endpoint: You can not acces it.").build());
        }
    }
}
