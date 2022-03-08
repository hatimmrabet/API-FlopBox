package lille.flopbox.api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

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
        JSONArray users;

        if(authHeader.startsWith("Basic"))
            auth = authHeader.substring("Basic".length()).trim();
        else
            auth = authHeader;

        try {
            users = getJsonFileContent("users.json");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File password.txt not found");
        } catch (IOException e) {
            throw new RuntimeException("IOExecption : "+e.getMessage());
        } catch (ParseException e) {
            throw new RuntimeException("ParseException : "+e.getMessage());
        }

        String encoded;
        for(int i=0; i<users.size(); i++)
        {
            String username = ((JSONObject) users.get(i)).get("username").toString();
            String password = ((JSONObject) users.get(i)).get("password").toString();
            encoded = new String(Base64.getEncoder().encode((username+":"+password).getBytes()));
            if(encoded.equals(auth))
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

    public static JSONArray getJsonFileContent(String filename) throws FileNotFoundException, IOException, ParseException
    {
        FileReader reader = new FileReader(filename);
        JSONParser jsonParser = new JSONParser();
        JSONObject content = (JSONObject) jsonParser.parse(reader);
        reader.close();
        return (JSONArray) content.get("users");
    }
 
    public static void saveFileToJson() throws IOException, ParseException
    {
        FileWriter f = new FileWriter("users1.json");
        f.write( UsersList.getInstance().toString());
        f.flush();
        f.close();
    }
}
