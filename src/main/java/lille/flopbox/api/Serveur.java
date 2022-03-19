package lille.flopbox.api;

/**
 * Classe d'objet serveur avec url et son port associé
 */
public class Serveur {
    public String url;
    public int port;

    /**
     * Serveur avec url et port specifié
     * 
     * @param url  : url du serveyr FTP
     * @param port : port du serveur FTP
     */
    public Serveur(String url, int port) {
        this.url = url;
        if (port == 0)
            port = 21;
        this.port = port;
    }
}
