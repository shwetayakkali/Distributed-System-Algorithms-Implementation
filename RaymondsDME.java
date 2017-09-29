
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Shweta Yakkali
 */
public class RaymondsDME implements Runnable{
    static int server_Port = 5511 ;
    String operation;
    static int holder=-1;
    static int token=-1;
    static HashMap<Integer,String> holder_mapping= new HashMap<Integer,String>();
    static Queue<Integer> queue= new LinkedList<Integer>();
    static boolean request_sent=false;
    static int ctr=0;
    static boolean critical_section=false;
    static boolean send_token=false;
    static int self_number=-1;
    static int y;
    static boolean send_request=false;
    static String global_lock="hi";
    
    
    
    public RaymondsDME(String operation) {
        this.operation = operation;

    }

    public RaymondsDME() {}
    
    public static void main(String args[]) throws UnknownHostException, InterruptedException{
        initializeTree();
        
        Runnable receiver = new RaymondsDME("receiver");
        new Thread(receiver).start();
        
        Thread.sleep(2000);

        Runnable sender = new RaymondsDME("sender");
        new Thread(sender).start();
        
        Thread.sleep(2000);
        
        Runnable requesting = new RaymondsDME("requesting");
        new Thread(requesting).start();

        while (true) {
        }
    }
    
    public void run(){//throws UnknownHostException, IOException, InterruptedException {
        
        try{
            
            RaymondsDME obj = new RaymondsDME();
            while(true){

                
                InetAddress iAddress = InetAddress.getLocalHost();
                String server_name = iAddress.getHostName();
                 
                if(operation.equals("requesting")){
                    if(critical_section){
                            System.out.println("*******ENTERED CRITICAL SECTION*********");
                            Thread.sleep(5000);
                            obj.exitCriticalSection();
                            System.out.println("********* EXITED CRITICAL SECTION******** ");
                            Thread.sleep(3000);
                            
                    }
                    else if((ctr==0|| ctr==1) && (self_number!=1) && !critical_section){
                        //System.out.println("Inside requesting thread counter = "+ ctr);
                        if(!request_sent ){//&& token!=1 ){//&& !critical_section){
                            
                            queue.add(self_number);
                            System.out.println("######## Requesting TOKEN......... for "+ctr+"  time");
                            request_sent=true;
                            //obj.send(self_number,holder,"request");
                            send_request=true;
                            ctr++; 
                            Thread.sleep(10000); 
                        }
                                                                         //comment this for multiple
                        
                        
                        }                                             
                                                                         
                                                                         
                                                                         
                    }
                    
                       
                else if(operation.equals("sender")){
                    //System.out.println("Inside Sender thread ");
                    
                    if (send_request){
                        //System.out.println("Sending request....");
                        //synchronized(global_lock){
                            Thread.sleep(500);
                            obj.send(self_number,holder,"request");
                            send_request=false;
                        //}
                        
                    }

                    if(send_token){
                        //System.out.println("Sending token.....");
                        //synchronized(global_lock){
                            obj.send(self_number,holder,"token");
                            send_token=false;
                        //}
                        
                        
                    }
                        
                    
                }

                else if(operation.equals("receiver")){
                    ServerSocket serverSocket = null;
                    try{
                    //System.out.println("Inside receiver thread.......");                    
                    serverSocket = new ServerSocket(server_Port);
                    
                    //System.out.println("Before accept statement......");
                    Socket socket = serverSocket.accept();
                    System.out.println("Connection established, Inside receiver thread at "+server_name);
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    String option=in.readUTF();


                    
                    
                    if(option.equals("token")){
                        
                        int parent=Integer.parseInt(in.readUTF());                          
                        System.out.println("Received TOKEN from     :"+parent);
                        //token=1;
                        //synchronized(queue){
                        
                        holder=queue.remove();
                        
                        //}
                        //System.out.println("Holder Check In Token: Holder after dequeueing is "+self_number+" ---> "+holder);
                        if(holder==self_number){
                            token=1;
                            critical_section=true;
                            System.out.println("Received Token, Entered Critical Section");
                            
                            request_sent=false;                         //original
                           
                            

                        }
                        else {
                            if(holder!=self_number){
                            System.out.println("Displaying queue during sending token   "+queue);
                            //synchronized(global_lock){
                                
                                token=0;
                                send_token=true;
                                
                            
                            //}

                            }
                            if(!queue.isEmpty()){
                                //synchronized(global_lock){
                                request_sent=true;                                               // change
                                send_request=true;
                                
                            }
                        }                       
                    }
                    
                    else if(option.equals("request")){                           
                        obj.receivedRequest(in,socket);
                    }
                    
                    serverSocket.close();
                    
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                finally{
                    serverSocket.close();
                }
                }
                
            
                
            }
            
        }
        catch(Exception e){
            //System.out.println(e);
            e.printStackTrace();
        }
        
    }
    
    public void exitCriticalSection() throws IOException, UnknownHostException, InterruptedException{
        synchronized(queue){
        RaymondsDME obj=new RaymondsDME();
        if(queue.isEmpty()){
            //System.out.println("Queue of the process which is in critical section is empty, Hold the token");
            token=1;
            critical_section=false;
            System.out.println("Token held as queue here is empty, but critical section= "+critical_section);
         
        }
        else{
            System.out.println("Queue here is not empty but Releasing the token in the Exit Critical Section");
            
            critical_section=false;
            
            holder=queue.remove();
            //obj.send(self_number,holder,"token");
            //synchronized(global_lock){
                token=0;
                request_sent=false;                         //changed
                send_token=true;
                Thread.sleep(2000);
            //}
            
            if(!queue.isEmpty()){
                //synchronized(global_lock
                    
                    send_request=true;
                    request_sent=true;                          //changed
                //}
                //obj.send(self_number,holder,"request");
            }
        }
    
        }
    }
    public void receivedRequest(DataInputStream in, Socket socket) throws IOException{
        RaymondsDME obj = new RaymondsDME();
        int child=Integer.parseInt(in.readUTF());
        System.out.println("Received REQUEST for token from    :"+child);
        boolean present=obj.checkIfElementInQueue(child);
        
        if(!present){
            queue.add(child);
            
            System.out.println("Process "+child+" not present in Queue, added to queue");
            System.out.println("Displaying queue     "+queue);
        }
        
        if(token==1 && critical_section){
            System.out.println("Inside Critical Section, Do Nothing!");
        }
        else {
            if(token==1 && !critical_section){
                System.out.println("Token present here but not in Critical Section");
                System.out.println("Displaying queue when I have token and don't need CS   "+queue);

                synchronized(queue){
                    y=queue.remove();  
                    
                    request_sent=false;                 //changed
                    
                    holder=y;
                    send_token=true;
                    token=0;
                }      

                //System.out.println("Holder Check In Request : Holder after dequeueing is "+self_number+" ---> "+holder);

                //synchronized(global_lock){
//                    holder=y;
                    
                //}

            }
            if(!queue.isEmpty()){
            //send(self_number,holder,"request");
                System.out.println("Queue not empty, Forwarding the request for token ");
                //synchronized(global_lock){
                
                send_request=true; 
                request_sent=true;                         //changed

                //}

            }
        }

        
        
        //}
        
        
    }
    
    
    
    public void send(int source,int destination,String type) throws UnknownHostException, IOException, InterruptedException{
        //synchronized(global_lock){
        //System.out.println("Sending started from "+source+" to "+destination+" for "+type);
        String destination_server=holder_mapping.get(destination);
//        InetAddress iAddress = InetAddress.getByName(destination_server + ".cs.rit.edu");
        Socket socket = new Socket(destination_server + ".cs.rit.edu", server_Port);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        //
        
        out.writeUTF(type);      
        out.writeUTF(source+"");
        //sending_flag=-1;
        System.out.println("Sent !!!!!! "+type+" to "+destination_server);
        
       if(type.equals("request")){
            Thread.sleep(1000);
        }   
    
    }
    
    
    public boolean checkIfElementInQueue(int child){
        boolean flag=false;
        if(queue.isEmpty())
            flag= false;
        else{
            //sSystem.out.println("In queue not empty of check "+child);
            Iterator it = queue.iterator();
            while (it.hasNext()){
                String temp= it.next().toString();
                //System.out.println(" temp string "+temp);
                int tempint=Integer.valueOf(temp);
                //System.out.println("temp int= "+ tempint);
                if(tempint==child){
                    flag= true;
                    break;
                }
            }
        }
        return flag;
    }
    
    /*public void sendRequest(String source_server) throws UnknownHostException, IOException{
        String destination_server=holder_mapping.get(holder);
        InetAddress iAddress = InetAddress.getByName(destination_server + ".cs.rit.edu");
        Socket socket = new Socket(destination_server + ".cs.rit.edu", server_Port);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeUTF("request");
        out.writeUTF(source_server);
        out.writeUTF(self_number+"");
        
        
    }*/
    
    public static void initializeTree() throws UnknownHostException{
        InetAddress iAddress = InetAddress.getLocalHost();
        String server_name = iAddress.getHostName();
        if(server_name.equals("glados")){
            holder=1;
            token=1;
            self_number=1;
            //critical_section=true;
            holder_mapping.put(1,"glados");
            holder_mapping.put(2,"comet");
            holder_mapping.put(3,"doors");
            System.out.println("The holder for "+server_name+self_number+" is "+holder+" which is "+holder_mapping.get(holder));
            
        }
        else if(server_name.equals("comet")|| server_name.equals("doors")){
            holder=1;
            if(server_name.equals("comet")){
                self_number=2;
                holder_mapping.put(1,"glados");
                holder_mapping.put(4,"gorgon");
                holder_mapping.put(5,"queeg");
                System.out.println("The holder for "+server_name+self_number+" is "+holder+" which is "+holder_mapping.get(holder));
            }
            else{
                self_number=3;
                holder_mapping.put(1,"glados");
                holder_mapping.put(6,"rhea");
                holder_mapping.put(7,"yes");
                System.out.println("The holder for "+server_name+self_number+" is "+holder+" which is "+holder_mapping.get(holder));
           
            } 
        }
        
        else if(server_name.equals("gorgon")||server_name.equals("queeg")){
            holder=2;
            holder_mapping.put(2,"comet");
            if(server_name.equals("gorgon"))
                self_number=4;
            else
                self_number=5;
            System.out.println("The holder for "+server_name+self_number+" is "+holder+" which is "+holder_mapping.get(holder));
            
        }
        else if(server_name.equals("rhea")||server_name.equals("yes")){
            holder=3;
            holder_mapping.put(3,"doors");
            if(server_name.equals("rhea"))
                self_number=6;
            else
                self_number=7;
            System.out.println("The holder for "+server_name+self_number+" is "+holder+" which is "+holder_mapping.get(holder));
        }
    
    }
}
