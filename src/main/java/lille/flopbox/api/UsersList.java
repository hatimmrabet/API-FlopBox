package lille.flopbox.api;

import java.util.HashMap;
import java.util.Set;
import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class UsersList {
    
    private static UsersList instance = null;
    private HashMap<String, User> users;
    
    private UsersList()
    {
        JSONObject jsonUsers = FileManager.getJsonFileContent("users.json");
        this.users = new HashMap<>();
        Set<String> keys = jsonUsers.keySet();
        for(String key :  keys)
        {
            User u = new User( (JSONObject) jsonUsers.get(key));
            this.users.put(u.username, u);
        }
    }

    public static UsersList getInstance()
    {
        if(instance == null)
            instance = new UsersList();
        return instance;
    }


    public HashMap<String,User> getUsers()
    {
        return this.users;
    }


    public User getUserByUsername(String username)
    {
        return this.users.get(username);
    }

    public HashMap<String,String> getServeursByUsername(String username)
    {
        return this.users.get(username).serveurs;
    }

    // public String toString()
    // {
    //     String ret = "{ \"users\":[";
    //     int i=0;
    //     for(User u : UsersList.getInstance().getUsers().values())
    //     {
    //         ret += u.toString();
    //         if(i!=UsersList.getInstance().getUsers().values().size())
    //             ret += ",";
    //         i++;
    //     }
    //     return ret+"]}";
    // }

    public JSONObject getUsersJSON()
    {
        JSONObject obj = new JSONObject();
        for(User u : this.users.values())
        {
            obj.put(u.username, (JSONObject) u.getUserJson());
        }
        return obj;
    }
}
