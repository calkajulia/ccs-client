import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

public class Main {

    private static final String message = "CCS DISCOVER";
    private static final InetAddress BROADCAST_ADDRESS;
    private static final String[] operations = {"ADD", "SUB", "MUL", "DIV"};
    private static final Random random = new Random();

    static {
        try {
            BROADCAST_ADDRESS = InetAddress.getByName("255.255.255.255");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        String startingCommand = scanner.nextLine();
        String[] splitStartingCommand = startingCommand.split(" ");
        if(!validateStartingCommand(splitStartingCommand)) {
            System.err.println("Usage: client <port>");
            System.err.println("Where <port> is the CCS server port number");
            System.exit(1);
        }

        int port = Integer.parseInt(splitStartingCommand[1]);
        InetAddress serverAddress = discoverServer(port);
        if(serverAddress == null) {
            log("Error: No CCS server found on port " + port);
            System.exit(1);
        }

        sendMessagesToServer(serverAddress, port);
        log("Terminated successfully.");
    }

    private static boolean validateStartingCommand(String[] command) {
        if(command.length != 2) {
            return false;
        }

        if(!command[0].equals("client")) {
            return false;
        }

        try {
            int port = Integer.parseInt(command[1]);
            if(port < 1 || port > 65535) {
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static void log(String message) {
        System.out.println("[CLIENT] " + message);
    }

    private static InetAddress discoverServer(int port) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);

        byte[] messageBytes = message.getBytes();
        DatagramPacket messagePacket = new DatagramPacket(messageBytes, messageBytes.length);
        messagePacket.setAddress(BROADCAST_ADDRESS);
        messagePacket.setPort(port);
        socket.send(messagePacket);
        log("Sent broadcast message: " + message);

        byte[] responseBuffer = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
        socket.receive(responsePacket);
        String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
        log("Response received: " + response);
        socket.close();

        if(response.equals("CCS FOUND")) {
            return responsePacket.getAddress();
        } else {
            return null;
        }
    }

    private static void sendMessagesToServer(InetAddress serverAddress, int port) throws IOException, InterruptedException {
        Socket socket = new Socket(serverAddress, port);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        for(int i = 0; i < 3; i++) {
            String message = generateMessage();
            writer.write(message + "\n");
            writer.flush();
            log("Sent message: " + message);

            String response = reader.readLine();
            log("Response received: " + response);

            Thread.sleep(random.nextInt(2000) + 1000);
        }
        socket.close();
        writer.close();
        reader.close();
    }

    private static String generateMessage() {
        String operation = operations[random.nextInt(operations.length)];
        int arg1 = random.nextInt(100);
        int arg2 = random.nextInt(100);
        return operation + " " + arg1 + " " + arg2;
    }
}
