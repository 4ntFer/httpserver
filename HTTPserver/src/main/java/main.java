import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class main {
    public static void main(String[] args){
        ServerSocket serverSocket;
        ArrayList<HandlingClient> allHandlingClients = new ArrayList<HandlingClient>();

        try{
            serverSocket = new ServerSocket(4242);

            System.out.println("Server was openned on port 4242!");

            while(true){
                /**
                 * The accept method blocks de execution
                 * until it is requested a new connection.
                 */

                allHandlingClients.add(new HandlingClient(serverSocket.accept(), allHandlingClients));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
