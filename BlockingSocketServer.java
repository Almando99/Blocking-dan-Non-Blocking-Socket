import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlockingSocketServer {
    private static final int PORT = 8888;
    private static AtomicBoolean isFirstClient = new AtomicBoolean(true);
    private static Socket firstClientSocket; // Menyimpan soket dari klien pertama

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server dibuka pada port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Klien terhubung: " + clientSocket.getInetAddress());

                // Memulai thread baru untuk menangani setiap koneksi klien
                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void handleClient(Socket clientSocket) {
        String clientName = null;

        try {
            InputStream inputStream = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            OutputStream outputStream = clientSocket.getOutputStream();

        // Menerima nama dari klien
            clientName = reader.readLine();
            System.out.println("Nama Client: " + clientName);

            if (isFirstClient.compareAndSet(true, false)) {
            // Jika ini adalah klien pertama, simpan soketnya
                firstClientSocket = clientSocket;

                while (true) {
                // Menerima pesan dari klien
                    String receivedMessage = reader.readLine();

                // Periksa apakah klien saat ini adalah klien pertama
                    if (clientSocket == firstClientSocket) {
                        if (receivedMessage == null) {
                        // Klien disconnect, berikan keterangan dan keluar dari loop
                            System.out.println("Klien " + clientName + " disconnect.");
                            break;
                        }

                        System.out.println("Pesan dari " + clientName + ": " + receivedMessage);

                        Scanner scanner = new Scanner(System.in);
                        System.out.print("Kirim pesan ke " + clientName + ": ");
                        String response = scanner.nextLine();

                    // Kirim tanggapan hanya ke klien pertama
                        outputStream.write(response.getBytes());
                        outputStream.write('\n');

                    // Keluar dari loop jika klien mengirim 'exit'
                        if (response.equalsIgnoreCase("exit")) {
                            break;
                        }
                    } 
                }
            } else {
            // Jika bukan klien pertama, beri tahu bahwa tidak diizinkan untuk mengirim dan menerima pesan
                System.out.println("Client " + clientName + " tidak diizinkan untuk menerima dan mengirim pesan.");
            }
        } catch (IOException e) {
            System.out.println(clientName + " disconnect " );
        } finally {
            try {
            // Menutup koneksi untuk klien tertentu
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


