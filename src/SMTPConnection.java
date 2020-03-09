import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Open an SMTP connection to a mailserver and send one mail.
 *
 */
public class SMTPConnection {
    public static void main(String[] args) throws IOException{
        //initializing variables to hold values
        String emailFrom;
        String emailTo;
        String subject;

        //(arraylist so that each line of the text can be send seperately)
        ArrayList<String> textBody = new ArrayList<>();

        //adding picture file
        File file = new File("hackerman.jpg");

        // string that will hold the picture after it has been encoded.
        String fileEncoded = "";

        //encoding the file. it will be the "fileEncoded"-string that will be sent.
        //help from https://stackoverflow.com/questions/36492084/how-to-convert-an-image-to-base64-string-in-java
        FileInputStream fileReader = new FileInputStream(file);
        byte[] bytes = new byte[ (int) file.length()];
        fileReader.read(bytes);
        fileEncoded = Base64.getEncoder().encodeToString(bytes);

        /*
        INPUT:
        Hardcoded email values.
        Could here have used a scanner for input and system.out.println for output to make a textbased UI asking for user input.
         */
        emailFrom = "s171242@student.dtu.com"; /*did not have an assigned TA due to not realizing we had to make groups.
                                                So just added me (s195471) and my brother in group "newcomers" */
        emailTo = "jacob1211@hotmail.com";
        subject = "Hello Jacob im your new TA";
        textBody.add("Hey Jacob");
        textBody.add("You forgot to assign yourself to a group.");

        //open a connection and use the send method to send an email. Afterwards close it.
        SMTPConnection client = new SMTPConnection();
        client.send(emailFrom, emailTo, subject, fileEncoded, textBody);
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
        connection = new Socket("localhost", SMTP_PORT); //using localhost through thinlinc
        fromServer = new BufferedReader(new InputStreamReader(connection.getInputStream())); //setup reader from server
        toServer =   new DataOutputStream(connection.getOutputStream());//setup writer to server


	/* Read a line from server and check that the reply code is 220.
	   If not, throw an IOException. */
        int reply = parseReply(fromServer.readLine()) ;
        if(!(reply == 220)){
            throw new IOException("Reply is not 220");
        }

	/* SMTP handshake. We need the name of the local machine.
	   Send the appropriate SMTP handshake command. */
        String localhost = "localhost";
        sendCommand("HELO "+ localhost, 250 );

        isConnected = true;
    }

    /* Send the message. Write the correct SMTP-commands in the
       correct order. No checking for errors, just throw them to the
       caller. */
    public void send(String fromMail, String toMail, String subject, String fileEncoded, ArrayList<String> text) throws IOException {

	/* Send all the necessary commands to send a message. Call
	   sendCommand() to do the dirty work. Do _not_ catch the
	   exception thrown from sendCommand(). */
        sendCommand("MAIL FROM: <" + fromMail + ">", 250);
        sendCommand("RCPT TO: <" + toMail + ">", 250);
        sendCommand("DATA", 354);
        sendCommand("To: " + toMail, 0);
        sendCommand("From: " + fromMail, 0);
        sendCommand("Subject: " + subject, 0);
        sendCommand("MIME-version: 1.0",0);
        sendCommand("Content-Type: multipart/mixed; boundary=seperator",0);
        sendCommand("",0);
        sendCommand("--seperator",0);
        sendCommand("",0);

        //text body. Sending each line as command.
        for(String line : text){
            sendCommand(line,0);
        }
        sendCommand("",0);

        //specifying the attached picture and its encoding style
        sendCommand("--seperator",0);
        sendCommand("Content-Type:application/octet-stream; name=hackerman.jpg",0);
        sendCommand("Content-Disposition: attachment; filename=hackerman.jpg",0);
        sendCommand("Content-Transfer-Encoding: base64",0);
        sendCommand("",0);
        //actual file in base64 encoding
        sendCommand(fileEncoded,0);

        //Ending message
        sendCommand(".", 250);

    }

    /* Close the connection. First, terminate on SMTP level, then
       close the socket. */
    public void close() {
        isConnected = false;
        try {
            sendCommand("QUIT", 221);//telling to stop connection
            connection.close();
        } catch (IOException e) {
            System.out.println("Unable to close connection: " + e);
            isConnected = true;
        }
    }

    /* Send an SMTP command to the server. Check that the reply code is
       what is is supposed to be according to RFC 821. */
    private void sendCommand(String command, int rc) throws IOException {

        /* Write command to server*/
        toServer.writeBytes (command+CRLF);

	/* Check that the server's reply code is the same as the parameter
	   rc. If not, throw an IOException. */

	if(rc==0){ //used for when we dont expect a reply from server.
	        return;
        }

	    int replyC = parseReply(fromServer.readLine());
	    if(!(replyC == rc)){
	        throw new IOException("wrong reply code");
        }
    }

    /* Parse the reply line from the server. Returns the reply code. */
    private int parseReply(String reply) {
        //splitting the reply by space and going to the first "word" which is always the reply code
        String code = reply.split(" ")[0];
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