import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Open an SMTP connection to a mailserver and send one mail.
 *
 */
public class SMTPConnection {
    public static void main(String[] args) throws IOException{
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your email adress");
        String emailFrom = scanner.nextLine();
        System.out.println("Enter reciever email adress");
        String emailTo = scanner.nextLine();
        System.out.println("Enter subject of mail");
        String subject = scanner.nextLine();
        System.out.println("Enter text you want to send. End with \"end\" on a single line");
        ArrayList<String> text = new ArrayList<>();
        text.add(scanner.nextLine());
        while(!text.get(text.size()-1).equals("end")){
            text.add(scanner.nextLine());
        }

        SMTPConnection client = new SMTPConnection();

        client.send(emailFrom, emailTo, text);
        client.close();
    }


    /* The socket to the server */
    private Socket connection;

    /* Streams for reading and writing the socket */
    private BufferedReader fromServer;
    private DataOutputStream toServer;

    private static final int SMTP_PORT = 25;
    private static final String CRLF = "\r\n";

    /* Are we connected? Used in close() to determine what to do. */
    private boolean isConnected = false;

    /* Create an SMTPConnection object. Create the socket and the 
       associated streams. Initialize SMTP connection. */
    public SMTPConnection() throws IOException {
        connection = new Socket("localhost", SMTP_PORT)/* Fill in */;
        fromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()))/* Fill in */;
        toServer =   new DataOutputStream(connection.getOutputStream())/* Fill in */;

        /* Fill in */
	/* Read a line from server and check that the reply code is 220.
	   If not, throw an IOException. */
        /* Fill in */
                        //forsøg
        int reply = parseReply(fromServer.readLine()) ;
        if(!(reply == 220)){
            throw new IOException("Reply is not 220");
        }

	/* SMTP handshake. We need the name of the local machine.
	   Send the appropriate SMTP handshake command. */
        String localhost = "localhost"/* Fill in */;
        sendCommand( /* Fill in */ "HELO "+localhost, 250 );

        isConnected = true;
    }

    /* Send the message. Write the correct SMTP-commands in the
       correct order. No checking for errors, just throw them to the
       caller. */
    public void send(String fromMail, String toMail, ArrayList<String> text) throws IOException {
        /* Fill in */
	/* Send all the necessary commands to send a message. Call
	   sendCommand() to do the dirty work. Do _not_ catch the
	   exception thrown from sendCommand(). */
        /* Fill in */
        sendCommand("MAIL FROM: <" + fromMail + ">", 250);
        sendCommand("RCPT TO: <" + toMail + ">", 250);
        sendCommand("DATA", 354);
        for(String line : text){
            sendCommand(line,0);
        }
        sendCommand(".", 250);//telling that message is done.

    }

    /* Close the connection. First, terminate on SMTP level, then
       close the socket. */
    public void close() {
        isConnected = false;
        try {
            sendCommand("QUIT", 221);
            connection.close();
        } catch (IOException e) {
            System.out.println("Unable to close connection: " + e);
            isConnected = true;
        }
    }

    /* Send an SMTP command to the server. Check that the reply code is
       what is is supposed to be according to RFC 821. */
    private void sendCommand(String command, int rc) throws IOException {
        /* Fill in */
        /* Write command to server and read reply from server. */
        toServer.writeBytes (command+CRLF);
        /* Fill in */

        /* Fill in */
	/* Check that the server's reply code is the same as the parameter
	   rc. If not, throw an IOException. */
	    int replyC = parseReply(fromServer.readLine());
	    if(!(replyC == rc)){
	        throw new IOException("wrong reply code");
        }
        /* Fill in */
    }

    /* Parse the reply line from the server. Returns the reply code. */
    private int parseReply(String reply) {
        /* Fill in */
        String code = reply.substring(0,2);
        return Integer.parseInt(code);
    }

    /* Destructor. Closes the connection if something bad happens. */
    protected void finalize() throws Throwable {
        if(isConnected) {
            close();
        }
        super.finalize();
    }
}