package lille.flopbox.api;

import java.util.Base64;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class User {

    private String username;
    private String password;
    private String auth;
    private HashMap<String, String> serveurs;

    /**
     * Creation d'un User
     * 
     * @param username : username de l'utilisateur
     * @param pass     : le mot de passe de l'utilisateur
     */
    public User(String username, String pass) {
        this.username = username;
        this.password = pass;
        this.auth = new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));
        this.serveurs = new HashMap<String, String>();
    }

    /**
     * Construire un User à partir du objet json.
     * 
     * @param json : objet Json represantant l'utilisateur.
     */
    User(JsonObject json) {
        this.username = json.getString("username");
        this.password = json.getString("password");
        this.auth = new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));
        this.serveurs = new HashMap<String, String>();
        JsonObject arr = json.getJsonObject("serveurs");
        for (String key : arr.keySet()) {
            this.serveurs.put(key, arr.getString(key));
        }
        ;
    }

    /**
     * Recuperer le username d'utilisateur
     * 
     * @return le username de User
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Recuperer la liste des serveurs
     * 
     * @return la liste des serveurs
     */
    public HashMap<String, String> getServeurs() {
        return this.serveurs;
    }

    /**
     * Ajouter un nouveau serveur
     * 
     * @param alias   : nom personnalisé du serveur
     * @param serveur : adresse URL du serveur
     */
    public void addServeur(String alias, String serveur) {
        this.serveurs.put(alias, serveur);
    }

    /**
     * L'authentification de l'utilisateur
     * 
     * @return Token d'authentification
     */
    public String getAuth() {
        return this.auth;
    }

    /**
     * convert User Class to JsonObject
     * 
     * @return JsonObject of user
     */
    public JsonObject getUserJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("username", this.username);
        builder.add("password", this.password);
        JsonObjectBuilder servsBuilder = Json.createObjectBuilder();
        for (String key : this.serveurs.keySet()) {
            servsBuilder.add(key, this.serveurs.get(key));
        }
        JsonObject servs = servsBuilder.build();
        builder.add("serveurs", servs);
        return builder.build();
    }
}
