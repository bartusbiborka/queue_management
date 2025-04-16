package model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {
    private BlockingQueue<Task> tasks;
    private AtomicInteger waitingPeriod;
    private List<Task> tasksDone;


    public Server(){
        tasksDone = new ArrayList<Task>();
        tasks = new LinkedBlockingQueue<Task>();
        waitingPeriod = new AtomicInteger(0);
    }

    //decrements the service time for the clients that are being served. the method is called every second
    public void decreaseTaskServiceTime(){
        if(!tasks.isEmpty()){
            Task t = tasks.peek();
            t.setServiceTime(t.getServiceTime()-1);
        }
    }

    @Override
    public void run() {
        while(true){
            Task t = null;
            try {
                t = tasks.peek();

                if (t != null) {
                    Thread.sleep(t.getServiceTime() * 1000L);
                    tasks.take();
                    //waitingPeriod.getAndAdd(-t.getServiceTime());
                    tasksDone.add(t);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addTask(Task newTask){
        try{
            tasks.put(newTask);
            waitingPeriod.getAndAdd(newTask.getServiceTime());
            newTask.setWaitingTime(waitingPeriod.get());
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    //getters and setters
    public BlockingQueue<Task> getTasks() {
        return tasks;
    }

    public void setTasks(BlockingQueue<Task> tasks) {
        this.tasks = tasks;
    }

    public AtomicInteger getWaitingPeriod() {
        return waitingPeriod;
    }

    public void setWaitingPeriod(AtomicInteger waitingPeriod) {
        this.waitingPeriod = waitingPeriod;
    }

    public List<Task> getTasksDone() {
        return tasksDone;
    }

    public void setTasksDone(List<Task> tasksDone) {
        this.tasksDone = tasksDone;
    }

    public void calculateWaitingTime(){
        if(waitingPeriod.get() > 0){
            waitingPeriod.getAndDecrement();
        }
    }
}
