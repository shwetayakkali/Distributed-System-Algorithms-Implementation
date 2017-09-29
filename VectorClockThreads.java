
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * VectorClockThreads class gives the vector time ordering of the events that occur.
 *
 * @author Shweta Yakkali
 */
public class VectorClockThreads implements Runnable {

    String operation;
    static int server_Port = 5553;
    static int vector_time[] = {0, 0, 0};
    static int balance = 1000;
    static HashMap<String, Integer> server_map = new HashMap<String, Integer>();
    static String server_list[] = {"glados", "yes", "rhea"};

    public VectorClockThreads(String operation) {
        this.operation = operation;

    }

    public VectorClockThreads() {
    }
    
   /**
    * initializes the index mapping
    */
    public static void initializeMap() {

        server_map.put("glados", 0);
        server_map.put("yes", 1);
        server_map.put("rhea", 2);

    }
   /**
    * Main() function calls all the methods.
    */
    public static void main(String args[]) throws InterruptedException {
        initializeMap();

        Runnable receiver = new VectorClockThreads("receiver");
        new Thread(receiver).start();
        Thread.sleep(3000);

        Runnable sender = new VectorClockThreads("sender");
        new Thread(sender).start();

        while (true) {
        }

    }
    /**
    * run() function has all the thread functionality.
    */
    public void run() {
        ServerSocket serverSocket = null;
        try {
            VectorClockThreads obj = new VectorClockThreads();

            while (true) {

                if (operation == "sender") {
                    Thread.sleep(5000);
                    System.out.println(operation);
                    obj.selectAnEvent(obj);
                    System.out.println("-------------------------");

                } else {
                    serverSocket = new ServerSocket(server_Port);
                    //System.out.println("In receiver ###############################################################");

                    Socket socket = serverSocket.accept();
                    System.out.println("Connection established");
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    synchronized (vector_time) {
                        String sending_server = in.readUTF();
                        System.out.println("from " + sending_server);

                        String current_server = in.readUTF();
                        int amount = Integer.parseInt(in.readUTF());
                        String vector_incoming = in.readUTF();
                        obj.processTheIncomingArrayString(vector_incoming, current_server);
                        balance += amount;

                        System.out.println("Vector clock at " + current_server + " Process is:");
                        System.out.println(vector_time[0] + "  " + vector_time[1] + "  " + vector_time[2]);
                        serverSocket.close();
                    }

                }

            }
        } catch (Exception ex) {

            System.out.println("Exception Occurred");
        }

    }
   /**
    * processTheIncomingArrayString() processes the incoming vector array.
    */
    public void processTheIncomingArrayString(String vector_incoming, String current_server) {
        vector_incoming = vector_incoming.substring(1);
        vector_incoming = vector_incoming.substring(0, vector_incoming.length() - 1);
        String list[] = vector_incoming.split(", ");
        int temp_vector[] = new int[3];
        int index = server_map.get(current_server);
        vector_time[index] += 1;
        for (int i = 0; i < list.length; i++) {

            temp_vector[i] = Integer.parseInt(list[i]);
            vector_time[i] = Math.max(vector_time[i], temp_vector[i]);
        }

    }
   /**
    * selectAnEvent() selects the event out of deposit, withdraw and transfer.
    */
    public void selectAnEvent(VectorClockThreads obj) throws UnknownHostException, IOException {
        Random random = new Random();
        int option = random.nextInt(3);
        //option=2;
        if (option == 0) {
            System.out.println("Deposit Selected");
            obj.deposit();
        } else if (option == 1) {
            System.out.println("Withdraw Selected");
            obj.withdraw();

        } else {
            obj.transfer();

        }

    }
    /**
    * deposit() adds the funds to the balance
    */
    public void deposit() throws UnknownHostException {
        Random random = new Random();
        int amount = random.nextInt(100);
        System.out.println("Amount to deposit is :" + amount);
        //lock
        synchronized (vector_time) {
            balance += amount;
            System.out.println("Balance after deposit is :" + balance);
            InetAddress iAddress = InetAddress.getLocalHost();
            String server_name = iAddress.getHostName();
            System.out.println("Vector clock to be updated for :" + server_name);
            int index = server_map.get(server_name);
            System.out.println("Index is :" + index);
            vector_time[index] += 1;
            System.out.println("Vector clock updated as :" + vector_time[index]);
            System.out.println("Vector clock at " + server_name + " Process is:");
            System.out.println(vector_time[0] + "  " + vector_time[1] + "  " + vector_time[2]);

        }

    }
    /**
    * withdraw() subtracts the amount from the balance.
    */
    public void withdraw() throws UnknownHostException {
        Random random = new Random();
        int amount = random.nextInt(100);
        System.out.println("Amount to withdraw is :" + amount);
        if (balance - amount > 0) {
            synchronized (vector_time) {
                balance -= amount;
                System.out.println("Balance after withdraw is :" + balance);
                InetAddress iAddress = InetAddress.getLocalHost();
                String server_name = iAddress.getHostName();
                System.out.println("Vector clock to be updated for :" + server_name);
                int index = server_map.get(server_name);
                vector_time[index] += 1;
                System.out.println("Vector clock updated as :" + vector_time[index]);
                System.out.println("Vector clock at " + server_name + " Process is:");
                System.out.println(vector_time[0] + "  " + vector_time[1] + "  " + vector_time[2]);
            }
        } else {
            System.out.println("Insufficient Funds, Event can't occur");

        }
    }
   /**
    * transfer() subtracts the amount from the balance and transfers the amount to a randomly selected process.
    */
    public void transfer() throws UnknownHostException, IOException {
        Random random = new Random();
        int amount = random.nextInt(100);
        InetAddress iAddress = InetAddress.getLocalHost();
        String server_name = iAddress.getHostName();
        int self_index = server_map.get(server_name);
        System.out.println("Amount to transfer is :" + amount);
        if (balance - amount > 0) {
            synchronized (vector_time) {
                balance -= amount;
                int transfer_to = random.nextInt(3);
                while (transfer_to == self_index) {
                    transfer_to = random.nextInt(3);

                }
                vector_time[self_index] += 1;

                System.out.println("Transferring... to " + server_list[transfer_to] + ".cs.rit.edu");
                iAddress = InetAddress.getByName(server_list[transfer_to] + ".cs.rit.edu");

                Socket socket = new Socket(server_list[transfer_to] + ".cs.rit.edu", server_Port);

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(server_name);
                out.writeUTF(server_list[transfer_to]);
                out.writeUTF(amount + "");
                out.writeUTF(Arrays.toString(vector_time));
                System.out.println("Transferred to: " + server_list[transfer_to]);

            }

        } else {
            System.out.println("Insufficient Funds, Event can't occur");
        }
    }
}
