package Router;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Router {
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
    
    
    
            
    private static String name;
    final static int MTU = 1500;
    final static int ROUTER_PORT = 80;
    final static int FS_PORT = 51510;

    static DatagramSocket socket;

    public static void main(String[] args) throws IOException, InterruptedException {

        
        
        socket = new DatagramSocket(ROUTER_PORT);

        name = args[0]; // set name

        ArrayList<byte[]> connectionHeaders = new ArrayList<>(); // prepare to make connections

        // prepare connection headers
        for(int i = 1; i<args.length;i++){
            byte[] header = new byte[2+(args[i].length())];
            int index = 0;
            header[index++] = CONNECT_TO;
            header[index++] = (byte) args[i].length();
            byte[] bytes = args[i].getBytes();
            for(int j = 0; j<bytes.length;j++)
                header[index++]=bytes[j];
            connectionHeaders.add(header);
        }

        // create request header
        int len = 0;
        for(int i = 0; i<connectionHeaders.size();i++)
            len = len + connectionHeaders.get(i).length;

        byte[] nameInBytes = name.getBytes();

        len+=2+(2+nameInBytes.length);    
        byte[] connectionRequest = new byte[len];

        int index = 0;
        connectionRequest[index++] = CONNECTION_REQUEST;
        System.out.println("Connections to request: "+ connectionHeaders.size());
        connectionRequest[index++] =  (byte) (connectionHeaders.size());

        connectionRequest[index++] = REQUESTOR_NAME;
        connectionRequest[index++] = (byte) nameInBytes.length;
        for(int i = 0; i<nameInBytes.length;i++)
            connectionRequest[index++] = nameInBytes[i];

        for(int i = 0; i<connectionHeaders.size();i++){
            byte[] currentHeader = connectionHeaders.get(i);
            for(int j = 0; j<currentHeader.length;j++)
                connectionRequest[index++] = currentHeader[j];
        }

        send(connectionRequest, InetAddress.getByName("controller"), 42); // send connection reques to to controller


        System.out.println("Hello from Router "+name);
        
        //hard coding a test for FS
        if(name.equals("1")){

            String p = "TESTING";
            byte[] pB = p.getBytes();

            String s = "5"; // destination in tests
            byte[] sB = s.getBytes();

            byte[] packet = new byte[2+(2+sB.length)+(2+name.length())+2+1+2+pB.length];
            

            index = 0;
            packet[index++] = PACKET_HEADER;
            packet[index++] = (byte) (2+sB.length+2+name.length()+3+2+pB.length);

            packet[index++] = DESTINATION_ID;
            packet[index++] = (byte) sB.length;
            for(int i = 0; i<sB.length; i++){
                packet[index++] = sB[i];
            }
                

            packet[index++] = SOURCE_ID;
            packet[index++] = (byte) name.length();
            byte[] nameB = name.getBytes();
            for(int i = 0;i<nameB.length;i++)
                packet[index++] = nameB[i];
            
            packet[index++] = PACKET_TYPE;
            packet[index++] = 1;
            packet[index++] = 'p';

            packet[index++] = PACKET;
            packet[index++] = (byte) pB.length;
            for(int i = 0; i<pB.length; i++)
                packet[index++] = pB[i];

            
            
            while(true){
                Thread.sleep(3000);
                System.out.println("Sending: " + new String(packet));
                send(packet, InetAddress.getByName("fs"), FS_PORT);
                
            }

            
        }
        while(true){
                receive();
            }
    }

    public static byte[] receive() throws IOException{
          
        byte[] data= new byte[MTU];
        DatagramPacket packet= new DatagramPacket(data, data.length);
    
        socket.receive(packet);

        // extract data from packet
        data= packet.getData();

        
        System.out.println("Router received: \""+new String(data)+",\" from: "+packet.getAddress());
        
        InetAddress address= InetAddress.getByName("fs");
        send(data, address, FS_PORT); // send to forwarding service
        return data;
    }

    public static void send(byte[] data, InetAddress address, int port) throws IOException{
        
        // create packet addressed to destination
        DatagramPacket packet= new DatagramPacket(data, data.length, address, port);
        socket.send(packet);
    }
}
