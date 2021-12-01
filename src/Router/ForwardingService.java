package Router;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;

public class ForwardingService  {
    // header types
    final static int CONTROLLER_REPLY = 0; // wraps header to signify packet is a reply from Controller
        final static int UPDATE = 1; // always length 2, first value is id of router to change, second value is updated data
            final static int ROUTER_ID = 2; // ID of router that will be updated
            final static int UPDATED_VAL = 3; // value to update to
    
    final static int FS_REQUEST = 4; // wraps header to signify that packet is a request from a Forwarding Service
        final static int REQUESTOR_ID = 5; // ID of FS

        final static int PACKET_HEADER = 6; // wraps packets header info
            final static int DESTINATION_ID = 7; // ID of final destination
            final static int SOURCE_ID = 8; // ID of initial source
            final static int PACKET_TYPE = 9; // Type of packet being transmitted (irrelevant for assignment but need irl)

    private static HashMap<String, Integer> forwardingTable;
    private static DatagramSocket socket;
    final static int MTU = 1500;
    public static void main(String[] args) throws SocketException {
        // init
        forwardingTable = new HashMap<>();
        socket = new DatagramSocket(51510);

        System.out.println("Hello from FS");
    }

    private static void forward(byte[] data, String dest) throws IOException{
        if(forwardingTable.containsKey(dest))
            send(data, forwardingTable.get(dest));
        else
            
            contactController(data);
    }

    private static void contactController(byte[] data) throws IOException{
        // TODO
        // contact controller
    }

    public static byte[] receive() throws IOException{
            
        byte[] data= new byte[MTU];
        DatagramPacket packet= new DatagramPacket(data, data.length);
    
        socket.receive(packet);

        // extract data from packet
        data= packet.getData();

        System.out.println("Node received: \""+data+",\" from: "+packet.getAddress());

        // TODO extrapolate data
        byte headerType = data[0];

        if(headerType == CONTROLLER_REPLY)
            interpretReply(data);
        else if(headerType==PACKET_HEADER)
            interpetHeader(data);

            

        String dest = "";
        forward(data, dest);
        return data;
    }

    public static byte[] interpretReply(byte[] data){
        // TODO
        byte[] ret = new byte[5];
        byte  routerToUpdate, update, packetType;
        byte[] destination, source;
        int index = 0;
        if(data[index]!=CONTROLLER_REPLY)
            return null;
        // can safely skip controller reply length, its irrelevant

        index = 2;
        if(data[index]!=UPDATE)
            return null;
        // can safely skip update length, we know it is two
        routerToUpdate = data[index];
        update = data[++index];
        if(data[++index] != DESTINATION_ID)
            return null;

        int destinationLenght = data[++index];
        destination = new byte[destinationLenght];
        for(int i = 0;i<destinationLenght;i++){
            destination[i] = data[i+index];
        }
        index+=destinationLenght;

        int sourceLenght = 8+destinationLenght+1; // we know destination starts at 8, add the dest length to get to the next header, add one to skip header type (we know it will be SOURCE_ID)


        

        return ret;
    }

    public static byte[] interpetHeader(byte[] data){
        // TODO

        return null;
    }

    public static void send(byte[] data, int dest) throws IOException{
        
        InetAddress address= InetAddress.getLocalHost();   
        int port= dest;                       
    
        // create packet addressed to destination
        DatagramPacket packet= new DatagramPacket(data, data.length, address, port);
        socket.send(packet);
    }
}
