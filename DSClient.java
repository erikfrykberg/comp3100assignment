import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class DSClient {
    
    //variable declarations.
    static DataOutputStream dout;
    static BufferedReader din;
    static String str;

    public static void main(String[] args) throws Exception {
        Socket s=new Socket("localhost",50000);  
        din = new BufferedReader(new InputStreamReader(s.getInputStream())); 
        dout = new DataOutputStream(s.getOutputStream());  
        
        /* COMMAND:
            
                ./ds-server -c '/home/erik/Documents/ds-sim/configs/sample-configs/ds-sample-config01.xml' -v brief -n
            
            or for more info:
                
                ./ds-server -c '/home/erik/Documents/ds-sim/configs/sample-configs/ds-sample-config01.xml' -v all -n
        */


        System.out.println("Server started + established connection correctly!\n");
        str = "";

        /**
         * 
         *  HANDSHAKE PROTOCOL!
         * 
        */

        push("HELO");

        //EXPECT "OK"
        recieve();

        //SEND AUTH
        push("AUTH erik");

        //EXPECT 'OK'
        recieve();

        /**
         * 
         *  LOOP THROUGH THE JOBS.
         * 
        */

        //JOB VARIABLES
        String jobId; 
        String jobCommand;

        // SERVER VARIABLES
        String largestType = ""; //store the largest type.
        int numberOfServers = 0;
        int index = 0;

        System.out.println(" ---<>--- str equals: " + str + " ---<>--- \n");
        //WHILE THERE ARE JOBS TO SCHEDULE.
        while(!str.equals("NONE")) {

            //SEND REDY (for jobs)
            push("REDY");

            //EXPECT JOBN or NONE RESPONSE
            recieve();

            if(str.equals("NONE")) {
                break;
            }

            //the following is the JOB breakdown.
            String[] jobStrings = str.split(" "); 
            jobCommand = jobStrings[0];
            jobId = jobStrings[2];

            //if the largest server type has not been set, then:
            System.out.println(" ---<>--- largest type equals: " + largestType + " ---<>---\n");
            if(largestType.equals("")) {
                
                String coresRequired = jobStrings[jobStrings.length - 3];
                String memoryRequired = jobStrings[jobStrings.length - 2];
                String disksRequired = jobStrings[jobStrings.length - 1];

                //REQUEST SERVERS
                push("GETS Capable " + coresRequired + " " + memoryRequired + " " + disksRequired);
    
                //RECIEVE THE DATA [number] [length of characters].
                recieve();
                Integer totalServers = Integer.parseInt(str.split(" ")[1]);
                System.out.println(" ---<>--- There should be: " + str.split(" ")[1] + " number of servers ---<>--- \n");
    
                //SEND 'OK'
                push("OK");
    
                int mostCores = 0;
                String currentType = "";
                //RECIEVE THE SERVERS - MUST BE AT LEAST 1!
                for(int i = 0; i < totalServers; i++){
                    recieve();
                    String[] serverInformation = str.split(" ");
                    Integer cores = Integer.parseInt(serverInformation[4]);

                    //bigger than the old one.. i.e. 16 over 4
                    if(cores > mostCores){
                        largestType = serverInformation[0]; //set to the biggest type
                        numberOfServers = 0; //reset the number of servers.
                        mostCores = cores; //set the new mostCores.
                        currentType = serverInformation[0]; //set the current type we're looking for.
                    }
                    //increase number of servers if of the same type.
                    if(serverInformation[0].equals(currentType)){
                        numberOfServers++;
                    }
                }

                push("OK");

                //RECIEVE '.'
                recieve();
            }

            /**
             * 
             *  SCHEDULE THE JOBS BY INDEX.
             * 
            */
            
            //check to make sure that it is a normal JOB.
            if(jobCommand.equals("JOBN")){
                if(index == numberOfServers){
                    index = 0;
                }
                System.out.println("---<>--- number of servers: " + numberOfServers + "\n");
                System.out.println("---<>--- index: " + index + "\n");
                //SEND JOB SCHEDULE
                push("SCHD " + jobId + " " + largestType + " " + index);
                index++;

                recieve();
            } else if(jobCommand.equals("JCPL")){
                System.out.println("-- COMPLETED JOB " + jobId + "!\n");
            }
        }

        /**
             * 
             *  QUIT!
             * 
            */

        //QUIT!
        push("QUIT");
        dout.flush();

        str = din.readLine();
        System.out.println("Connection Closed.." + "\u001B[0m");

        dout.close();  
        s.close();  
    }

    static void recieve() throws IOException {
        str = din.readLine();
        System.out.println("RCVD: \'" + str + "\'\n");
    }

    static void push(String str) throws IOException {
        str = str + "\n";
        dout.write((str).getBytes());
        System.out.println("SENT: " + str);
        dout.flush();
    }

}