package business.logic;

import gui.SimulationFrame;
import model.Server;
import model.Task;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SimulationManager implements Runnable {
    private final Scheduler scheduler;
    private final SimulationFrame frame;
    private final List<Task> tasks;
    private double averageServiceTime;
    private int peakHour = 0;
    private int peakTasks = 0;

    public SimulationManager(SimulationFrame frame, int queueNumber, SelectionPolicy selectionPolicy) {
        tasks = new ArrayList<>();
        this.frame = frame;
        scheduler = new Scheduler(queueNumber);
        scheduler.changeStrategy(selectionPolicy);

    }

    public void generateRandomTasks(int minArrival, int maxArrival){
        for(int i =0; i<frame.getNumberOfClients(); i++){
            int arrival = minArrival+ (int)(Math.random()*(maxArrival - minArrival +1));
            int service = frame.getMinProcessingTime() + (int)(Math.random() * (frame.getMaxProcessingTime() - frame.getMinProcessingTime() + 1));
            Task task = new Task(arrival, service);
            tasks.add(task);
        }
        tasks.sort(Comparator.comparingInt(Task::getArrivalTime));
        int count=0;
        double average = 0;
        for(Task task : tasks){
            average += task.getServiceTime();
            count++;
        }
        average /= tasks.size();
        this.averageServiceTime = average;
    }

    //every second, this method counts how many tasks are processed in the queues, and decides whether its peakhour
    public void verifyPeakHour(int crtTime){
        List<Server> servers = scheduler.getServers();
        int taskNumber = 0;
        for(Server server : servers){
            for(Task task : server.getTasks()){
                taskNumber++;
            }
        }
        if(taskNumber > peakTasks){
            peakTasks = taskNumber;
            peakHour = crtTime;
        }
    }

    @Override
    public void run() {
        int currentTime=0;
        while(currentTime <= frame.getTimeLimit()){
            List<Task> toBeDeleted=new ArrayList<>();
            for(Task task: tasks){
                if(currentTime==task.getArrivalTime()){
                    scheduler.dispatchTask(task);
                    toBeDeleted.add(task);
                }
            }
            tasks.removeAll(toBeDeleted);
            toBeDeleted.clear();
            frame.updateSituation(currentTime, scheduler.getServers(), tasks);
            verifyPeakHour(currentTime);
            scheduler.decrementServiceTimes();
            if(currentTime == frame.getTimeLimit() || (tasks.isEmpty() && !scheduler.areThereWaitingClients())){
                //the simulation ends when the current time reaches simulation time or when there are no clients in the queues, with no clients waiting
                double average = scheduler.calculateAverageWaitingTime();
                JOptionPane.showMessageDialog(null, "Simulation is over!\nAverage waiting time: " + average + "\nAverage service time: " + averageServiceTime + "\nPeak hour: " + peakHour + ", with " + peakTasks + " tasks in the queues.");
                frame.writeAverageTime(average);
                break;
            }
            currentTime++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //getters and setters
    public int getPeakHour() {
        return peakHour;
    }

    public void setPeakHour(int peakHour) {
        this.peakHour = peakHour;
    }

    public int getPeakTasks() {
        return peakTasks;
    }

    public void setPeakTasks(int peakTasks) {
        this.peakTasks = peakTasks;
    }
}
