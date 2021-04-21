/**
 * This class contains configuration to be set for a peer
 */
public class CommonConfiguration {
    //Number of preferred neighbors of a peer
    public static int numberOfPreferredNeighbours;
    //The interval to determine preferred neighbors
    public static int unchockingInterval;
    //The interval to determine optimistically unchoked neighbors
    public static int optimisticUnchokingInterval;
    //Name of the file to be transferred
    public static String fileName;
    //The size of the file to be transferred
    public static int fileSize;
    //The size of each piece the file needs to be divided into
    public static int pieceSize;
}
