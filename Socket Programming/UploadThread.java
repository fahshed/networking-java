import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class UploadThread extends Thread {
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private File file;

    public UploadThread(Socket socket, InputStream in, OutputStream out, File file) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.file = file;
        this.start();
    }

    @Override
    public void run() {
        if(!file.exists()) {
            String error = "1";
            byte [] errorByte = error.getBytes();
            try {
                out.write(errorByte);
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Error: File doesn't exist");
        } else {
            String error = "0";
            byte [] errorByte = error.getBytes();
            try {
                out.write(errorByte);
            } catch (IOException e) {
                e.printStackTrace();
            }

            HTTPServer.sendFileInChunks(file, out, 100);
            System.out.println("Uploaded: " + file.getName());
        }

        try {
            socket.close();
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
