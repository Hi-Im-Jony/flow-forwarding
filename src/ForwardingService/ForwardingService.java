package ForwardingService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

public class ForwardingService  {
   // header types
    final static int CONTROLLER_REPLY = -1; // wraps header to signify packet is a reply from Controller
        final static int UPDATE = -2; // always length 2, first value is id of router to change, second value is updated data
            final static int ROUTER_ID = -3; // ID of router that will be updated
            final static int UPDATED_VAL = -4; // value to update to
    
    final static int FS_REQUEST = -5; // wraps header to signify that packet is a request from a Forwarding Service
        final static int QUERY = -6; // name of router we are asking about
        // include REQUESTOR_NAME (declared further below)
        
        final static int PACKET_HEADER = -7; // wraps packets header info
            final static int DESTINATION_ID = -8; // ID of final destination
            final static int SOURCE_ID = -9; // ID of initial source
            final static int PACKET_TYPE = -10; // Type of packet being transmitted (irrelevant for assignment but need irl)
            final static int PACKET = -11; // the actual packet

    final static int CONNECTION_REQUEST = -12; // request from router to connect to another router / make presence known
        final static int REQUESTOR_NAME = -13; // ID of FS
        final static int CONNECT_TO = -14; // router to connect to
    
    final static int APP_ALERT = -15; // an alert from an App to a FS that it wants to receive stuff
        // REQUESTOR_NAME must be included;
        final static int STRING = -16; // string to associate with app


    
    final static int MTU = 1500;
    final static int ROUTER_PORT = 80;
    final static int FS_PORT = 51510;

    private static HashMap<String, InetAddress> forwardingTable;
    private static DatagramSocket socket;
    private static String routerID; // ID of parent router

    public static void main(String[] args) throws IOException, InterruptedException {
        // init
        forwardingTable = new HashMap<>();
        socket = new DatagramSocket(FS_PORT);

        routerID = args[0];

        System.out.println("Hello from FS");

        while(true)
            receive();
    }

    private static void forward(byte[] data, String dest) throws IOException, InterruptedException{
        String[] dests = dest.split(".");
        if(dests.length>1)
            dest = dests[0]; // only interested in next "hop"
        else if(dest.equals(routerID)){
            System.out.println("Destination reached!");
            return;
        }
        if(forwardingTable.containsKey(dest)){
            InetAddress address = forwardingTable.get(dest);
            //System.out.println("Forwarding:"+new String(data)+", to "+address.getHostName());
            //System.out.println("Dest addr is "+ address.toString());
            send(data, address, ROUTER_PORT);
            //System.out.println("Sent...");
        }
        else
            contactController(data, dest);
        Thread.sleep(100);
    }

    private static void update(String router, InetAddress address ){
        forwardingTable.put(router, address);
    }

    private static void contactController(byte[] data, String dest) throws IOException{
        //System.out.println("Contacting controller about: "+dest);
        // check dest format

        int index = 0;
        byte[] fsRequest = new byte[2+(2+dest.length()+2+routerID.length()+data.length)];

        fsRequest[index++] = FS_REQUEST;
        fsRequest[index++] = 1;

        fsRequest[index++] = QUERY;
        fsRequest[index++]= (byte) dest.length();
        byte[] destB = dest.getBytes();
        for(int i = 0; i<destB.length;i++)
            fsRequest[index++] = destB[i];

        fsRequest[index++] = REQUESTOR_NAME;
        fsRequest[index++] = (byte) routerID.length();
        byte[] idB = routerID.getBytes();
        for(int i = 0; i<idB.length;i++){
            fsRequest[index++] = idB[i];
        }
        
        
        // add rest of data
        for(int i = 0; i<data.length;i++)
            fsRequest[index++] = data[i];

        send(fsRequest, InetAddress.getByName("controller"), 42);
    }

    public static void receive() throws IOException, InterruptedException{
            
        byte[] data= new byte[MTU];
        DatagramPacket packet= new DatagramPacket(data, data.length);
        // System.out.println("Receiving...");
        socket.receive(packet);

        // extract data from packet
        data= packet.getData();

        
        System.out.println("FS received: \""+new String(data)+",\" from: "+packet.getAddress());

        // Extract header information
        byte[][] headerInfo = null;

        byte headerType = data[0]; // the first byte will indicate if it is just a normal packet or a reply from controller
        if(headerType == CONTROLLER_REPLY)
            headerInfo = interpretReply(data);
        else if(headerType==PACKET_HEADER)
            headerInfo = interpretHeader(data);

        if(headerInfo == null){ // should not still be null
            System.out.println("Header info is empty...");
            return;
        }

        // check if dest is of format "tcd.scss"
        String dest = new String(headerInfo[0]);
        String[] dests = dest.split(".");
        

        if(dests.length>1){
            String prefix = dests[0]; // next hop
            if(prefix.equals(routerID)){ // we can drop the prefix
                // drop prefix
                dest = "";
                for(int i = 1; i<dests.length;i++){
                    dest = dest+dests[i]+".";
                }
                dest = dest.substring(0,dest.length()-1); // remove last "."
            }

        }
        System.out.println("Dest: "+dest);
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

        newHeader[index++] = PACKET_TYPE;
        newHeader[index++] = (byte) headerInfo[2].length;
        for(int i = 0; i<headerInfo[2].length;i++){
            newHeader[index++] = headerInfo[2][i];
        }

        newHeader[index++] = PACKET;
        newHeader[index++] = (byte) headerInfo[3].length;
        for(int i = 0; i<headerInfo[3].length; i++){
            newHeader[index++] = headerInfo[3][i];
        }
        forward(newHeader, dest);
    }

    public static byte[][] interpretReply(byte[] data) throws UnknownHostException{
        System.out.println("Received reply!");
        byte[][] ret = new byte[4][];
        byte[] routerToUpdate, update, destination, source, packetType, packet;
        int index = 0;
        if(data[index++]!=CONTROLLER_REPLY) 
        {
            System.out.println("Error 1");
            return null;
        }
        index++; // skip len, irrelevant
        
        if(data[index++]!=UPDATE) 
        {
            System.out.println("Error 2");
            return null;
        }
        index++; // skip len, irrelevant
        
        if(data[index++]!=ROUTER_ID)
        {
            System.out.println("Error 3");
            return null;
        }
        int routerIdLen = data[index++]; 
        routerToUpdate = new byte[routerIdLen];
        for(int i = 0; i<routerIdLen;i++){
            routerToUpdate[i] = data[index++];
        }

        if(data[index++]!=UPDATED_VAL)
        {
            System.out.println("Error 4");
            return null;
        }
        int updateLen = data[index++];
        update = new byte[updateLen];
        for(int i = 0; i<updateLen;i++){
            update[i] =  data[index++];
        }
        
        if(data[index++] != PACKET_HEADER)
        {
            System.out.println("Error 5");
            return null;
        }
        index++; // skip len, irrelevant
        
        if(data[index++] != DESTINATION_ID)
        {
            System.out.println("Error 6");
            return null;
        }
        int destinationLenght = data[index++];
        destination = new byte[destinationLenght];
        for(int i = 0;i<destinationLenght;i++){
            destination[i] = data[index++];
        }

        if(data[index++] != SOURCE_ID)
        {
            System.out.println("Error 7");
            return null;
        }
        int sourceLen = data[index++];
        source = new byte[sourceLen];
        for(int i = 0; i<sourceLen;i++){
            source[i] = data[index++];
        }

        if(data[index++] != PACKET_TYPE) 
        {
            System.out.println("Error 8");
            return null;
        }
        index++; // skip len, irrelevant
        packetType = new byte[1];
        packetType[0] = data[index++]; 

        if(data[index++]!= PACKET)
        {
            System.out.println("Error 9");
            return null;
        }
        int packetLen = data[index++];
        packet = new byte[packetLen];
        for(int i = 0; i<packetLen;i++){
            packet[i] = data[index++];
        }
        
        
        ret[0] = destination;
        ret[1] = source;
        ret[2] = packetType;
        ret[3] = packet;

        update(new String(routerToUpdate), InetAddress.getByAddress(update));

        return ret;
    }

    public static byte[][] interpretHeader(byte[] data){
        byte[][] ret = new byte[4][];
        byte[] destination, source, packetType, packet;

        int index = 0;
        int checker =  0;
        if( data[index++] != (byte) PACKET_HEADER) 
        {
            System.out.println("Error 10: index  is: "+index);
            System.out.println("Error 10: checker is: "+checker);
            return null;
        }
        index++; // skip len, irrelevant
        if(data[index++] != DESTINATION_ID)
        {
            System.out.println("Error 11");
            return null;
        }
        int destinationLenght = data[index++];
        destination = new byte[destinationLenght];
        for(int i = 0;i<destinationLenght;i++){
            destination[i] = data[index++];
        }

        if(data[index++] != SOURCE_ID)
        {
            System.out.println("Error 12");
            return null;
        }
        int sourceLen = data[index++];
        source = new byte[sourceLen];
        for(int i = 0; i<sourceLen;i++){
            source[i] = data[index++];
        }

        if(data[index++] != PACKET_TYPE) 
        {
            System.out.println("Error 13");
            return null;
        }
        index++; // skip len, irrelevant
        packetType = new byte[1];
        packetType[0] = data[index++]; 

        if(data[index++]!= PACKET)
        {
            System.out.println("Error 14");
            return null;
        }
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
