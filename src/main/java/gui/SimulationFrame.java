package gui;

import business.logic.SelectionPolicy;
import business.logic.SimulationManager;
import model.Server;
import model.Task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimulationFrame extends JFrame {
    private int maxProcessingTime;
    private int minProcessingTime;
    private int numberOfServers;
    private int numberOfClients;
    private int timeLimit;
    private FileWriter fileWriter;

    private JPanel simulationPanel;

    private List<JLabel> lblServers = new ArrayList<>();

    public SimulationFrame() {
        openWindow();
    }


    public void openWindow() {
        this.setTitle("Simulation Frame");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setSize(1000, 600);
        this.setLocationRelativeTo(null);

        //components to set up the simulation
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Number of clients:"));
        JTextField tfClient = new JTextField(10);
        panel.add(tfClient);

        panel.add(new JLabel("Number of queues:"));
        JTextField tfQueue = new JTextField(10);
        panel.add(tfQueue);

        panel.add(new JLabel("Simulation interval:"));
        JTextField tfSimInterval = new JTextField(10);
        panel.add(tfSimInterval);

        panel.add(new JLabel("Minimum and maximum arrival time:"));
        JTextField tfMinArrival = new JTextField(5);
        JTextField tfMaxArrival = new JTextField(5);
        panel.add(tfMinArrival);
        panel.add(tfMaxArrival);

        panel.add(new JLabel("Minimum and maximum service time:"));
        JTextField tfMinService = new JTextField(5);
        JTextField tfMaxService = new JTextField(5);
        panel.add(tfMinService);
        panel.add(tfMaxService);

        panel.add(new JLabel("Selection policy: "));
        String[] options = {"Shortest Waiting Time", "Shortest Queue"};
        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setSelectedIndex(0);
        comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(comboBox);


        JButton btnStart = new JButton("Start");
        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    //getting values from the textfields and combobox
                    numberOfClients = Integer.parseInt(tfClient.getText());
                    numberOfServers = Integer.parseInt(tfQueue.getText());
                    timeLimit = Integer.parseInt(tfSimInterval.getText());
                    int minArrival = Integer.parseInt(tfMinArrival.getText());
                    int maxArrival = Integer.parseInt(tfMaxArrival.getText());
                    minProcessingTime = Integer.parseInt(tfMinService.getText());
                    maxProcessingTime = Integer.parseInt(tfMaxService.getText());


                    String selectionPolicyCombo = (String) comboBox.getSelectedItem();
                    SelectionPolicy selectionPolicy = null;
                    if(selectionPolicyCombo.equals("Shortest Waiting Time")){
                        selectionPolicy=SelectionPolicy.SHORTEST_TIME;
                    }else{
                        selectionPolicy=SelectionPolicy.SHORTEST_QUEUE;
                    }


                    tfClient.setText("");
                    tfQueue.setText("");
                    tfSimInterval.setText("");
                    tfMinArrival.setText("");
                    tfMaxArrival.setText("");
                    tfMinService.setText("");
                    tfMaxService.setText("");

                    SimulationManager simulationManager = new SimulationManager(SimulationFrame.this, numberOfServers, selectionPolicy);
                    simulationManager.generateRandomTasks(minArrival, maxArrival);
                    displaySimulation();
                    Thread t = new Thread(simulationManager);
                    t.start();
                }catch (NumberFormatException ex){
                    JOptionPane.showMessageDialog(SimulationFrame.this, "Please enter valid numbers.");

                }
            }
        });

        panel.add(btnStart);
        this.add(panel);
        this.setVisible(true);

    }

    public void displaySimulation(){
        //for the user to view the simulation
        this.getContentPane().removeAll();
        simulationPanel = new JPanel();
        simulationPanel.setLayout(new BoxLayout(simulationPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(simulationPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add(scrollPane, BorderLayout.CENTER);

        //opening the output file
        try {
            fileWriter = new FileWriter("result.txt");
        }catch (IOException ex){
            JOptionPane.showMessageDialog(this, "Error opening the file result.txt.");
        }

        this.revalidate();
        this.repaint();
    }

    public void updateSituation(int currentTime, List<Server> servers, List<Task> tasks){
        //every second a new box gets added to the panel that contains the simulation info
        //these infos also get written in result.txt file, using a stringbuilder
        JPanel situationPanel = new JPanel();
        situationPanel.setLayout(new BoxLayout(situationPanel, BoxLayout.Y_AXIS));
        situationPanel.setBorder(BorderFactory.createEtchedBorder());

        StringBuilder fileText = new StringBuilder();

        JLabel lblCurrentTime = new JLabel("Current time: " + currentTime);
        situationPanel.add(lblCurrentTime);
        fileText.append("Current time: ").append(currentTime).append("\n");

        StringBuilder text = new StringBuilder();
        for(Task t: tasks){
            text.append("(").append(t.getId()).append(", ").append(t.getArrivalTime()).append(", ").append(t.getServiceTime()).append(");");
        }
        JLabel lblWaiting = new JLabel("Waiting clients: " + text.toString());
        situationPanel.add(lblWaiting);
        fileText.append("Waiting clients: ").append(text.toString()).append("\n");

        for(int i = 0; i<servers.size(); i++){
            Server server = servers.get(i);
            StringBuilder taskText = new StringBuilder();

            for(Task t: servers.get(i).getTasks()){
                taskText.append("(").append(t.getId()).append(", ").append(t.getArrivalTime()).append(", ").append(t.getServiceTime()).append(");");
            }

            if(taskText.isEmpty()){
                taskText.append("closed");
            }
            JLabel lblServer = new JLabel("Server " + (i+1) + ": " + taskText);
            situationPanel.add(lblServer);
            fileText.append("Server: ").append(i+1).append(": ").append(taskText.toString()).append("\n");
        }

        simulationPanel.add(situationPanel);
        simulationPanel.revalidate();
        simulationPanel.repaint();

        try{
            fileWriter.write(fileText.toString() + "\n");
            fileWriter.flush();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error writing to file.");
        }
    }

    //at the end of the smiulation, the average waiting time gets written in the file
    public void writeAverageTime(double average){
        try {
            fileWriter.write("Average waiting time: " + average + "\n");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error writing to file.");
        }
    }

    //setters and getters
    public int getMaxProcessingTime() {
        return maxProcessingTime;
    }

    public void setMaxProcessingTime(int maxProcessingTime) {
        this.maxProcessingTime = maxProcessingTime;
    }

    public int getMinProcessingTime() {
        return minProcessingTime;
    }

    public void setMinProcessingTime(int minProcessingTime) {
        this.minProcessingTime = minProcessingTime;
    }

    public int getNumberOfServers() {
        return numberOfServers;
    }

    public void setNumberOfServers(int numberOfServers) {
        this.numberOfServers = numberOfServers;
    }

    public int getNumberOfClients() {
        return numberOfClients;
    }

    public void setNumberOfClients(int numberOfClients) {
        this.numberOfClients = numberOfClients;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public List<JLabel> getLblServers() {
        return lblServers;
    }

    public void setLblServers(List<JLabel> lblServers) {
        this.lblServers = lblServers;
    }
}
