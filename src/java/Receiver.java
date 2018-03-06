import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

/**
 * Receiver is a representation of a server in a network. Receiver takes in a series
 * of DatagramPackets that is pieces of a photo, puts them into a DatagramPacket Array
 * and pieces them back together.
 *
 * @author  Laura Humenik, Caleb Crosby
 *
 * @date 2/20/2018
 */





// Server.

public class Receiver {

    private final static int PORT = 19;
    private final static Logger errors = Logger.getLogger("errors");

    public static void main(String a[]) throws Exception {

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            while ( true ) {
                try {
                    // git example
                    System.out.println("git example");
                    // to receive num of cuts
                    DatagramPacket numOfCutsPacket =
                        new DatagramPacket(new byte[1024], 1024);

                    // receives number Of cuts
                    socket.receive(numOfCutsPacket);

                    // saves number of cuts
                    String str = new String(numOfCutsPacket.getData(), 0,
                        numOfCutsPacket.getLength(), "US-ASCII");

                    int cutNum = Integer.valueOf(str);
                    System.out
                        .println("The new array should be " + (cutNum + 1));

                    // Create an array to collect all cuts
                    List<DatagramPacket> dataGPacketArray =
                        new ArrayList<>(cutNum + 1);

                    // SEND SIGNAL TO SEND OVER THE CUTSIZES OF FILES.
                    DatagramPacket signal = new DatagramPacket(new byte[1], 1,
                        numOfCutsPacket.getAddress(),
                        numOfCutsPacket.getPort());
                    socket.send(signal);
                    System.out.println("Signal Sent.");

                    // RECEIVE CUTSIZE
                    DatagramPacket cutSizePacket =
                        new DatagramPacket(new byte[1024], 1024);

                    // receives number Of cuts
                    socket.receive(cutSizePacket);

                    // SAVE SIZE OF CUTS
                    str = new String(cutSizePacket.getData(), 0,
                        cutSizePacket.getLength(), "US-ASCII");
                    int cutSize = Integer.parseInt(str.trim());
                    System.out.println("The cut size should be " + (cutSize));
                    int endOffset = cutSize;

                    // SEND SIGNAL TO SEND OVER PACKETS!!
                    socket.send(signal);
                    System.out.println("Signal Sent.\n");

                    // Receive Multiple Files.
                    cutNum = cutNum + 1;
                    int startOffset = 0;
                    int packetNumber = 1;
                    while ( cutNum > 0 ) {

                        DatagramPacket fileReceived = new DatagramPacket(
                            new byte[cutSize], cutSize);

                        socket.receive(fileReceived);
                        dataGPacketArray.add(fileReceived);
                        System.out.println("Packet " + packetNumber +
                            " Received" + "\n Start byte offset: " + startOffset +
                            "\n End byte offset: " + (endOffset - 1) + "\n\n");

                        cutNum-- ;
                        startOffset += cutSize;
                        endOffset += cutSize;
                        packetNumber++ ;

                    }

                    // SEND SIGNAL TO LET SERVER KNOW ALL PACKETS HAVE BEEN RECIEVED
                    socket.send(signal);
                    System.out.println("Signal Sent.");

                    // CREATE OUTPUT FILE USING DATA INSIDE THE DATAGRAM PACKETS THAT ARE IN THE
                    // DATAGPACKETARRAY
                    File outputFile = new File(
                        "C:\\Users\\Rorst\\Desktop\\GP1.Attempt1\\src\\java\\outputPhoto.jpg");
                    FileOutputStream fos = new FileOutputStream(outputFile);
                    for ( int i = 0; i < dataGPacketArray.size(); i++ ) {
                        fos.write(dataGPacketArray.get(i).getData());
                    }

                    fos.close();
                    System.exit(0);

                } catch ( IOException | RuntimeException ex ) {
                    errors.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        } catch ( IOException ex ) {
            errors.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

}