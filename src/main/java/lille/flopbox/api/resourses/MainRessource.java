package lille.flopbox.api.resourses;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
public class MainRessource {
    
    @GET
    public Response showEndPoints()
    {
        JsonObjectBuilder jsb = Json.createObjectBuilder();

        jsb.add("GET /auth/login", "Pour se connecter.");

        jsb.add("GET /alias/", "Afficher tous les serveurs d'utilisateurs.");
        jsb.add("GET /alias/:alias", "Afficher l'url du serveur FTP lié à cet alias.");
        jsb.add("POST /alias/", "Creer un nouveau serveur.");
        jsb.add("PUT /alias/:alias", "Modifier un serveur.");
        jsb.add("DELETE /alias/:alias", "Supprimer un serveur.");

        jsb.add("GET /:alias/files-details/:path", "Afficher le details des fichiers sur le serveur avec lien de telechargement.");
        jsb.add("POST /:alias/empty-directory/:path", "creer un dossier vide dans le path specifie sur le serveur.");
        jsb.add("GET /:alias/file/:path", "Telecharger un fichier en local.");
        jsb.add("GET /:alias/directory/:path", "Telecharger un dossier en local.");
        jsb.add("POST /:alias/file/:path", "Uploadé un fichier en local.");
        jsb.add("POST /:alias/directory/:path", "Uploadé un dossier en local.");
        jsb.add("PUT /:alias/rename/:path", "Renommer un fichier ou un dossier.");
        jsb.add("DELETE /:alias/file/:path", "supprimer un dossier ou fichier dans le path specifie sur le serveur.");
        
        return Response.ok(jsb.build()).build();
    }
}
