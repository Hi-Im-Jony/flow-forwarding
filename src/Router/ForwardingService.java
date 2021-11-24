package Router;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;

public class ForwardingService  {

    
    private static HashMap<String, Integer> forwardingTable;
    private static DatagramSocket socket;
    public static void main(String[] args) throws SocketException {
        // init
        forwardingTable = new HashMap<>();
        socket = new DatagramSocket(51510);

        System.out.println("Hello from FS");
    }

    public void forward(Byte[] data, String dest){
        if(forwardingTable.containsKey(dest))
            send(data, forwardingTable.get(dest));
        else
            contactController(dest);
            
            send(data, forwardingTable.get(dest));
    }

    private void contactController(String dest){
        // TODO
        // contact controller
        receive();
    }

    private void send(Byte[] data, int dest){
        // TODO
    }

    private void receive(){
        // TODO
    }
}
