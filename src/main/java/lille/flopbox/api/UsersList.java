package lille.flopbox.api;

import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class UsersList {
    
    private static UsersList instance = null;
    private HashMap<String, User> users;
    
    private UsersList()
    {
        JSONArray jsonUsers = FileManager.getJsonFileContent("users.json");
        this.users = new HashMap<>();
        for(int i=0; i<jsonUsers.size(); i++)
        {
            User u = new User( (JSONObject) jsonUsers.get(i));
            this.users.put(u.username, u);
        }
    }

    public static UsersList getInstance()
    {
        if(instance == null)
            return new UsersList();
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

    public String toString()
    {
        String ret = "{ \"users\":[";
        int i=0;
        for(User u : UsersList.getInstance().getUsers().values())
        {
            ret += u.toString();
            if(i!=UsersList.getInstance().getUsers().values().size())
                ret += ",";
            i++;
        }
        return ret+"]}";
    }
}
