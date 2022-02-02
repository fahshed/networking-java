import java.io.*;
import java.net.Socket;
import java.util.Date;

public class BrowserThread extends Thread {
    private Socket s;
    private InputStream in;
    private OutputStream out;
    //private BufferedReader br;
    private String baseContent;
    private String htmlBody;

    public BrowserThread(Socket s, InputStream in, OutputStream out) throws IOException {
        this.s = s;
        this.in = in;
        this.out = out;
        //this.br = new BufferedReader(new InputStreamReader(in));
        this.baseContent = "<html> <head> <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"> <title> $title </title> </head> <body> <h1> Welcome to CSE 322 Offline 1</h1> $body </body> </html>";
        this.htmlBody = HTTPServer.directoryToHtml(new File("root"));
        this.start();
    }

    @Override
    public void run() {
        byte [] inputBytes = new byte[200];
        String input = null;

        try {
            //input = br.readLine();
            in.read(inputBytes);
            input = new String(inputBytes);
            //System.out.println(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (input == null || input.length() == 0) {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        if (input.startsWith("GET")) {
            input = input.split("\n")[0].trim();
            String requestedPath = input.substring(5, input.length() - 8).trim();
            //System.out.println("requested path " + requestedPath);

            if (requestedPath.length() > 0 && !requestedPath.equalsIgnoreCase("favicon.ico")) {
                File file = new File(requestedPath);

                if(!file.exists()) {
                    htmlBody = "404 error";
                    System.out.println("404 error");
                    //serverResponse(htmlBody);
                    serverResponse(htmlBody, "404", "text/html", null);
                }
                else if(file.isDirectory()) {
                    htmlBody = HTTPServer.directoryToHtml(file);
                    //serverResponse(htmlBody);
                    serverResponse(htmlBody, "200 OK", "text/html", null);
                }
                else {
                    serverResponse("","200 OK", "text/html", file);
                }
            }
            else {
                //serverResponse(htmlBody);
                serverResponse(htmlBody, "200 OK", "text/html", null);
            }
        }
        else if(input.startsWith("UPLOAD")) {

            int chunkSize = 100;

            byte [] errorByte = new byte[10];
            try {
                in.read(errorByte);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String error = new String(errorByte).trim();

            if(error.equals("1")) {
                System.out.println("Error: File doesn't exist.");
            }
            else if(error.equals("0")) {
                try {
                    String fileName = input.substring(7).trim();
                    String filePath = "root\\" + fileName;

                    FileOutputStream fos = new FileOutputStream(filePath);

                    byte[] fileData = new byte[chunkSize];

                    int read = 0;
                    while ((read = in.read(fileData)) > 0) {
                        fos.write(fileData, 0, read);
                        //System.out.println(read);
                    }

                    fos.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            System.out.println();
            System.out.println("closing socket");
            System.out.println("Port " + s.getPort());
            System.out.println("Local Port " + s.getLocalPort());
            System.out.println();
            out.close();
            in.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void serverResponse(String body, String status, String mime, File file) {
        String content;
        content = baseContent.replace("$body", body);
        content = content.replace("$title", "Offline Server");

        PrintWriter pr = new PrintWriter(out);
        pr.write("HTTP/1.1 " + status + "\r\n");
        pr.write("Server: Java HTTP Server: 1.0\r\n");
        pr.write("Date: " + new Date() + "\r\n");
        pr.write("Content-Type: " + mime + "\r\n");
        if(file!=null) {
            pr.write("Content-Length: " + file.length() + "\r\n");
            pr.write("Content-Disposition: attachment; filename=" + file.getName() + ";" + "\r\n");
            pr.write("\r\n");
            pr.flush();
            HTTPServer.sendFileInChunks(file, this.out, 1024);
        }
        pr.write("Content-Length: " + content.length() + "\r\n");
        pr.write("\r\n");
        pr.write(content);
        pr.flush();
    }
}
