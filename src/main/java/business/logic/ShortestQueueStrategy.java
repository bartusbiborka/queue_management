package business.logic;

import model.Server;
import model.Task;

import java.util.List;

public class ShortestQueueStrategy implements Strategy{

    @Override
    public void addTask(List<Server> servers, Task t) {
        Server minServer = null;
        int minQueue = 9999;

        for(Server s : servers){
            int count =0;
            for(Task task: s.getTasks()){
                count++;
            }

            if(count < minQueue){
                minQueue = count;
                minServer = s;
            }
        }
        if(minServer != null){
            minServer.addTask(t);
        }
    }
}
