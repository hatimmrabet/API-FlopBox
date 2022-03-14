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
     * Lister tous les dossiers du repertoire courant.
     * @param authHeader
     * @param alias
     * @param username
     * @param password
     * @return reponse json avec le details de chaque fichier.
     */
    @GET
    @Secured
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cmdList(@HeaderParam("Authorization") String authHeader,
            @PathParam("alias") String alias,
            @HeaderParam("username") String username,
            @HeaderParam("password") String password) {
        return cmdListWithPath(authHeader, alias, username, password, "");
    }

    /**
     * Lister tous les fichiers contenues dans un Path.
     * @param authHeader
     * @param alias
     * @param username
     * @param password
     * @param path le path des fichiers à lister
     * @return reponse json avec le details de chaque fichier.
     */
    @GET
    @Secured
    @Path("list/{path: .*}")
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

        if(path.equals(""))
            path = ".";

        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(serveur);
            // Verifier connection au serveur
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Connection to server Failsed").build();
            }
            // entering passive mode
            ftp.enterLocalPassiveMode(); 
            // connection
            if (!ftp.login(username, password))
            {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Username ou mot de passe sont incorrectes.").build();
            }
            // verifier le path
            if(!ftp.changeWorkingDirectory(path))
            {
                ftp.disconnect();
                return Response.status(Status.NOT_FOUND).entity("The path : "+path+" is not a directory.").build();
            }
            // recuperation des noms des fichiers
            FTPFile[] fichiers = ftp.listFiles();
            ftp.logout();
            ftp.disconnect();
            return Response.status(Status.OK).entity(getFilesDetails(fichiers)).build();
        } catch (IOException e) {
            throw new RuntimeException("cmdList : " + e.getMessage());
        }
    }

    public JsonArray getFilesDetails(FTPFile[] files) {
        DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JsonArrayBuilder ret = Json.createArrayBuilder();
        for (FTPFile file : files) {
            JsonObjectBuilder jsb = Json.createObjectBuilder();
            jsb.add("name", file.getName());
            jsb.add("size", file.getSize());
            jsb.add("date", dateFormater.format(file.getTimestamp().getTime()));
            ret.add(jsb.build());
        }
        return ret.build();
    }



}