package lille.flopbox.api;

import java.util.Base64;
import java.util.HashMap;

import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class User {

    String username;
    String password;
    String auth;
    HashMap<String, String> serveurs;

    User(JSONObject json)
    {
        this.username = json.get("username").toString();
        this.password = json.get("password").toString();
        this.auth = new String(Base64.getEncoder().encode((username+":"+password).getBytes()));
        this.serveurs = new HashMap<String, String>();
        JSONObject arr = (JSONObject) json.get("serveurs");
        for(Object key : arr.keySet())
        {
            String keystr = (String) key;
            String value = arr.get(keystr).toString();
            this.serveurs.put(keystr, value);
        };
    }
    
    // public String toString()
    // {
    //     String ret = "{"+"\"username\":\""+this.username+"\",\"password\":\""+this.password+"\",\"serveurs\":{";
    //     int i=0;
    //     for(String key : this.serveurs.keySet())
    //     {
    //         ret += "\""+key+"\":\""+this.serveurs.get(key)+"\"";
    //         if(i!=this.serveurs.keySet().size()-1)
    //             ret +=",";
    //         i++;
    //     }
    //     ret += "}}";
    //     return ret;
    // }

    public JSONObject getUserJson() {
        JSONObject obj = new JSONObject();
        obj.put("username", this.username);
        obj.put("password", this.password);
        JSONObject serveurs = new JSONObject();
        this.serveurs.forEach((k,v) -> {
            serveurs.put(k,v);
        });
        obj.put("serveurs",serveurs);
        return obj;
    }
}
