package lille.flopbox.api;

import java.util.Base64;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class User {

    String username;
    String password;
    String auth;
    public HashMap<String, String> serveurs;

    public User(String username, String pass)
    {
        this.username = username;
        this.password = pass;
        this.auth = new String(Base64.getEncoder().encode((username+":"+password).getBytes()));
        this.serveurs = new HashMap<String, String>();
    }

    /**
     * Constructeur user a partir du objet json
     * @param json
     */
    User(JsonObject json)
    {
        this.username = json.getString("username");
        this.password = json.getString("password");
        this.auth = new String(Base64.getEncoder().encode((username+":"+password).getBytes()));
        this.serveurs = new HashMap<String, String>();
        JsonObject arr = json.getJsonObject("serveurs");
        for(String key : arr.keySet())
        {
            this.serveurs.put(key, arr.getString(key));
        };
    }

    /**
     * convert User Class to JsonObject
     * @return JsonObject of user
     */
    public JsonObject getUserJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("username", this.username);
        builder.add("password", this.password);
        JsonObjectBuilder servsBuilder = Json.createObjectBuilder();
        for(String key : this.serveurs.keySet())
        {
            servsBuilder.add(key, this.serveurs.get(key));
        }
        JsonObject servs = servsBuilder.build();
        builder.add("serveurs", servs);
        return builder.build();
    }
}
