package Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Node {
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
            
    // Datagram stuff
    final static int MTU = 1500;
    final static int NODE_PORT = 40;
    static DatagramSocket NodeSocket;

    private static class App extends Thread{
        DatagramSocket AppSocket;
        @Override
        public void run(){
            System.out.println("Hello from App");
           
            
            try {
                AppSocket = new DatagramSocket();
                
                // generate a packet, hard coded for now
                byte[] header = new byte[11];

                header[0] = DESTINATION_ID;
                String destination = "trinity";
                byte[] destInBytes = destination.getBytes();

                header[1] = (byte) destInBytes.length;
                for(int i = 1; i<destInBytes.length+1;i++)
                    header[i] = destInBytes[i-1];
                
                String data = "Some Data";
                byte[] dataInBytes = data.getBytes();

                byte[] packet = new byte[header.length+dataInBytes.length];

                for(int i = 0; i<header.length;i++)
                    packet[i] = header[i];
                
                for(int i = header.length; i<(dataInBytes.length+header.length);i++)
                    packet[i] = dataInBytes[i-header.length];

                // packet done
                while(true){
                    System.out.println("Sending to Node");
                    send(packet, NODE_PORT);
                    Thread.sleep(2000);
                }
            

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
        }

        public void send(byte[] data, int dest) throws IOException{
            
            InetAddress address= InetAddress.getLocalHost();   
            int port= dest;                       
        
            // create packet addressed to destination
            DatagramPacket packet= new DatagramPacket(data, data.length, address, port);
            AppSocket.send(packet);
        }
}

    public static void main(String[] args) throws IOException {
        App demo = new App();
        demo.start();
        System.out.println("Hello from Node");
        NodeSocket = new DatagramSocket(NODE_PORT);
        while(true){
            System.out.println("Receiving...");
            receive();
        }
    }

    public static byte[] receive() throws IOException{
            
        byte[] data= new byte[MTU];
        DatagramPacket packet= new DatagramPacket(data, data.length);
    
        NodeSocket.receive(packet);

        // extract data from packet
        data= packet.getData();

        System.out.println("Node received: \""+data+",\" from: "+packet.getAddress());
        return data;
    }

    public void send(byte[] data, int dest) throws IOException{
        
        InetAddress address= InetAddress.getLocalHost();   
        int port= dest;                       
    
        // create packet addressed to destination
        DatagramPacket packet= new DatagramPacket(data, data.length, address, port);
        NodeSocket.send(packet);
    }
}
