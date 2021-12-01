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

    private static HashMap<String, byte[]> forwardingTable;
    private static DatagramSocket socket;
    final static int MTU = 1500;
    public static void main(String[] args) throws SocketException {
        // init
        forwardingTable = new HashMap<>();
        socket = new DatagramSocket(51510);

        System.out.println("Hello from FS");
    }

    private static void forward(byte[] data, String dest) throws IOException{
        if(forwardingTable.containsKey(dest)){
            InetAddress address = InetAddress.getByAddress(forwardingTable.get(dest));
            send(data, address, 3);
        }
        else
            
            contactController(data);
    }

    private static void update(String router,byte[] address ){
        forwardingTable.put(router, address);
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

        byte[][] headerInfo = null;
        if(headerType == CONTROLLER_REPLY)
            headerInfo = interpretReply(data);
        else if(headerType==PACKET_HEADER)
            headerInfo = interpretHeader(data);

        if(headerInfo == null)
            System.out.println("An error has occured");
            

        String dest = "";
        forward(data, dest);
        return data;
    }

    public static byte[][] interpretReply(byte[] data){
        // TODO
        byte[][] ret = new byte[5][];
        byte[] routerToUpdate, update, destination, source, packetType;
        int index = 0;
        if(data[index+=2]!=CONTROLLER_REPLY) // skip len, irrelevant
            return null;
        
        if(data[index+=2]!=UPDATE) // skip len, irrelevant
            return null;
        
        if(data[index++]!=ROUTER_ID)
            return null;
        int routerIdLen = data[index++]; 
        routerToUpdate = new byte[routerIdLen];
        for(int i = 0; i<routerIdLen;i++){
            routerToUpdate[i] = data[index++];
        }

        if(data[index++]!=UPDATED_VAL)
            return null;
        int updateLen = data[index++];
        update = new byte[updateLen];
        for(int i = 0; i<updateLen;i++){
            update[i] =  data[index++];
        }
        
        if(data[index+=2] != PACKET_HEADER) // skip len, irrelevant
            return null;
        
        if(data[index++] != DESTINATION_ID)
            return null;
        int destinationLenght = data[index++];
        destination = new byte[destinationLenght];
        for(int i = 0;i<destinationLenght;i++){
            destination[i] = data[index++];
        }

        if(data[index++] != SOURCE_ID)
            return null;
        int sourceLen = data[index++];
        source = new byte[sourceLen];
        for(int i = 0; i<sourceLen;i++){
            source[i] = data[index++];
        }

        if(data[index+=2] != PACKET_TYPE) // skip len, irrelevant
            return null;
        packetType = new byte[1];
        packetType[0] = data[index]; 

        ret[0] = routerToUpdate;
        ret[1] = update;
        ret[2] = destination;
        ret[3] = source;
        ret[4] = packetType;

        return ret;
    }

    public static byte[][] interpretHeader(byte[] data){
        byte[][] ret = new byte[3][];
        byte[] destination, source, packetType;

        int index = 0;

        if(data[index+=2] != PACKET_HEADER) // skip len, irrelevant
            return null;
        
        if(data[index++] != DESTINATION_ID)
            return null;
        int destinationLenght = data[index++];
        destination = new byte[destinationLenght];
        for(int i = 0;i<destinationLenght;i++){
            destination[i] = data[index++];
        }

        if(data[index++] != SOURCE_ID)
            return null;
        int sourceLen = data[index++];
        source = new byte[sourceLen];
        for(int i = 0; i<sourceLen;i++){
            source[i] = data[index++];
        }

        if(data[index+=2] != PACKET_TYPE) // skip len, irrelevant
            return null;
        packetType = new byte[1];
        packetType[0] = data[index]; 

        ret[0] = destination;
        ret[1] = source;
        ret[3] = packetType;

        return ret;
    }

    public static void send(byte[] data,InetAddress address, int port) throws IOException{
        
    
        // create packet addressed to destination
        DatagramPacket packet= new DatagramPacket(data, data.length, address, port);
        socket.send(packet);
    }
}
