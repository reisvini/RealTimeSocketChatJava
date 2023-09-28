import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ChatMessageHandler implements Runnable {

    public static ArrayList<ChatMessageHandler> clientHandlers = new ArrayList<>();
    public Socket socket;
    private BufferedReader buffReader;
    private BufferedWriter buffWriter;
    private String name;

    public ChatMessageHandler(Socket socket) {
        try {
            this.socket = socket;
            this.buffWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.buffReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.name = buffReader.readLine();
            clientHandlers.add(this);

            broadcastMessage(name + " entrou na sala", true);

        } catch (IOException e) {
            closeAll(socket, buffReader, buffWriter);
        }
    }

    @Override
    public void run() {

        String incomingMessageFromClient;

        while (socket.isConnected()) {
            try {
                incomingMessageFromClient = buffReader.readLine();
                broadcastMessage(incomingMessageFromClient, false);
            } catch (IOException e) {
                closeAll(socket, buffReader, buffWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend, boolean isFromServer) {
        LocalTime currentTime = LocalTime.now();

        for (ChatMessageHandler clientHandler : clientHandlers) {
            try {
                String formattedTime = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                String senderName = isFromServer ? "!CHAT"
                        : !clientHandler.name.equals(name) ? name
                                : "Você";

                String message = String.format("%s - %s: %s", formattedTime, senderName, messageToSend);

                clientHandler.buffWriter.write(message);
                clientHandler.buffWriter.newLine();
                clientHandler.buffWriter.flush();
            } catch (IOException e) {
                closeAll(socket, buffReader, buffWriter);
            }
        }
    }

    public void handleWithClientExit() {
        clientHandlers.remove(this);
        broadcastMessage(name + " saiu da sala", true);
    }

    public void closeAll(Socket socket, BufferedReader buffReader, BufferedWriter buffWriter) {

        handleWithClientExit();
        try {
            if (buffReader != null) {
                buffReader.close();
            }
            if (buffWriter != null) {
                buffWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
        }

    }

}