package business.logic;

import model.Server;
import model.Task;

import java.util.List;

public class TimeStrategy implements Strategy {

    @Override
    public void addTask(List<Server> servers, Task t) {
        int minimTime = 99999;
        Server minServer = null;

        for(Server server : servers){
            int waitingTime = server.getWaitingPeriod().get();
            if(waitingTime < minimTime){
                minServer = server;
                minimTime=waitingTime;
            }
        }

        if(minServer != null){
            minServer.addTask(t);
        }
    }
}
