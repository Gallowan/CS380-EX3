/**
 * CS 380.01 - Computer Networks
 * Professor: NDavarpanah
 *
 * Exercise 3
 * Ex3Client
 *
 * Justin Galloway
 */

import java.io.*;
import java.util.*;
import java.net.*;

public class Ex3Client {
    // Initialize ArrayLists for bytes and buffering
    private static ArrayList<Integer> byteList = new ArrayList<>();
    private static ArrayList<Integer> buffer = new ArrayList<>();
    
    public static void main(String[] args) {
        try {
            // Server Connection
            Socket s = new Socket("18.221.102.182", 38103);
            System.out.println("Connected to server.");

            // Initialize inputstream and use to determine bytes to read in
            InputStream is = s.getInputStream();
            int byteCount = is.read();
            System.out.println("Reading " + byteCount + " bytes.");

            // Read bytes
            System.out.println("Data received:");
            int col = 0;
            System.out.print("   "); // For formatting

            while (true) {
                if (byteCount == 0) {
                    break;
                }
                // Read in
                int bytes = is.read();
                System.out.print(formatBytes(bytes));
                byteList.add(bytes);
                col++;

                // Cut to new line at 20 characters
                if (col % 10 == 0)
                    System.out.print("\n   ");
                byteCount--;
            }
            
            System.out.println(); // For formatting
            // Byte formatting
            bitTransfer();
            byte[] checkSum = new byte[2];

            int sum = (int) checkSum();
            System.out.println("Checksum calculated: 0x" + Integer.toHexString(sum & 0xFFFF) +".");

            // Organize and send info back to server.
            checkSum[0] = (byte) ((sum>>8)&0xFF);
            checkSum[1] = (byte) (sum&0xFF);
            PrintStream ps = new PrintStream(s.getOutputStream(), true);
            ps.write(checkSum, 0, checkSum.length);
            byte ack = (byte) is.read();
            if (ack == 1)
                System.out.println("Response good.");
            else
                System.out.println("Response bad!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Accesses byteList and changes 2 bytes into 16 bits, then adds to buffer
    public static void bitTransfer() {
        int size = byteList.size();
        int byteSpace = 0;

        while (size > 1) {
            int space1 = byteList.get(byteSpace);
            //Shift bit pattern to left
            space1 = space1 << 8;
            int space2 = byteList.get(byteSpace + 1);
            int bitOr = space1 | space2;
            // Add to buffer list
            buffer.add(bitOr);
            // Shift space and size accordingly
            byteSpace += 2;
            size -= 2;
        }

        // Return to buffer if it is an odd space
        if (size > 0) {
            int oddCheck = byteList.get(byteList.size() - 1);
            oddCheck = oddCheck << 8;
            buffer.add(oddCheck);
        }
    }

        // Format the bytes properly to output into console to user
    public static String formatBytes(int bits) {
        String input = Integer.toHexString(bits & 0xFF);
        //int size =(input.length() == 1) ? return "0" + input;
        int size = input.length();
        if (size == 1) {
            return "0" + input;
        }
        return input;
    }

    // Checksum algorithm for IPv4
    public static long checkSum() {
        int length = buffer.size();
        int index = 0;
        long sum = 0;

        // While the length of the buffer isn't zero, append the sum at the
        // location of the index
        while (length > 0) {
            sum += buffer.get(index);
            if ((sum & 0xFFFF0000) > 0) {
                // Carry occurred
                sum = sum & 0xFFFF;
                sum++;
            }
            index++;
            length--;
        }
        sum = ~sum;
        sum = sum & 0xFFFF;
        return sum;
    }
}