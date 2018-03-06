package ns.ics;

import java.io.*;
import java.net.*;
import java.nio.file.*;



/**
 * Sender is a representation of a client in a network. Sender takes in a photo
 * from a command line. Breaks it up into 12 or more pieces and sends them over
 * to a class called Receiver with a port of 13 using DatagramPackets.
 *
 * @author  Laura Humenik, Caleb Crosby
 *
 * @date 2/20/2018
 */



// Client
public class Sender {

    private final static int PORT = 19;
    private static final String HOSTNAME = "localhost";



    public static void main(String[] arg) throws Exception {

        try (DatagramSocket socket = new DatagramSocket(0)) {
            socket.setSoTimeout(100000);

            InetAddress ia = InetAddress.getByName(HOSTNAME);

            // GETS THE FILE FROM A COMMAND LINE
            if ( arg.length > 0 ) {
                File file = new File(arg[0]);
                String stringPath = file.getPath();
                Path path = Paths.get(stringPath);

                // PUTES FILE IN BYTE []
                byte[] fileData = Files.readAllBytes(path);

                //REFERENCE OF HOW MUCH IS LEFT TO SEND FROM FILE/
                int fileSizeLeft = fileData.length;

                System.out
                    .println("The length of the bit array for the file is: " +
                        fileSizeLeft);

              //LARGEST BYTE[] A DATAGRAMPACKET CAN HOLD
               int cutSize = 65000;

                //CHANGES CUTSIZE IF THE FILE IS LESS THAN (6500*12)
                if(fileSizeLeft < (cutSize*12)) {
                    cutSize = fileSizeLeft/12;

                }

                //GETS NUMBER OF CUTS FOR FILE
                int numOfCuts = (fileSizeLeft / cutSize);


                System.out.println(
                    "The length of the bit array for the file is: " + cutSize);

                // Put number of cuts into byte [] and send it
                byte[] numCutBytes = String.valueOf(numOfCuts).getBytes();
                DatagramPacket numCutPacket = new DatagramPacket(numCutBytes,
                    numCutBytes.length, ia, PORT);
                socket.send(numCutPacket);

                DatagramPacket signal =
                    new DatagramPacket(new byte[1024], 1024);
                socket.receive(signal);
                System.out.println("Signal Received!");

                // Put number of cuts into byte [] and send it
                byte[] cutSizeBytes = String.valueOf(cutSize).getBytes();
                DatagramPacket cutSizePacket = new DatagramPacket(cutSizeBytes,
                    cutSizeBytes.length, ia, PORT);
                socket.send(cutSizePacket);

                // RECEIVE SIGNAL TO SEND OVER FILE PACKES.
                socket.receive(signal);
                System.out.println("Signal Received!\n");

                int offset = 0;

                System.out.println("Cut size= " + cutSize);
                while ( fileSizeLeft > 0 ) {
                    System.out.println("File size left = " + fileSizeLeft);

                    if ( fileSizeLeft > cutSize ) {

                        DatagramPacket filePacket = new DatagramPacket(fileData,
                            offset, cutSize, ia, PORT);
                        socket.send(filePacket);

                        int packetNumber = (offset / cutSize) + 1;
                        System.out.print("Packet " + packetNumber + " Sent " +
                            "\n Start byte offset: " + offset +
                            "\n End byte offset: " + (offset + cutSize - 1) +
                            "\n\n");

                        offset = offset + cutSize;
                        fileSizeLeft = fileSizeLeft - cutSize;
                    }

                    else if ( fileSizeLeft < cutSize ) {
                        DatagramPacket filePacket = new DatagramPacket(fileData,
                            offset, fileSizeLeft, ia, PORT);
                        socket.send(filePacket);

                        System.out.print("Packet " + ( (offset / cutSize) + 1) +
                            " Sent " + "\n Start byte offset: " + offset +
                            "\n End byte offset: " + (offset + fileSizeLeft) +
                            "\n\n");

                        fileSizeLeft -= fileSizeLeft;

                    }

                }

                socket.receive(signal);
                System.out.println("Signal Received!");
            }
        } catch ( IOException ex ) {
            ex.printStackTrace();
        }
    }

}
