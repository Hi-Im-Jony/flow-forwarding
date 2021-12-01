package Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Node {
    // header types
    final static int CONTROLLER_REPLY = 0; // packet is a reply from the controller
    final static int COMBINATION = 1; // header type is a combo of other various types
    final static int DESTINATION_ID = 2; // end node packet is being sent to
    final static int SOURCE_ID = 3; // source of packet
    final static int UPDATE = 4; // always length 2, first value is id of router to change, second value is updated data

    // Datagram stuff
    final static int MTU = 1500;
    final static int NODE_PORT = 1;
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
