# PlateForme FlopBox 

Hatim M'RABET EL KHOMSSI - 21/03/2022
[La vidéo de démonstration](docs/demonstration.mp4)
## Introduction :

Le projet consiste à développer un RESTful API pour la manipulation des fichiers dans des serveurs FTP tiers.
Les end-points disponibles sont :
- GET /auth/login
- GET /alias/
- GET /alias/:alias
- POST /alias/
- PUT /alias/:alias
- DELETE /alias/:alias
- GET /:alias/files-details/:path
- POST /:alias/empty-directory/:path
- GET /:alias/file/:path
- GET /:alias/directory/:path
- POST /:alias/file/:path
- POST /:alias/directory/:path
- PUT /:alias/rename/:path
- DELETE /:alias/file/:path
Le serveur se lance sur l'adresse: `http://localhost:8080/flopbox/v1/`


## Architecture :

Le rendu est composé de :
* Un dossier **src** qui contient le projet Maven.
* Un dossier **doc** qui contient la vidéos de démonstration du projet.
* Un dossier **download** qui va contenir les fichier que les utilisateurs téléchargent.
* Un fichier **users.json** qui représente notre base de données.
* Un dossier **target** qui contiendra la documentation javadoc et l'exécutable jar (pour le lancement du serveur merci de choisir la version cette version [FlopBox-3.0-jar-with-dependencies.jar](target/FlopBox-3.0-jar-with-dependencies.jar)).

Le projet est divisé en 3 packages, le premier contient les classes d'objet, le deuxième pour l'authentification et la sécurisation des requêtes protégées et le troisième englobe tous les autres requêtes. 

Dans la classe `FileManager`, il y a tout se qui est manipulation du fichier de base de données.
La classe `Serveur` représente un serveur FTP avec un URL et un numéro de port.
La classe `User` représente un utilisateur avec son username, mot de passe, et sa liste de serveurs, tous les utilisateurs sont dans une liste dans la classe `UsersList`.

En cas où il y a des données qui manquent ou une exception, on retourne une réponse selon le problème, avec un message expliquant le problème.

```java
    @DELETE
    @Secured
    @Path("file/{path: .*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeFile(...){

        if (username == null || password == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing Headers.").build();
        if (path == null)
            return Response.status(Status.BAD_REQUEST).entity("Missing path.").build();
        if (serveur == null)
            return Response.status(Status.NOT_FOUND).entity("Alias '" + alias + "' not found.").build();
        ...
        try {
            ...
            return Response.status(Status.OK).entity("Directory deleted.").build();
        } catch (IOException e) {
            return Response.status(Status.BAD_REQUEST).entity("Exception : " + e.getMessage()).build();
        }
    }
```

## Code Samples :

Pour la manipulation des utilisateurs, dans la classe `UsersList`, on utilise le patron de conception Singleton, pour avoir une seule liste des utilisateurs dans le programme, et faire toutes les manipulations sur la même liste, pour éviter d'avoir plusieurs versions. L'enregistrement de la liste dans le fichier `users.json` se fait uniquement quand on éteint le serveur (en cliquant sur `Entrer` et non pas sur `CTRL+C`)

```java
    private UsersList() {
        JsonObject jsonUsers = FileManager.getJsonFileContent("users.json");
        this.users = new HashMap<>();
        for (String key : jsonUsers.keySet()) {
            User u = new User(jsonUsers.get(key).asJsonObject());
            this.users.put(key, u);
        }
    }

    public static UsersList getInstance() {
        if (instance == null)
            instance = new UsersList();
        return instance;
    }
```

Pour la suppression des dossiers, la fonction `deleteFilesDirectories` permet de supprimer son contenue et le dossier lui-même, en utilisant la récursivité. Pareil pour le téléchargement d'un dossier et l'envoie d'un dossier vers un serveur FTP.

```java
    public boolean deleteFilesDirectories(FTPClient ftp, String path) {
        try {
            FTPFile[] files = ftp.listFiles(path);
            for (FTPFile file : files) {
                if (file.isDirectory()) {
                    deleteFilesDirectories(ftp, path + "/" + file.getName());
                } else {
                    if (!ftp.deleteFile(path + "/" + file.getName()))
                        return false;
                }
            }
            if (!ftp.removeDirectory(path))
                return false;
        } catch (IOException e) {
            throw new RuntimeException("deleteFiles : " + e.getMessage());
        }
        return true;
    }
```

Pour éviter de télécharger tout un dossier directement, on peut lancer la requête `GET /:alias/files-details/:path` pour avoir une arborescence du fichier correspondant au path, avec un lien pour télécharger chaque fichier tout seul.

```java
    public JsonArray getFilesDetails(FTPClient ftp, String path, String alias) {
        ...
        try {
            files = ftp.listFiles(path);
            for (FTPFile file : files) {
                ...
                // ajout de lien de telechargement
                if (file.isDirectory()) {
                    jsb.add("download", Main.BASE_URI + alias + "/directory" + path + file.getName());
                    jsb.add("content", getFilesDetails(ftp, path + file.getName() + "/", alias));
                } else
                    jsb.add("download", Main.BASE_URI + alias + "/file" + path + file.getName());

                ret.add(jsb.build());
            }
        } catch (IOException e1) {
            ...
        }
        return ret.build();
    }
```