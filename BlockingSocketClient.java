import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class BlockingSocketClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8888;

    public static void main(String[] args) {

        try {
            // Membuat koneksi ke server
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            System.out.println(" Terhubung ke server");

            // Input stream untuk menerima data dari server
            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Output stream untuk mengirim data ke server
            OutputStream outputStream = socket.getOutputStream();
            Scanner scanner = new Scanner(System.in);

            // Membaca nama dari pengguna
            System.out.print("Masukkan nama anda : ");
            String name = scanner.nextLine();

            // Mengirim nama ke server
            outputStream.write(name.getBytes());
            outputStream.write('\n');  // Menambahkan newline untuk memisahkan pesan

            while (true) {
                // Membaca pesan dari pengguna
                System.out.print("");
                String message = scanner.nextLine();

                // Mengirim pesan ke server
                outputStream.write(message.getBytes());
                outputStream.write('\n');  // Menambahkan newline untuk memisahkan pesan
                
                // Break dari loop jika pengguna memasukkan 'exit'
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }

                // Menerima balasan dari server
                String serverResponse = reader.readLine();
                System.out.println("Balasan dari server : " + serverResponse);
            }

            // Menutup koneksi
            socket.close();
            scanner.close();

        } catch (IOException e) {
            System.out.println("Anda tidak dapat mengirim pesan");
        }
    }
}
