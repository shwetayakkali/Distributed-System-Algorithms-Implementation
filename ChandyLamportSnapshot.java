
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class ChandyLamportSnapshot implements Runnable {

    String operation;
    int server_Port = 5552;
    static int balance = 1000;
    static HashMap<String, Integer> server_map = new HashMap<String, Integer>();
    static String server_list[] = {"glados", "yes", "rhea"};
    //int[] attached_channels={0,0,0,0};
    static int marker = 0;
    static Boolean state_recorded = false;              //reset
    static HashSet<Integer> marker_list = new HashSet<Integer>();
    static HashMap<String, Integer> attached_channels = new HashMap<String, Integer>();     //move money to balance
    static int temp = 0;
    static HashMap<String, Boolean> acknowledgement = new HashMap<String, Boolean>();       //reset
    static int marker_receipt = 0;
    static String process1;
    static String process2;
    static boolean send_now = false;                //reset

    public ChandyLamportSnapshot(String operation) {
        this.operation = operation;

    }

    public ChandyLamportSnapshot() {
    }

    public static void main(String args[]) throws InterruptedException, UnknownHostException {
        initializeMap();
        InetAddress iAddress = InetAddress.getLocalHost();
        String server_name = iAddress.getHostName();
        receiptTracking(server_name);

        new ChandyLamportSnapshot().initializeChannels();
        Runnable receiver = new ChandyLamportSnapshot("receiver");
        new Thread(receiver).start();

        Thread.sleep(3000);

        Runnable sender = new ChandyLamportSnapshot("sender");
        new Thread(sender).start();

        Thread.sleep(3000);

        if (server_name.equals("glados")) {
            Runnable snapshot_initiator = new ChandyLamportSnapshot("snapshot");
            new Thread(snapshot_initiator).start();

        }

        while (true) {
        }

    }
   /**
    * initializeChannels() initializes the channel amounts.
    */
    public void initializeChannels() {
        attached_channels.put("glados", 0);
        attached_channels.put("yes", 0);
        attached_channels.put("rhea", 0);
    }
   /**
    * receiptTracking() initializes the other two processes for a particular process.
    */
    public static void receiptTracking(String current_server) {
        if (current_server.equals("glados")) {
            process1 = "yes";
            process2 = "rhea";

        } else if (current_server.equals("yes")) {
            process1 = "glados";
            process2 = "rhea";

        } else {
            process1 = "glados";
            process2 = "yes";

        }

    }
    /**
    * run() initializes sender, receiver and snapshot functionalities.
    */
    public void run() {
        ServerSocket serverSocket = null;
        try {

            ChandyLamportSnapshot obj = new ChandyLamportSnapshot();
            while (true) {

                if (operation == "sender") {
                    Thread.sleep(5000);
                    System.out.println("In sender#####################################");

                    System.out.println(operation);
                    obj.transfer();

                    if (send_now) {
                        InetAddress iAddress = InetAddress.getLocalHost();
                        String server_name = iAddress.getHostName();
                        System.out.println("Broadcasting from sender thread to "+server_name);
                        obj.broadcastMarkerToOthers(server_name);
                        send_now=false;

                    }

                } else if (operation == "receiver") {
                    serverSocket = new ServerSocket(server_Port);
                    System.out.println("In receiver######################################");

                    //System.out.println("Entered else");
                    Socket socket = serverSocket.accept();
                    System.out.println("Connection established");
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    String action = in.readUTF();
                    //System.out.println("ACTION is :"+action);
                    if (action.equals("transfer")) {
                        //synchronized(attached_channels){
                        String sending_server = in.readUTF();
                        //System.out.println("from "+sending_server);                       
                        String current_server = in.readUTF();
                        int amount = Integer.parseInt(in.readUTF());

                        if (state_recorded == true) {
                            System.out.println("State already recorded, listening on channel from " + sending_server + " -----> " + current_server);
                            int amount_more = amount + attached_channels.get(sending_server);
                            attached_channels.put(sending_server, amount);
                            System.out.println("Amount on channel is: " + amount_more);

                        } else {
                            //System.out.println("Inside state recorded= false");
                            System.out.println("State not recorded, Amount received from " + sending_server + " is : " + amount);
                            balance += amount;
                            System.out.println("Balance updated here as :" + balance);

                        }

                        serverSocket.close();
                        //}
                    } else {

                        //synchronized(attached_channels){                                    //no need for sync here
                        System.out.println("Receiving.....");
                        marker = Integer.parseInt(in.readUTF());
                        String from_server = in.readUTF();
                        String current_server = in.readUTF();
                        System.out.println("Received Marker from " + from_server + " at " + current_server);
                        if (!marker_list.contains(marker)) {
                            marker_list.add(marker);
                            System.out.println("Recorded state is:...........Balance is :" + balance);
                            state_recorded = true;
                            attached_channels.put(from_server, 0);
                            acknowledgement.put(from_server, true);
                            System.out.println("acknowledgement received in if for " + from_server + " at " + current_server + " as " + acknowledgement.get(from_server));
                            /*if (acknowledgement.containsKey(process1) && acknowledgement.containsKey(process2)) {
                                System.out.println("Coming here in if of marker not yet seen *********************** for server "+current_server+" from "+from_server);
                                if (acknowledgement.get(process1) && acknowledgement.get(process2)) {
                                    state_recorded = false;
                                    System.out.println("RECEIVED MARKER " + marker + " FROM BOTH " + process1 + " and " + process2 + " at " + current_server + " and state recorded as " + state_recorded);

                                }
                            }*/
                            
                            if (temp == 0) {
                                send_now = true;
                                //
                                
                                temp++;
                                
                            }

                        } else {
                            System.out.println("Marker " + marker + " is already present here at " + current_server);
                            System.out.println("Channel State from " + from_server + " -----> " + current_server + " is : " + attached_channels.get(from_server));
                            //marker_receipt++;
                            //System.out.println("marker receipt at "+current_server+" is "+marker_receipt+" for marker "+marker);

                            acknowledgement.put(from_server, true);
                            System.out.println("acknowledgement received in else for " + from_server + " at " + current_server + " as " + acknowledgement.get(from_server));

                            if (acknowledgement.containsKey(process1) && acknowledgement.containsKey(process2)) {
                                
                                if (acknowledgement.get(process1) && acknowledgement.get(process2)) {
                                    state_recorded = false;
                                    System.out.println("RECEIVED MARKER " + marker + " FROM BOTH " + process1 + " and " + process2 + " at " + current_server + " and state recorded as " + state_recorded);

                                }
                            }
                        }

                        serverSocket.close();

                        //}
                    }

                } else {
                    Thread.sleep(10000);
                    System.out.println("In Snapshot################################################");

                    //System.out.println("Taking the Snapshot... ");
                    //synchronized(attached_channels){                    //no need for sync here
                    if (temp == 0) {
                        System.out.println("Inside snapshot thread on glados " + temp);
                        obj.snapshot();
                        temp++;
                    }
                    //}

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
   /**
    * broadcastMarkerToOthers() sends the marker to the other two processes.
    */
    public void broadcastMarkerToOthers(String current_server) throws IOException {

        //System.out.println("In Broadcast :");
        System.out.println("In Broadcasting method, sending the Marker to others :" + marker);
        int curr_idx = server_map.get(current_server);
        synchronized (attached_channels) {
            for (int i = 0; i < server_list.length; i++) {                                  //sync after this line

                if (i != curr_idx) {
                    Socket socket = new Socket(server_list[i] + ".cs.rit.edu", server_Port);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF("marker");  
                    out.writeUTF(marker + "");
                    out.writeUTF(current_server);
                    out.writeUTF(server_list[i] + "");
                    System.out.println("Broadcast Marker sent " + current_server + " to : " + server_list[i]);
                    acknowledgement.put(server_list[i], false);
                    
                }
            }
        }

    }
    
   /**
    * snapshot() sends a unique marker to all the processes.
    */
    public void snapshot() throws IOException {

        System.out.println("Recording Balance on Process 1 as :" + balance);
        state_recorded = true;
        marker++;
        marker_list.add(marker);
        synchronized (attached_channels) {

            for (int i = 1; i < server_list.length; i++) {              //synchronize this part

                Socket socket = new Socket(server_list[i] + ".cs.rit.edu", server_Port);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF("marker");
                out.writeUTF(marker + "");
                out.writeUTF("glados");
                out.writeUTF(server_list[i] + "");
                System.out.println("Snapshot Marker " + marker + " sent from glados to : " + server_list[i]);
                acknowledgement.put(server_list[i], false);
            }
        }

    }
    
   /**
    * initializeMap() initializes the server map.
    */
    public static void initializeMap() {

        server_map.put("glados", 0);
        server_map.put("yes", 1);
        server_map.put("rhea", 2);

    }
   /**
    * transfer() send the marker.
    */
    public void transfer() throws UnknownHostException, IOException {
        Random random = new Random();
        int amount = random.nextInt(100);

        InetAddress iAddress = InetAddress.getLocalHost();
        String server_name = iAddress.getHostName();
        int self_index = server_map.get(server_name);
        //System.out.println("Amount to transfer is :"+amount);
        synchronized (attached_channels) {

            balance = balance - amount;
            int transfer_to = random.nextInt(3);
            while (transfer_to == self_index) {
                transfer_to = random.nextInt(3);
            }

            //System.out.println("Transferring... to "+server_list[transfer_to]);
            iAddress = InetAddress.getByName(server_list[transfer_to] + ".cs.rit.edu");

            Socket socket = new Socket(server_list[transfer_to] + ".cs.rit.edu", server_Port);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            out.writeUTF("transfer");
            out.writeUTF(server_name);
            out.writeUTF(server_list[transfer_to]);
            out.writeUTF(amount + "");
            System.out.println("Amount " + amount + " transferred to: " + server_list[transfer_to] + " Balance will be= " + balance);
            //System.out.println("Balance after transfer is "+balance);

        }

    }

}
