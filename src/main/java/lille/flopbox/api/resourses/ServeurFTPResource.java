package lille.flopbox.api.resourses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import lille.flopbox.api.FileManager;
import lille.flopbox.api.Main;
import lille.flopbox.api.Serveur;
import lille.flopbox.api.User;
import lille.flopbox.api.UsersList;
import lille.flopbox.api.auth.Secured;

/***
 * Gestion des commandes des serveurs FTP.
 */
@Path("{alias}")
public class ServeurFTPResource {

    /**
     * Lister tous les fichiers et leurs details contenus dans un Path.
     * 
     * @param authHeader : Token d'authentification
     * @param alias      : alias du serveur
     * @param username   : nom d'utilisateur
     * @param password   : mot de passe d'utilisateur
     * @param path       : le path des fichiers à lister
     * @return reponse json avec le details de chaque fichier.
     */
    @GET
    @Secured
    @Path("files-details{path: (/.*)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFiles(@HeaderParam("Authorization") String authHeader,
            @PathParam("alias") String alias,
            @HeaderParam("username") String username,
            @HeaderParam("password") String password,
            @PathParam("path") String path) {

        if (username == null || password == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing Headers.").build();

        User u = UsersList.getInstance().getUserByUsername(FileManager.getUsernameFromAuth(authHeader));
        Serveur serveur = u.getServeurs().get(alias);

        if (serveur == null)
            return Response.status(Status.NOT_FOUND).entity("Alias '" + alias + "' not found.").build();

        if (path.equals(""))
            path = ".";

        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(serveur.url, serveur.port);
            // Verifier connection au serveur
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Connection to server Failed").build();
            }
            // entering passive mode
            ftp.enterLocalPassiveMode();
            // connection
            if (!ftp.login(username, password)) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Username ou mot de passe sont incorrectes.").build();
            }
            // verifier le path
            if (!ftp.changeWorkingDirectory(path)) {
                ftp.disconnect();
                return Response.status(Status.NOT_FOUND).entity("The path : " + path + " is not a directory.").build();
            }
            // recuperation des noms des fichiers
            JsonObject files = getFilesDetails(ftp, path, alias);
            ftp.logout();
            ftp.disconnect();
            return Response.status(Status.OK).entity(files).build();
        } catch (IOException e) {
            return Response.status(Status.BAD_REQUEST).entity("Exception : " + e.getMessage()).build();
        }
    }

    /**
     * Tableau Json avec chaque fichiers et ses details, et lien pour le telecharger
     * 
     * @param ftp   : ftp client
     * @param path  : path vers le fichier sur le serveur FTP
     * @param alias : alias du serveur.
     * @return Tableau Json
     */
    public JsonObject getFilesDetails(FTPClient ftp, String path, String alias) {
        FTPFile[] files;
        DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JsonObjectBuilder ret = Json.createObjectBuilder();
        try {
            files = ftp.listFiles(path);
            for (FTPFile file : files) {
                JsonObjectBuilder jsb = Json.createObjectBuilder();
                jsb.add("name", file.getName());
                jsb.add("size", file.getSize());
                jsb.add("date", dateFormater.format(file.getTimestamp().getTime()));
                jsb.add("path", path + file.getName());
                if (file.isDirectory()) {
                    jsb.add("type","directory");
                    jsb.add("download", Main.BASE_URI + alias + "/directory" + path + file.getName());
                    jsb.add("content", getFilesDetails(ftp, path + file.getName() + "/", alias));
                } else {
                    jsb.add("type","file");
                    jsb.add("download", Main.BASE_URI + alias + "/file" + path + file.getName());
                }
                ret.add(file.getName(), jsb.build());
            }
        } catch (IOException e1) {
            System.out.println(e1.getMessage());
        }
        return ret.build();
    }

    /**
     * Creation d'un dossier.
     * 
     * @param authHeader : Token d'authentification
     * @param alias      : alias du serveur
     * @param username   : nom d'utilisateur
     * @param password   : mot de passe d'utilisateur
     * @param path       : le path du creation du fichier
     * @return Reponse Http avec message du creation ou d'erreur
     */
    @POST
    @Secured
    @Path("empty-directory/{path: .*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response makeDirectory(@HeaderParam("Authorization") String authHeader,
            @PathParam("alias") String alias,
            @HeaderParam("username") String username,
            @HeaderParam("password") String password,
            @PathParam("path") String path) {

        if (username == null || password == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing Headers.").build();
        if (path == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing path.").build();

        User u = UsersList.getInstance().getUserByUsername(FileManager.getUsernameFromAuth(authHeader));
        Serveur serveur = u.getServeurs().get(alias);

        if (serveur == null)
            return Response.status(Status.NOT_FOUND).entity("Alias '" + alias + "' not found.").build();

        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(serveur.url, serveur.port);
            // Verifier connection au serveur
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Connection to server Failed").build();
            }
            // entering passive mode
            ftp.enterLocalPassiveMode();
            // connection
            if (!ftp.login(username, password)) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Username ou mot de passe sont incorrectes.").build();
            }
            // Creation du dossier
            if (!ftp.makeDirectory(path)) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Directory Creation failed.").build();
            }
            ftp.logout();
            ftp.disconnect();
            return Response.status(Status.CREATED).entity("Directory created.").build();
        } catch (IOException e) {
            return Response.status(Status.BAD_REQUEST).entity("Exception : " + e.getMessage()).build();
        }
    }

    /**
     * supprimer un dossier.
     * 
     * @param authHeader : Token d'authentification
     * @param alias      : alias du serveur
     * @param username   : nom d'utilisateur
     * @param password   : mot de passe d'utilisateur
     * @param path       : le path du dossier à supprimer
     * @return Reponse Http avec message du réussite ou d'erreur
     */
    @DELETE
    @Secured
    @Path("file/{path: .*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeFile(@HeaderParam("Authorization") String authHeader,
            @PathParam("alias") String alias,
            @HeaderParam("username") String username,
            @HeaderParam("password") String password,
            @PathParam("path") String path) {

        if (username == null || password == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing Headers.").build();
        if (path == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing path.").build();

        User u = UsersList.getInstance().getUserByUsername(FileManager.getUsernameFromAuth(authHeader));
        Serveur serveur = u.getServeurs().get(alias);

        if (serveur == null)
            return Response.status(Status.NOT_FOUND).entity("Alias '" + alias + "' not found.").build();

        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(serveur.url, serveur.port);
            ftp.setListHiddenFiles(true); // afficher les fichiers cachés
            // Verifier connection au serveur
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Connection to server Failed").build();
            }
            // entering passive mode
            ftp.enterLocalPassiveMode();
            // connection
            if (!ftp.login(username, password)) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Username ou mot de passe sont incorrectes.").build();
            }
            // suppression du dossier et son contenue
            if (!deleteFilesDirectories(ftp, path)) {
                ftp.logout();
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Directory deletion failed.").build();
            }
            ftp.logout();
            ftp.disconnect();
            return Response.status(Status.OK).entity("Directory deleted.").build();
        } catch (IOException e) {
            return Response.status(Status.BAD_REQUEST).entity("Exception : " + e.getMessage()).build();
        }
    }

    /**
     * Fonction de suppression des dossiers et des fichiers.
     * 
     * @param ftp  : client ftp
     * @param path : path du repertoire courant
     * @return true si le fichier est supprimé, false sinon
     */
    public boolean deleteFilesDirectories(FTPClient ftp, String path) {
        try {
            FTPFile[] files = ftp.listFiles(path);
            for (FTPFile file : files) {
                if (file.isDirectory()) {
                    deleteFilesDirectories(ftp, path + "/" + file.getName());
                } else {
                    if (!ftp.deleteFile(path + "/" + file.getName()))
                        return false;
                }
            }
            if (!ftp.removeDirectory(path))
                return false;
        } catch (IOException e) {
            throw new RuntimeException("deleteFiles : " + e.getMessage());
        }
        return true;
    }

    /**
     * rename a folder or a file.
     * 
     * @param authHeader  : Token d'authentification
     * @param alias       : alias du serveur
     * @param username    : nom d'utilisateur
     * @param password    : mot de passe d'utilisateur
     * @param path        : le path vers le fichier à renommer
     * @param oldfilename the file name to change.
     * @param newfilename the name to give.
     * @return Reponse Http avec message du réussite ou d'erreur
     */
    @PUT
    @Secured
    @Path("rename/{path: .*}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response renameFile(@HeaderParam("Authorization") String authHeader,
            @PathParam("alias") String alias,
            @HeaderParam("username") String username,
            @HeaderParam("password") String password,
            @PathParam("path") String path,
            @FormParam("oldname") String oldfilename,
            @FormParam("newname") String newfilename) {

        if (username == null || password == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing Headers.").build();
        if (path == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing path.").build();
        if (oldfilename == null || newfilename == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing Form params.").build();

        User u = UsersList.getInstance().getUserByUsername(FileManager.getUsernameFromAuth(authHeader));
        Serveur serveur = u.getServeurs().get(alias);
        if (path.equals(""))
            path = ".";

        if (serveur == null)
            return Response.status(Status.NOT_FOUND).entity("Alias '" + alias + "' not found.").build();

        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(serveur.url, serveur.port);
            // Verifier connection au serveur
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Connection to server Failed").build();
            }
            // entering passive mode
            ftp.enterLocalPassiveMode();
            // connection
            if (!ftp.login(username, password)) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Username ou mot de passe sont incorrectes.").build();
            }
            // change directory to path
            if (!ftp.changeWorkingDirectory(path)) {
                ftp.logout();
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("The path " + path + " not found.").build();
            }
            // renomer fichier
            if (!ftp.rename(oldfilename, newfilename)) {
                ftp.logout();
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Rename failed.").build();
            }
            ftp.logout();
            ftp.disconnect();
            return Response.status(200).entity("Resource updated successfully.").build();
        } catch (IOException e) {
            return Response.status(Status.BAD_REQUEST).entity("Exception : " + e.getMessage()).build();
        }
    }

    /**
     * Telechargement d'un fichier dans un serveur FTP vers le dossier downloads.
     * 
     * @param authHeader : Token d'authentification
     * @param alias      : alias du serveur
     * @param username   : nom d'utilisateur
     * @param password   : mot de passe d'utilisateur
     * @param path       : le path vers le fichier à telecharger
     * @return Reponse Http avec message du réussite ou d'erreur
     */
    @GET
    @Secured
    @Path("file/{path: .*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response downloadFile(@HeaderParam("Authorization") String authHeader,
            @PathParam("alias") String alias,
            @HeaderParam("username") String username,
            @HeaderParam("password") String password,
            @PathParam("path") String path) {

        if (username == null || password == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing Headers.").build();
        if (path == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing path.").build();
        if (path.equals(""))
            path = ".";

        User u = UsersList.getInstance().getUserByUsername(FileManager.getUsernameFromAuth(authHeader));
        Serveur serveur = u.getServeurs().get(alias);

        if (serveur == null)
            return Response.status(Status.NOT_FOUND).entity("Alias '" + alias + "' not found.").build();

        FTPClient ftp = new FTPClient();
        try {
            ftp.setControlEncoding("UTF-8");
            ftp.connect(serveur.url, serveur.port);
            // Verifier connection au serveur
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Connection to server Failed").build();
            }
            // entering passive mode
            ftp.enterLocalPassiveMode();
            // connection
            if (!ftp.login(username, password)) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Username ou mot de passe sont incorrectes.").build();
            }
            // set files type
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            // extraction du nom du fichier
            String pathParts[] = path.split("/");
            String filename = pathParts[pathParts.length - 1];
            // download du fichiers dans le dossier downloads
            OutputStream output = new FileOutputStream("downloads/" + filename);
            if (!ftp.retrieveFile(path, output)) {
                output.close();
                File file = new File("downloads/" + filename);
                file.delete();
                return Response.status(Status.BAD_REQUEST).entity("File download failed.").build();
            }
            output.close();
            // deconnection du serveur
            ftp.logout();
            ftp.disconnect();
            return Response.status(200).entity(new FileInputStream("downloads/" + filename)).build();
        } catch (IOException e) {
            return Response.status(Status.BAD_REQUEST).entity("Exception : " + e.getMessage()).build();
        }
    }

    /**
     * Telechargement d'un dossier dans un serveur FTP au dossier "downloads"
     * 
     * @param authHeader : Token d'authentification
     * @param alias      : alias du serveur
     * @param username   : nom d'utilisateur
     * @param password   : mot de passe d'utilisateur
     * @param path       : le path vers le dossier à telecharger
     * @return Reponse Http avec message du réussite ou d'erreur
     */
    @GET
    @Secured
    @Path("directory/{path: .*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response downloadDirectory(@HeaderParam("Authorization") String authHeader,
            @PathParam("alias") String alias,
            @HeaderParam("username") String username,
            @HeaderParam("password") String password,
            @PathParam("path") String path) {

        if (username == null || password == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing Headers.").build();
        if (path == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing path.").build();
        if (path.equals(""))
            path = ".";

        User u = UsersList.getInstance().getUserByUsername(FileManager.getUsernameFromAuth(authHeader));
        Serveur serveur = u.getServeurs().get(alias);

        if (serveur == null)
            return Response.status(Status.NOT_FOUND).entity("Alias '" + alias + "' not found.").build();

        FTPClient ftp = new FTPClient();
        try {
            ftp.setControlEncoding("UTF-8");
            ftp.connect(serveur.url, serveur.port);
            // Verifier connection au serveur
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Connection to server Failed").build();
            }
            // entering passive mode
            ftp.enterLocalPassiveMode();
            // connection
            if (!ftp.login(username, password)) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Username ou mot de passe sont incorrectes.").build();
            }
            // set files type
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            // verification si c'est un dossier
            if (!ftp.changeWorkingDirectory(path)) {
                ftp.logout();
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("The path " + path + " not found.").build();
            }
            String pathParts[] = path.split("/");
            String filename = pathParts[pathParts.length - 1];
            if (path == ".")
                filename = alias;
            File dossier = new File("downloads/" + filename);
            if (!dossier.mkdir())
                throw new RuntimeException("Cannot create directory " + dossier.getAbsolutePath());

            // download du fichiers dans le dossier downloads
            String workingDir = ftp.printWorkingDirectory();
            if (!getFilesDirectories(ftp, workingDir, dossier.getCanonicalPath())) {
                ftp.logout();
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Download Directory failed.").build();
            }
            // Zipper le dossier
            FileManager.zip(dossier.getCanonicalPath(), dossier.getCanonicalPath() + ".zip");
            // supprimer le dossier non zippé
            FileManager.DeleteFilesDirectories(dossier);
            // deconnection du serveur
            ftp.logout();
            ftp.disconnect();
            // renvoiyé le inputStream du zip
            return Response.status(200).entity(new FileInputStream(dossier.getCanonicalPath() + ".zip")).build();
        } catch (IOException e) {
            return Response.status(Status.BAD_REQUEST).entity("Exception : " + e.getMessage()).build();
        }
    }

    /**
     * Telecharger un dossier et son contenu.
     * 
     * @param ftp  : client ftp
     * @param path : path du repertoire courant
     * @return true, s'il est telechargé, flase sinon.
     */
    public boolean getFilesDirectories(FTPClient ftp, String remotePath, String downloadPath) {
        try {
            FTPFile[] files = ftp.listFiles(remotePath);
            for (FTPFile file : files) {
                if (file.isDirectory()) {
                    File dossier = new File(downloadPath + "/" + file.getName());
                    // System.out.println(dossier.getAbsolutePath());
                    dossier.mkdir();
                    getFilesDirectories(ftp, remotePath + "/" + file.getName(), downloadPath + "/" + file.getName());
                } else {
                    OutputStream output = new FileOutputStream(downloadPath + "/" + file.getName());
                    if (!ftp.retrieveFile(remotePath + "/" + file.getName(), output)) {
                        output.close();
                        File localfile = new File(downloadPath + "/" + file.getName());
                        localfile.delete();
                        return false;
                    }
                    output.close();
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    /***
     * Uploader un fichier vers un repertoire dans un serveur FTP
     * 
     * @param authHeader          : Token d'authentification
     * @param alias               : alias du serveur
     * @param username            : nom d'utilisateur
     * @param password            : mot de passe d'utilisateur
     * @param path                : le path où on veut uploader le fichier.
     * @param uploadedInputStream : input stream du fichier.
     * @param fileDetail          : details du fichier.
     * @return Reponse Http avec message du réussite ou d'erreur
     */
    @POST
    @Secured
    @Path("file/{path: .*}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@HeaderParam("Authorization") String authHeader,
            @PathParam("alias") String alias,
            @HeaderParam("username") String username,
            @HeaderParam("password") String password,
            @PathParam("path") String path,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {
        System.out.println(fileDetail.getFileName());
        if (username == null || password == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing Headers.").build();
        if (path == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing path.").build();
        if (fileDetail.getFileName().equals(""))
            return Response.status(Status.BAD_REQUEST).entity("Missing file.").build();
        if (path.equals(""))
            path = ".";

        User u = UsersList.getInstance().getUserByUsername(FileManager.getUsernameFromAuth(authHeader));
        Serveur serveur = u.getServeurs().get(alias);

        if (serveur == null)
            return Response.status(Status.NOT_FOUND).entity("Alias '" + alias + "' not found.").build();

        FTPClient ftp = new FTPClient();
        try {
            ftp.setControlEncoding("UTF-8");
            ftp.connect(serveur.url, serveur.port);
            // Verifier connection au serveur
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Connection to server Failed").build();
            }
            // entering passive mode
            ftp.enterLocalPassiveMode();
            // connection
            if (!ftp.login(username, password)) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Username ou mot de passe sont incorrectes.").build();
            }
            // set files type
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            // changer le repertoire actuel
            if (!ftp.changeWorkingDirectory(path)) {
                ftp.logout();
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("The path " + path + " not found.").build();
            }
            // uploader le fichier
            if (ftp.storeFile(fileDetail.getFileName(), uploadedInputStream)) {
                uploadedInputStream.close();
            } else {
                uploadedInputStream.close();
                return Response.status(Status.BAD_REQUEST).entity("File upload failed.").build();
            }
            // deconnection du serveur
            ftp.logout();
            ftp.disconnect();
            return Response.status(200).entity("File uploaded successfully.").build();
        } catch (IOException e) {
            return Response.status(Status.BAD_REQUEST).entity("Exception : " + e.getMessage()).build();
        }
    }

    /***
     * Uploader un dossier vers un repertoire dans un serveur FTP
     * 
     * @param authHeader : Token d'authentification
     * @param alias      : alias du serveur
     * @param username   : nom d'utilisateur
     * @param password   : mot de passe d'utilisateur
     * @param path       : le path où on veut uploader le fichier.
     * @param localpath  : le path vers le dossier en local
     * @return Reponse Http avec message du réussite ou d'erreur
     */
    @POST
    @Secured
    @Path("directory/{path: .*}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFolder(@HeaderParam("Authorization") String authHeader,
            @PathParam("alias") String alias,
            @HeaderParam("username") String username,
            @HeaderParam("password") String password,
            @PathParam("path") String path,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {

        if (username == null || password == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing Headers.").build();
        if (path == null || fileDetail.getFileName().equals(""))
            return Response.status(Status.BAD_REQUEST).entity("Missing path.").build();
        if (path.equals(""))
            path = ".";

        User u = UsersList.getInstance().getUserByUsername(FileManager.getUsernameFromAuth(authHeader));
        Serveur serveur = u.getServeurs().get(alias);

        if (serveur == null)
            return Response.status(Status.NOT_FOUND).entity("Alias '" + alias + "' not found.").build();

        FTPClient ftp = new FTPClient();
        try {
            ftp.setControlEncoding("UTF-8");
            ftp.connect(serveur.url, serveur.port);
            // Verifier connection au serveur
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Connection to server Failed").build();
            }
            // entering passive mode
            ftp.enterLocalPassiveMode();
            // connection
            if (!ftp.login(username, password)) {
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("Username ou mot de passe sont incorrectes.").build();
            }
            // set files type
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            // changer le repertoire actuel
            if (!ftp.changeWorkingDirectory(path)) {
                ftp.logout();
                ftp.disconnect();
                return Response.status(Status.BAD_REQUEST).entity("The path " + path + " not found.").build();
            }
            // unzipper ll'archive
            FileManager.unzip(uploadedInputStream);
            File fileUpload = new File("./downloads/" + fileDetail.getFileName().replace(".zip", ""));
            if (!fileUpload.exists())
                return Response.status(Status.NOT_FOUND)
                        .entity("File in path:" + fileUpload.getAbsolutePath() + " not found.").build();
            // uploader le dossier
            if (!uploadFilesDirectories(ftp, ftp.printWorkingDirectory(), fileUpload.getCanonicalPath())) {
                FileManager.DeleteFilesDirectories(fileUpload);
                return Response.status(Status.BAD_REQUEST).entity("Directory upload failed.").build();
            }
            // deconnection du serveur
            ftp.logout();
            ftp.disconnect();
            // suprimmer le dossier sur le serveur
            FileManager.DeleteFilesDirectories(fileUpload);
            return Response.status(200).entity("Directory uploaded successfully.").build();
        } catch (IOException e) {
            return Response.status(Status.BAD_REQUEST).entity("Exception : " + e.getMessage()).build();
        }
    }

    /**
     * Uploader un dossier et son contenu.
     * 
     * @param ftp       : client ftp
     * @param ftppath   : path du repertoire sur le serveur ftp
     * @param localpath : path du repertoire au local
     * @return true, s'il est uploadé, flase sinon.
     */
    public boolean uploadFilesDirectories(FTPClient ftp, String ftppath, String localpath) {
        File fileUpload = new File(localpath);
        try {
            if (fileUpload.isDirectory()) {
                if (!ftp.makeDirectory(ftppath + "/" + fileUpload.getName()))
                    return false;
                for (File f : fileUpload.listFiles()) {
                    if (!uploadFilesDirectories(ftp, ftppath + "/" + fileUpload.getName(),
                            localpath + "/" + f.getName()))
                        return false;
                }
            } else {
                FileInputStream uploadedInputStream = new FileInputStream(fileUpload);
                if (!ftp.storeFile(ftppath + "/" + fileUpload.getName(), uploadedInputStream)) {
                    uploadedInputStream.close();
                    return false;
                }
                uploadedInputStream.close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

}