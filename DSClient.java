import java.net.*;
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
        push("AUTH ERIK.FRYKBERG");

        //EXPECT 'OK'
        recieve();

        /**
         * 
         *  LOOP THROUGH THE JOBS.
         * 
        */
        System.out.println(" ---<>--- str equals: " + str + " ---<>--- \n");
        //WHILE THERE ARE JOBS TO SCHEDULE.
        // while(!str.equals("NONE")) {

            //SEND REDY (for jobs)
            push("REDY");

            //EXPECT JOBN or NONE RESPONSE
            recieve();

            //the following is the first JOB breakdown.
            String[] jobStrings = str.split(" "); 
            String coresRequired = jobStrings[jobStrings.length - 3];
            String memoryRequired = jobStrings[jobStrings.length - 2];
            String disksRequired = jobStrings[jobStrings.length - 1];
            String jobId = jobStrings[2];

            //REQUEST SERVERS
            push("GETS Capable " + coresRequired + " " + memoryRequired + " " + disksRequired);

            //RECIEVE THE DATA [number] [length of characters].
            recieve();

            //SEND 'OK'
            push("OK");

            //RECIEVE THE SERVERS.
            recieve();
            // str = din.readLine();
            // System.out.println("RCVD: \'" + str + "\'\n");
        // }

        // String[] brkn = str.split(" ");
        // String cores = brkn[brkn.length - 3];
        // String mem = brkn[brkn.length - 2];
        // String disk = brkn[brkn.length - 1];
        // String id = brkn[2];
        // System.out.println("---<>--- id: " + id + ", cores: " + cores + ", mem: " + mem + ", disk: " + disk + "---<>---\n");

        // //SEND JOB SCHEDULE
        // push("GETS Capable " + cores + " " + mem + " " + disk);

        // //READ LINE
        // recieve();

        // //SEND OK
        // push("OK");
        // dout.flush();

        // //READ LINE
        // recieve();

        // //SEND OK x2
        // push("OK");
        // dout.flush();

        // //READ LINE
        // recieve();

        // String[] brkn2 = str.split(" "); 
        // String type = brkn2[0];
        // String serverId = brkn2[1];

        // //SEND JOB SCHEDULE
        // push("SCHD " + id + " " + type + " " + serverId);

        // //READ LINE
        // recieve();

        // //SEND OK
        // push("ok");
        // dout.flush();

        // //READ LINE
        // recieve();

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