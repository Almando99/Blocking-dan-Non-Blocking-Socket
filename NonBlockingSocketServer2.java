import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class NonBlockingSocketServer2 {
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private Map<SocketChannel, String> clients = new HashMap<>();

    public static void main(String[] args) {
        try {
            new NonBlockingSocketServer2().startServer();
        } catch (IOException e) {
            System.out.println("Disconnected");
        } 
    }
    
    // Fungsi untuk memulai server
    public void startServer() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(8888));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started...");

        // Thread untuk menangani input dari server
        Thread serverInputThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);

            while (true) {
                // mengirim pesan kepada Client
                System.out.print("");
                String input = scanner.nextLine();

                if (input.startsWith("/send")) {
                    // Format: /send <client_name> <message>
                    String[] tokens = input.split(" ");
                    if (tokens.length >= 3) {
                        String clientName = tokens[1];
                        String message = input.substring(tokens[0].length() + tokens[1].length() + 2);

                        sendPrivateMessage(clientName, message);
                    } else {
                        System.out.println("Invalid command. Use /send <client_name> <message>");
                    }
                } else {
                    // Broadcast pesan dari server ke semua client
                    for (SocketChannel clientChannel : clients.keySet()) {
                        try {
                            ByteBuffer responseBuffer = ByteBuffer.wrap(("Server: " + input).getBytes());
                            clientChannel.write(responseBuffer);
                        } catch (IOException e) {
                            System.out.println("Client belum ada.");
                        }
                    }
                }
            }
        });

        serverInputThread.start();

        while (true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                if (key.isAcceptable()) {
                    acceptClient();
                } else if (key.isReadable()) {
                    readMessage(key);
                }

                keyIterator.remove();
            }
        }
    }

    // Fungsi untuk menerima koneksi dari client
    private void acceptClient() throws IOException {
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client connected: " + clientChannel.getRemoteAddress());
    }

    // Fungsi untuk membaca pesan dari client
    private void readMessage(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(256);
        int bytesRead = clientChannel.read(buffer);

        if (bytesRead == -1 && bytesRead == 0) {
            // Client disconnected
            String clientName = clients.get(clientChannel);
            System.out.println("Client disconnected: " + clientName);
            clients.remove(clientChannel);
            clientChannel.close();
            return;
        }

        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        String message = new String(bytes);

        if (!clients.containsKey(clientChannel)) {
            // Client baru, atur nama
            clients.put(clientChannel, message.trim());
            System.out.println("Client registered: " + message.trim());
        } else {
            // Broadcast pesan ke client lainnya
            String senderName = clients.get(clientChannel);
            System.out.println("Pesan dari " + senderName + ": " + message);

        }
    }

    // Fungsi untuk mengirim pesan pribadi ke client tertentu
    private void sendPrivateMessage(String clientName, String message) {
        for (Map.Entry<SocketChannel, String> entry : clients.entrySet()) {
            if (entry.getValue().equals(clientName)) {
                SocketChannel targetClient = entry.getKey();
                try {
                    ByteBuffer responseBuffer = ByteBuffer.wrap(("Private message from server: " + message).getBytes());
                    targetClient.write(responseBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        // pesan jika nama tidak ditemukan
        System.out.println("Client not found: " + clientName);
    }


}
