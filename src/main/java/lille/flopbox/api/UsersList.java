package lille.flopbox.api;

import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Instance avec la liste de tous les utilisateurs de la platforme.
 */
public class UsersList {

    private static UsersList instance = null;
    private HashMap<String, User> users;

    /**
     * Constructeur Singletoon pour la recuperation des données à partir du fichier
     * "users.json"
     */
    private UsersList() {
        JsonObject jsonUsers = FileManager.getJsonFileContent("users.json");
        this.users = new HashMap<>();
        for (String key : jsonUsers.keySet()) {
            User u = new User(jsonUsers.get(key).asJsonObject());
            this.users.put(key, u);
        }
    }

    /**
     * Creation et recuperation de la liste des users
     * 
     * @return la list des utilisateurs de la platforme
     */
    public static UsersList getInstance() {
        if (instance == null)
            instance = new UsersList();
        return instance;
    }

    /**
     * La liste des utilisateurs sous la forme de la classe "User"
     * 
     * @return la liste des utilisateurs
     */
    public HashMap<String, User> getUsers() {
        return this.users;
    }

    /**
     * Chercher un utilisateur en utilisant son username
     * 
     * @param username : username de l'utilisateur à chercher
     * @return l'utilisateur recherché, sinon null
     */
    public User getUserByUsername(String username) {
        return this.users.get(username);
    }

    /**
     * recuperer les serveurs d'un utilisateur
     * 
     * @param username : username de l'utilisateur à chercher
     * @return la liste des serveurs de l'utilisateur
     */
    public HashMap<String, String> getServeursByUsername(String username) {
        return this.users.get(username).getServeurs();
    }

    /**
     * convertir la class "UsersList" a un object json qui contient des objet json
     * de class "User"
     * 
     * @return jsonObject de tous les utilisateurs
     */
    public JsonObject getUsersJSON() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        for (User u : this.users.values()) {
            builder.add(u.getUsername(), u.getUserJson());
        }
        return builder.build();
    }

    /**
     * Ajouté un utilisateur à la liste des utilisateurs
     * 
     * @param newUser : le nouveau utilisateur à ajouter
     * @return true si l'utilisateur est ajouté, false s'il existe déjà.
     */
    public boolean addUser(User newUser) {
        if (this.users.containsKey(newUser.getUsername())) {
            return false;
        } else {
            this.users.put(newUser.getUsername(), newUser);
            return true;
        }
    }
}
