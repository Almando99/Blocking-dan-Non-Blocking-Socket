import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class NonBlockingSocketClient2 {
    private SocketChannel socketChannel;

    public static void main(String[] args) {
        try {
	    // Membuat objek NonBlockingSocketClient2 dan memulai klien
            new NonBlockingSocketClient2().startClient();
        } catch (IOException e) {
            // Menangani kesalahan jika terjadi disconnect dari server
            System.out.print("Server Disconnect.");
        }
    }

    // Memulai klien dan menyiapkan thread input dan output
    public void startClient() throws IOException {
        // Membuka saluran socket non-blocking dan mengonfigurasikannya
        socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 8888));
        socketChannel.configureBlocking(false);

        // Thread input untuk membaca input pengguna dan mengirim pesan ke server
        Thread inputThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Masukkan nama anda : ");

            while (true) {
                // Menunggu masukan pengguna
                System.out.print("");
                String message = scanner.nextLine();

                try {
                    // Mengirim masukan pengguna sebagai pesan ke server
                    sendMessage(message);
                } catch (IOException e) {
                    // Menangani kesalahan jika terjadi disconnect dari server
                    System.out.print("Server Disconnect.");
                }
            }
        });
        
        // Thread output untuk menerima pesan dari server
        Thread outputThread = new Thread(() -> {
            ByteBuffer buffer = ByteBuffer.allocate(256);

            while (true) {
                try {
                    // Membaca data dari server ke dalam buffer
                    int bytesRead = socketChannel.read(buffer);

                    if (bytesRead > 0) {
                        // Memproses dan mencetak pesan yang diterima
                        buffer.flip();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        String receivedMessage = new String(bytes);
                        System.out.println(receivedMessage.trim());
                        buffer.clear();
                    }
                } catch (IOException e) {
                    // Menangani kesalahan jika terjadi disconnect dari server
                    System.out.print ("Server Disconnect.");
                    break;
                }
            }
        });

        // Memulai thread input dan output
        inputThread.start();
        outputThread.start();
    }
    // Mengirim pesan ke server
    private void sendMessage(String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        socketChannel.write(buffer);
    }
}
