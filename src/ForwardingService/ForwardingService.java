package ForwardingService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
            final static int PACKET = 10; // the actual packet

    
    final static int MTU = 1500;
    final static int ROUTER_PORT = 80;
    final static int FS_PORT = 51510;

    private static HashMap<String, byte[]> forwardingTable;
    private static DatagramSocket socket;
    private static String routerID; // ID of parent router

    public static void main(String[] args) throws IOException {
        // init
        forwardingTable = new HashMap<>();
        socket = new DatagramSocket(FS_PORT);

        routerID = args[0];

        System.out.println("Hello from FS");

        while(true)
            receive();
    }

    private static void forward(byte[] data, String dest) throws IOException{
        if(forwardingTable.containsKey(dest)){
            InetAddress address = InetAddress.getByAddress(forwardingTable.get(dest));
            send(data, address, ROUTER_PORT);
        }
        else
            contactController(data, dest);
    }

    private static void update(String router, byte[] address ){
        forwardingTable.put(router, address);
    }

    private static void contactController(byte[] data, String dest) throws IOException{
        // TODO
    }

    public static void receive() throws IOException{

    
            
        byte[] data= new byte[MTU];
        DatagramPacket packet= new DatagramPacket(data, data.length);
        System.out.println("Receiving...");
        socket.receive(packet);

        // extract data from packet
        data= packet.getData();

        System.out.println("FS received: \""+data+",\" from: "+packet.getAddress());

        // Extract header information
        byte[][] headerInfo = null;

        byte headerType = data[0]; // the first byte will indicate if it is just a normal packet or a reply from controller
        if(headerType == CONTROLLER_REPLY)
            headerInfo = interpretReply(data);
        else if(headerType==PACKET_HEADER)
            headerInfo = interpretHeader(data);

        if(headerInfo == null){ // should not still be null
            System.out.println("An error has occured");
            return;
        }

        // check if dest is of format "tcd.scss"
        String dest = new String(headerInfo[0]);
        String[] dests = dest.split(".");
        

        String prefix = dests[0]; // next hop
        if(prefix.equals(routerID)){ // we can drop the prefix
            // drop prefix
            dest = "";
            for(int i = 1; i<dests.length;i++){
                dest = dest+dests[i]+".";
            }
            dest = dest.substring(0,dest.length()-1); // remove last "."

            byte[] destInBytes = dest.getBytes();

            // redo header to change dest field
            int newHeaderLen = 2+(2+destInBytes.length+2+headerInfo[1].length+2+headerInfo[2].length+2+headerInfo[3].length);
            byte[] newHeader = new byte[newHeaderLen];
            int index = 0;
            newHeader[index++] = PACKET_HEADER;
            newHeader[index++] = (byte) (newHeaderLen-2);

            newHeader[index++] = DESTINATION_ID;
            newHeader[index++] = (byte) destInBytes.length;
            for(int i = 0; i<destInBytes.length;i++){
                newHeader[index++] = destInBytes[i];
            }

            newHeader[index++] = SOURCE_ID;
            newHeader[index++] = (byte) headerInfo[1].length;
            for(int i = 0; i<headerInfo[1].length;i++){
                newHeader[index++] = headerInfo[1][i];
            }

            newHeader[index++] = PACKET_HEADER;
            newHeader[index++] = (byte) headerInfo[2].length;
            for(int i = 0; i<headerInfo[2].length;i++){
                newHeader[index++] = headerInfo[2][i];
            }

            newHeader[index++] = PACKET;
            newHeader[index++] = (byte) headerInfo[3].length;
            for(int i = 0; i<headerInfo[3].length; i++){
                newHeader[index++] = headerInfo[2][i];
            }
            forward(newHeader, dest);
            return;
        }

        else // don't drop prefix as it hasn't gotten to destination
            forward(data, dest);
    }

    public static byte[][] interpretReply(byte[] data){
        byte[][] ret = new byte[3][];
        byte[] routerToUpdate, update, destination, source, packetType, packet;
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
        packetType[0] = data[index++]; 

        if(data[index++]!= PACKET)
            return null;
        int packetLen = data[index++];
        packet = new byte[packetLen];
        for(int i = 0; i<packetLen;i++){
            packet[i] = data[index++];
        }
        
        
        ret[0] = destination;
        ret[1] = source;
        ret[2] = packetType;
        ret[3] = packet;

        update(new String(routerToUpdate), update);

        return ret;
    }

    public static byte[][] interpretHeader(byte[] data){
        byte[][] ret = new byte[3][];
        byte[] destination, source, packetType, packet;

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
        packetType[0] = data[index++]; 

        if(data[index++]!= PACKET)
            return null;
        int packetLen = data[index++];
        packet = new byte[packetLen];
        for(int i = 0; i<packetLen;i++){
            packet[i] = data[index++];
        }
        
        
        ret[0] = destination;
        ret[1] = source;
        ret[2] = packetType;
        ret[3] = packet;

        return ret;
    }

    public static void send(byte[] data,InetAddress address, int port) throws IOException{
        
    
        // create packet addressed to destination
        DatagramPacket packet= new DatagramPacket(data, data.length, address, port);
        socket.send(packet);
    }
}
