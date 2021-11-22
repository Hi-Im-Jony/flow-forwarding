package Router;
import java.util.HashMap;

public class Router {
    
}
class ForwardingService {
    
    HashMap<String, Integer> forwardingTable;

    ForwardingService(){
        forwardingTable = new HashMap<>();
    }

    public int forward(String dest){
        if(forwardingTable.containsKey(dest))
            return forwardingTable.get(dest);
        else
            contactController(dest);
            return forwardingTable.get(dest);
    }

    private void contactController(String dest){
        // TODO
    }

}