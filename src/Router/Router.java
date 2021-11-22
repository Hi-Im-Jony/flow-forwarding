package Router;
import java.util.HashMap;

public class Router {
    private ForwardingService service;
    private int id;

    Router(){
        service = new ForwardingService();
    }

    public static void main(String[] args) {
        
    }

    
}
class ForwardingService {
    
    HashMap<String, Integer> forwardingTable;

    ForwardingService(){
        forwardingTable = new HashMap<>();
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