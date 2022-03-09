package lille.flopbox.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class FileManager {
    
    public static ArrayList<String> getFileContent(String filename) throws FileNotFoundException, IOException 
    {
        ArrayList<String> result = new ArrayList<String>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            while (br.ready()) {
                result.add(br.readLine());
            }
        }
        return result;
    }

    public static boolean canAccess(String authHeader)
    {
        if(authHeader == null || authHeader.length() == 0)
            return false;

        String auth ;
        Collection<User> users;

        if(authHeader.startsWith("Basic"))
            auth = authHeader.substring("Basic".length()).trim();
        else
            auth = authHeader;

        users = UsersList.getInstance().getUsers().values();
        for(User u : users)
        {
            if(u.auth.equals(auth))
            {
                return true;
            }
        }
        return false;
    }

    public static String getUsernameFromAuth(String authHeader)
    {
        String username;
        authHeader = authHeader.substring("Basic".length()).trim();
        authHeader = new String(Base64.getDecoder().decode(authHeader.getBytes()));
        username = authHeader.split(":")[0];
        return username;
    }

    public static JSONArray getJsonFileContent(String filename)
    {
        JSONObject content = new JSONObject();
        try {
            content = (JSONObject) new JSONParser().parse(new FileReader(filename));
        } catch (IOException | ParseException e) {
            throw new RuntimeException("getJsonFileContent : "+e.getMessage());
        }
        return (JSONArray) content.get("users");
    }
 
    /***
     * NOT WORKING
     */
    public static void saveFileToJson()
    {
        File file = new File("users.json");
        // file.deleteOnExit();
        try (FileWriter f = new FileWriter(file)) {
            f.write(UsersList.getInstance().toString());
            f.flush();
            f.close();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
