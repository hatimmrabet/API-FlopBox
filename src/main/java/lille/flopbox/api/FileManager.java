package lille.flopbox.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import org.apache.commons.io.FileUtils;

/**
 * La gestion des fichiers
 */
public class FileManager {

    /**
     * Verifier si l'utilisateur peut se connecter
     * 
     * @param authHeader : Token d'authentification
     * @return True si le token correspond à un utilisateur existant, False sinon.
     */
    public static boolean canAccess(String authHeader) {
        if (authHeader == null || authHeader.length() == 0)
            return false;

        String auth;
        Collection<User> users;

        if (authHeader.startsWith("Basic"))
            auth = authHeader.substring("Basic".length()).trim();
        else
            auth = authHeader;

        users = UsersList.getInstance().getUsers().values();
        for (User u : users) {
            if (u.getAuth().equals(auth)) {
                return true;
            }
        }
        return false;
    }

    /**
     * extraire le username a partir du Token de basic authentification
     * 
     * @param authHeader : Token d'authentification
     * @return le username contenue dans le token
     */
    public static String getUsernameFromAuth(String authHeader) {
        String username;
        authHeader = authHeader.substring("Basic".length()).trim();
        authHeader = new String(Base64.getDecoder().decode(authHeader.getBytes()));
        username = authHeader.split(":")[0];
        return username;
    }

    /**
     * recuperer les donnees du fichier json sous format d'objet json
     * 
     * @param filename : le nom du fichier json
     * @return un objet Json avec le contenu du fichier passé en parametre
     */
    public static JsonObject getJsonFileContent(String filename) {
        JsonObject content = Json.createObjectBuilder().build();
        try {
            JsonReader reader = Json.createReader(new FileInputStream(new File(filename)));
            content = reader.readObject();
            reader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFoundException " + e.getMessage());
        }
        return content;
    }

    /**
     * Sauvegarder les utilisateurs dans un fichier json "users.json"
     * 
     * @param filename : nom du fichier json
     */
    public static void saveFileToJson(String filename) {
        try {
            JsonWriter writer = Json.createWriter(new FileOutputStream(filename));
            writer.writeObject(UsersList.getInstance().getUsersJSON());
            writer.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFoundException " + e.getMessage());
        }
    }

    public static void unzip(InputStream uploadedInputStream) throws IOException
    {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(uploadedInputStream);
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = FileManager.newFile(new File("./downloads/"), zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new RuntimeException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new RuntimeException("Failed to create directory " + parent);
                }
                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    public static void zip(String zipFilePath, String destDirectory) throws IOException {
        FileOutputStream fos = new FileOutputStream(destDirectory);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(zipFilePath);
        FileManager.zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath, destFilePath;
        try {
            destDirPath = destinationDir.getCanonicalPath();
            destFilePath = destFile.getCanonicalPath();
            if (!destFilePath.startsWith(destDirPath + File.separator)) {
                throw new RuntimeException("Entry is outside of the target dir: " + zipEntry.getName());
            }
        } catch (IOException e) {
            throw new RuntimeException("IOException: " + e.getMessage());
        }
        return destFile;
    }

    public static void DeleteFilesDirectories(File file)
    {
        if(file.exists())
        {
            if(file.isDirectory())
            {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    throw new RuntimeException("Delete Files Exception : "+e.getMessage());
                }
            }
            if(file.isFile())
            {
                file.delete();
            }
        }
    }
}
