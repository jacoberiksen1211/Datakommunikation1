import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import java.util.Base64;


/**
 * Open an SMTP connection to a mailserver and send one mail.
 *
 */
public class mailClient1 {
    /* The socket to the server */
    private Socket connection;
    // Picture objects
    BufferedImage bufferimage;
    ByteArrayOutputStream output;
    byte[] bytes;
    String base64;


    /* Streams for reading and writing the socket */
    private BufferedReader fromServer;

    private DataOutputStream toServer;
    private static final int SMTP_PORT = 25;
    private static final String CRLF = "\r\n";

    /* Are we connected? Used in close() to determine what to do. */
    private boolean isConnected = false;

    /* Create an SMTPConnection object. Create the socket and the
       associated streams. */
    public mailClient1() throws IOException {

        // Converts picture to Base64 string
        bufferimage = ImageIO.read(new File("FILE PATH!!!"));
        output = new ByteArrayOutputStream();
        // file type
        ImageIO.write(bufferimage, "png", output );
        bytes = output.toByteArray();

        base64 = Base64.getEncoder().encodeToString(bytes);
        System.out.println(base64);
        // Initialize SMTP connection.
        connection = new Socket("localhost", SMTP_PORT);
        fromServer = new BufferedReader((new InputStreamReader(connection.getInputStream())));
        toServer =   new DataOutputStream(connection.getOutputStream());


	/* Read a line from server and check that the reply code is 220.
	   If not, throw an IOException. */

        if(!(parseReply(fromServer.readLine())==220)){
            throw new IOException();
        }


	/* SMTP handshake. We need the name of the local machine.
	   Send the appropriate SMTP handshake command. */
        String localhost ="localhost";
        sendCommand("HELO "+localhost,250);

        isConnected = true;
    }

    /* Send the message. Write the correct SMTP-commands in the
       correct order. No checking for errors, just throw them to the
       caller. */
    public void send() throws IOException {

        sendCommand("MAIL FROM: FROM MAIL HERE!!!",250);

	/* Send all the necessary commands to send a message. Call
	   sendCommand() to do the dirty work. Do _not_ catch the
	   exception thrown from sendCommand(). */

        // After a lot of research on internet I was only able to send pictures with these line
        sendCommand("RCPT TO: TO MAIL HERE!!!",250);
        sendCommand("DATA",354);
        sendCommand("Subject:s180103",0);
        sendCommand("MIME-Version: 1.0",0);
        sendCommand("Content-Type:multipart/mixed;boundary=\"KkK170891tpbkKk__FV_KKKkkkjjwq\"",0);
        sendCommand("--KkK170891tpbkKk__FV_KKKkkkjjwq",0);
        // Filename and type to be sent
        sendCommand("Content-Type:application/octet-stream;name=\"s180103.png\"",0);
        sendCommand("Content-Transfer-Encoding:base64",0);
        sendCommand("Content-Disposition:attachment;filename=\"s180103.png\"",0);
        sendCommand("",0);
        // Base64 string (The picture)
        sendCommand(base64,0);

        sendCommand("",0);
        sendCommand("",0);
        sendCommand("--KkK170891tpbkKk__FV_KKKkkkjjwq",0);
        sendCommand(" ",0);
        sendCommand(" ",0);
        // Text message
        sendCommand("TEXT HERE!!!",0);
        sendCommand("TEXT HERE!!!",0);
        sendCommand("",0);
        sendCommand(".",250);


    }

    /* Close the connection. First, terminate on SMTP level, then
       close the socket. */
    public void close() {
        isConnected = false;

        try {
            sendCommand("QUIT",221);
            connection.close();
        } catch (IOException e) {
            System.out.println("Unable to close connection: " + e);
            isConnected = true;
        }
    }

    /* Send an SMTP command to the server. Check that the reply code is
       what is is supposed to be according to RFC 821. */
    private void sendCommand(String command, int rc) throws IOException {

        /* Write command to server and read reply from server. */

        toServer.writeBytes(command+CRLF);

        if (rc==0)
            return;

	/* Check that the server's reply code is the same as the parameter
	   rc. If not, throw an IOException. */

        if(!(parseReply(fromServer.readLine())==rc)){
            throw new IOException();
        }
    }

    /* Parse the reply line from the server. Returns the reply code. */
    private int parseReply(String reply) {
        String temp = reply.substring(0,3);
        int tempint = Integer.parseInt(temp);
        return tempint;
    }

    /* Destructor. Closes the connection if something bad happens. */

    protected void finalize() throws Throwable {
        if(isConnected) {
            close();
        }
        super.finalize();
    }

    public static void main(String[] args) {
        try {
            mailClient1 test = new mailClient1();
            test.send();
            test.close();
        }catch (IOException e){
            System.out.println("noget gik galt");
        }
    }
}
