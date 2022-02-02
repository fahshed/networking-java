import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client1 {
    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);

        Socket socket = new Socket("localhost", 6789);
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        System.out.println("Remote port: " + socket.getPort());
        System.out.println("Local port: " + socket.getLocalPort());
        System.out.println();

        System.out.print("Enter the file name/path to be uploaded: ");
        File file = new File(scan.nextLine());

        String send = "UPLOAD " + file.getName();
        byte [] sendBytes = send.getBytes();
        out.write(sendBytes);

        new UploadThread(socket, in, out, file);
    }
}
