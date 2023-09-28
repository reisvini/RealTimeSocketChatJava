import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader buffReader;
    private BufferedWriter buffWriter;
    private String name;

    public Client(Socket socket, String name) {
        try {

            this.socket = socket;
            this.buffWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.buffReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.name = name;

        } catch (IOException e) {
            closeAll(socket, buffReader, buffWriter);
        }
    }

    public void sendMessage() {
        try {
            buffWriter.write(name);
            buffWriter.newLine();
            buffWriter.flush();

            System.out.println("Digite uma mensagem: ");

            Scanner sc = new Scanner(System.in);

            while (socket.isConnected()) {
                String messageToSend = sc.nextLine();
                buffWriter.write(messageToSend);
                buffWriter.newLine();
                buffWriter.flush();

            }
        } catch (IOException e) {
            closeAll(socket, buffReader, buffWriter);

        }
    }

    public void readMessage() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                String incomingMessage;

                while (socket.isConnected()) {
                    try {
                        incomingMessage = buffReader.readLine();
                        System.out.println(incomingMessage);
                    } catch (IOException e) {
                        closeAll(socket, buffReader, buffWriter);
                    }

                }

            }

        }).start();
    }

    public void closeAll(Socket socket, BufferedReader buffReader, BufferedWriter buffWriter) {
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
            e.getStackTrace();
        }
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Coloque seu nome para começar o chat: ");
        String name = sc.nextLine();
        Socket socket = new Socket("localhost", 3030);
        Client client = new Client(socket, name);
        client.readMessage();
        client.sendMessage();
    }

}