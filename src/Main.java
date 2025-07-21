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

        while(true) {
            if(validateStartingCommand(splitStartingCommand)) {
                int port = Integer.parseInt(splitStartingCommand[1]);

                InetAddress serverAddress = discoverServer(port);

                if(serverAddress == null) {
                    log("Failed on discovering server.");
                    return;
                }

                sendMessagesToServer(serverAddress, port);
                log("Terminated.");
                return;
            } else {
                log("Invalid starting command. Correct usage: client <port>");
                return;
            }
        }
    }

    private static void log(String message) {
        System.out.println("[CLIENT] " + message);
    }

    private static boolean validateStartingCommand(String[] command) {
        return command[0].equals("client") && command[1].matches("\\d+");
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
    }

    private static String generateMessage() {
        String operation = operations[random.nextInt(operations.length)];
        int arg1 = random.nextInt(100);
        int arg2 = random.nextInt(100);
        return operation + " " + arg1 + " " + arg2;
    }
}
