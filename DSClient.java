import java.net.*;
import java.io.*;

public class DSClient {
    static DataOutputStream dout;
    public static void main(String[] args) throws Exception {
        Socket s=new Socket("localhost",50000);  
        BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream())); 
        dout = new DataOutputStream(s.getOutputStream());  
        
        /* COMMAND:
            
                ./ds-server -c '/home/erik/Documents/ds-sim/configs/sample-configs/ds-sample-config01.xml' -v brief -n
            
            or for more info:
                
                ./ds-server -c '/home/erik/Documents/ds-sim/configs/sample-configs/ds-sample-config01.xml' -v all -n
        */


        System.out.println("Server started + established connection correctly!\n");
        String str = "";

        /**
         * 
         *  HANDSHAKE PROTOCOL!
         * 
        */

        push("HELO");

        //EXPECT "OK"
        str = din.readLine();
        recieved(str);

        //SEND AUTH
        push("AUTH ERIK.FRYKBERG");

        //EXPECT 'OK'
        str = din.readLine();
        recieved(str);

        /**
         * 
         *  DETERMINE AND SAVE THE LARGEST SERVER TYPES.
         * 
        */
        


        
        //SEND REDY
        push("REDY");

        //EXPECT SYSTEM LOG INFORMATION
        str = din.readLine();
        recieved(str);

        String[] brkn = str.split(" ");
        String cores = brkn[brkn.length - 3];
        String mem = brkn[brkn.length - 2];
        String disk = brkn[brkn.length - 1];
        String id = brkn[2];
        System.out.println("id: " + id + ", cores: " + cores + ", mem: " + mem + ", disk: " + disk);

        //SEND JOB SCHEDULE
        push("GETS Capable " + cores + " " + mem + " " + disk);

        //READ LINE
        str = din.readLine();
        recieved(str);

        //SEND OK
        push("OK");
        dout.flush();

        //READ LINE
        str = din.readLine();
        recieved(str);

        //SEND OK x2
        push("OK");
        dout.flush();

        //READ LINE
        str = din.readLine();
        recieved(str);

        String[] brkn2 = str.split(" "); 
        String type = brkn2[0];
        String serverId = brkn2[1];

        //SEND JOB SCHEDULE
        push("SCHD " + id + " " + type + " " + serverId);

        //READ LINE
        str = din.readLine();
        recieved(str);

        //SEND OK
        push("ok");
        dout.flush();

        //READ LINE
        str = din.readLine();
        recieved(str);

        //QUIT!
        push("QUIT");
        dout.flush();

        str = din.readLine();
        System.out.println("Connection Closed.." + "\u001B[0m");

        dout.close();  
        s.close();  
    }

    static void recieved(String str) {
        System.out.println("RCVD: \'" + str + "\'");
    }

    static void push(String str) throws IOException {
        str = str + "\n";
        dout.write((str).getBytes());
        System.out.println("SENT: " + str);
        dout.flush();
    }

}