package lille.flopbox.api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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
        ArrayList<String> content;
        if(authHeader.startsWith("Basic"))
            auth = authHeader.substring("Basic".length()).trim();
        else
            auth = authHeader;

        try {
            content = getFileContent("passwd.txt");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File password.txt not found");
        } catch (IOException e) {
            throw new RuntimeException("IOExecption : "+e.getMessage());
        }

        for(String elem : content)
        {
            if(elem.equals(auth))
            {
                return true;
            }
        }
        return false;
    }
}
