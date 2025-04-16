package business.logic;

import model.Server;
import model.Task;

import java.util.ArrayList;
import java.util.List;

import static business.logic.SelectionPolicy.SHORTEST_QUEUE;
import static business.logic.SelectionPolicy.SHORTEST_TIME;

public class Scheduler {
    private List<Server> servers;
    private int maxNoServers;
    private Strategy strategy;

    public Scheduler(int maxNoServers) {
        this.maxNoServers = maxNoServers;
        this.servers = new ArrayList<Server>();

        for(int i=0; i<maxNoServers; i++) {
            this.servers.add(new Server());

            Thread serverThread = new Thread(servers.get(i));
            serverThread.start();
        }
    }



    public void changeStrategy(SelectionPolicy selectionPolicy){
        if(selectionPolicy==SHORTEST_TIME){
            strategy = new TimeStrategy();
        }
        if (selectionPolicy==SHORTEST_QUEUE){
            strategy = new ShortestQueueStrategy();
        }
    }

    public void dispatchTask( Task t){
        if(strategy!=null){
            strategy.addTask(servers, t);}
    }

    //for the tasks that are being served, the method decrements the servicetime and calculates the new waitinig time for each queue
    public void decrementServiceTimes(){
        for(Server s : servers){
            s.decreaseTaskServiceTime();
            s.calculateWaitingTime();
        }
    }

    //when the simulation is over, this method iterates through the clients that have been served and calculates the average waiting time based on their waiting time
    public double calculateAverageWaitingTime(){
        double average =0;
        int counter =0;
        for(Server server:servers){
            for(Task task: server.getTasksDone()){
                average += task.getWaitingTime();
                counter++;
            }
        }

        return average / counter;
    }

    //used for verifying whether the simulation should stop
    public boolean areThereWaitingClients(){
        for(Server server:servers){
            if(!server.getTasks().isEmpty()){
                return true;
            }
        }
        return false;
    }

    //getters and setters
    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public int getMaxNoServers() {
        return maxNoServers;
    }

    public void setMaxNoServers(int maxNoServers) {
        this.maxNoServers = maxNoServers;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

}
