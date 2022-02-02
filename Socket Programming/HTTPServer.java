import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPServer {
    private static final int PORT = 6789;

    public static String directoryToHtml(File root) {
        File [] files = root.listFiles();

        StringBuilder sb = new StringBuilder();
        String path;
        String name;
        for (File file : files) {
            path = file.getParentFile().getName() + "\\" + file.getName();
            name = file.getName();
            if(file.isDirectory()) {
                sb.append("<b> <a href=\"" + path + "\"> " + name + " </a> </b>");

            } else {
                sb.append("<a href=\"" + path + "\"> " + name + " </a>");
            }
            sb.append('\n');
            sb.append("</br>");
            sb.append('\n');
        }

        return sb.toString();
    }

    public static void sendFileInChunks(File file, OutputStream out, int chunkSize) {
        try {
            FileInputStream fis = new FileInputStream(file);

            byte [] fileData = new byte[chunkSize];

            int read = 0;
            while((read = fis.read(fileData)) > 0) {
                out.write(fileData, 0, read);
                //System.out.println(read);
            }
            fis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    public static void main(String[] args) throws IOException {
        ServerSocket serverConnect = new ServerSocket(PORT);
        System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

        while(true) {
            Socket s = serverConnect.accept();
            InputStream in = s.getInputStream();
            OutputStream out = s.getOutputStream();

            System.out.println("Connected to a new client");
            System.out.println("Remote Port " + s.getPort());
            System.out.println("Local Port " + s.getLocalPort());
            System.out.println();

            new BrowserThread(s, in, out);
        }
    }
}