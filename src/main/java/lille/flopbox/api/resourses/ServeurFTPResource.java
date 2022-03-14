package lille.flopbox.api.resourses;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import lille.flopbox.api.FileManager;
import lille.flopbox.api.User;
import lille.flopbox.api.UsersList;
import lille.flopbox.api.auth.Secured;

@Path("{alias}")
public class ServeurFTPResource {

    /**
     * endpoint pour se connecter
     * 
     * @param authHeader
     * @param alias
     * @param username
     * @param password
     * @return
     */
    @GET
    @Secured
    @Path("login")
    public Response login(@HeaderParam("Authorization") String authHeader,
            @PathParam("alias") String alias,
            @HeaderParam("username") String username,
            @HeaderParam("password") String password) {

        if (username == null || password == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing Headers.").build();

        User u = UsersList.getInstance().getUserByUsername(FileManager.getUsernameFromAuth(authHeader));
        String serveur = u.getServeurs().get(alias);

        if (serveur == null)
            return Response.status(Status.NOT_FOUND).entity("Alias '" + alias + "' not found.").build();

        FTPClient ftp = new FTPClient();

        try {
            ftp.connect(serveur);
            System.out.println("Connected to " + serveur + ".");
            System.out.print(ftp.getReplyString());
            // Verifier connection au serveur
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Connection to server Failsed").build();
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        // connection
        try {
            if (ftp.login(username, password)) {
                ftp.logout();
                ftp.disconnect();
                return Response.status(Status.OK).entity("Connected").build();
            } else {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Username ou mot de passe sont incorrectes.").build();
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @GET
    @Secured
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cmdList(@HeaderParam("Authorization") String authHeader,
            @PathParam("alias") String alias,
            @HeaderParam("username") String username,
            @HeaderParam("password") String password,
            @PathParam("path") String path)
    {
        return cmdListWithPath(authHeader, alias, username, password, path);
    }

    @GET
    @Secured
    @Path("list/{path}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cmdListWithPath(@HeaderParam("Authorization") String authHeader,
            @PathParam("alias") String alias,
            @HeaderParam("username") String username,
            @HeaderParam("password") String password,
            @PathParam("path") String path) {

        if (username == null || password == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing Headers.").build();

        User u = UsersList.getInstance().getUserByUsername(FileManager.getUsernameFromAuth(authHeader));
        String serveur = u.getServeurs().get(alias);

        if (serveur == null)
            return Response.status(Status.NOT_FOUND).entity("Alias '" + alias + "' not found.").build();

        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(serveur);
            // Verifier connection au serveur
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Connection to server Failsed").build();
            }
            ftp.enterLocalPassiveMode();    //entering passive mode
            // connection
            if (!ftp.login(username,password)) {
                return Response.status(Status.BAD_REQUEST).entity("Username ou mot de passe sont incorrectes.").build();
            }
            //recuperation des noms des fichiers
            FTPFile[] fichiers = ftp.listFiles(path);
            ftp.logout();
            ftp.disconnect();
            return Response.status(Status.OK).entity(getFilesDetails(fichiers)).build();
        } catch (IOException e) {
            throw new RuntimeException("cmdList : "+e.getMessage());
        }
    }
    
    public JsonArray getFilesDetails(FTPFile[] files) {
        DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JsonArrayBuilder ret = Json.createArrayBuilder();
        for (FTPFile file : files) {
            JsonObjectBuilder jsb = Json.createObjectBuilder();
            jsb.add("name", file.getName());
            jsb.add("size",file.getSize());
            jsb.add("date", dateFormater.format(file.getTimestamp().getTime()));
            ret.add(jsb.build());
        }
        return ret.build();
    }





}