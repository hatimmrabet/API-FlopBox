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
}
