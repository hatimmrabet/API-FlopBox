package lille.flopbox.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.Collection;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;

public class FileManager {

    /**
     * Verifier si l'utilisateur peut se connecter
     * 
     * @param authHeader : Token d'authentification
     * @return True si le token correspond à un utilisateur existant, False sinon.
     */
    public static boolean canAccess(String authHeader) {
        if (authHeader == null || authHeader.length() == 0)
            return false;

        String auth;
        Collection<User> users;

        if (authHeader.startsWith("Basic"))
            auth = authHeader.substring("Basic".length()).trim();
        else
            auth = authHeader;

        users = UsersList.getInstance().getUsers().values();
        for (User u : users) {
            if (u.getAuth().equals(auth)) {
                return true;
            }
        }
        return false;
    }

    /**
     * extraire le username a partir du Token de basic authentification
     * 
     * @param authHeader : Token d'authentification
     * @return le username contenue dans le token
     */
    public static String getUsernameFromAuth(String authHeader) {
        String username;
        authHeader = authHeader.substring("Basic".length()).trim();
        authHeader = new String(Base64.getDecoder().decode(authHeader.getBytes()));
        username = authHeader.split(":")[0];
        return username;
    }

    /**
     * recuperer les donnees du fichier json sous format d'objet json
     * 
     * @param filename : le nom du fichier json
     * @return un objet Json avec le contenu du fichier passé en parametre
     */
    public static JsonObject getJsonFileContent(String filename) {
        JsonObject content = Json.createObjectBuilder().build();
        try {
            JsonReader reader = Json.createReader(new FileInputStream(new File(filename)));
            content = reader.readObject();
            reader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFoundException " + e.getMessage());
        }
        return content;
    }

    /**
     * Sauvegarder les utilisateurs dans un fichier json "users.json"
     * 
     * @param filename : nom du fichier json
     */
    public static void saveFileToJson(String filename) {
        try {
            JsonWriter writer = Json.createWriter(new FileOutputStream(filename));
            writer.writeObject(UsersList.getInstance().getUsersJSON());
            writer.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFoundException " + e.getMessage());
        }
    }
}
