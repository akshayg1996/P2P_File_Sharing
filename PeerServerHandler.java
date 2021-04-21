import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This method is used to handle the File server thread
 */
public class PeerServerHandler implements Runnable {
    //The File server socket
    private ServerSocket serverSocket;
    //Current PeerID
    private String peerID;
    //Socket of remote peer
    private Socket otherPeerSocket;
    //Remote peer thread
    private Thread otherPeerThread;

    /**
     * Constructor to initialize File Server class with server socket and peerID of current peer
     * @param serverSocket - socket of File server
     * @param peerID - peerID of the current peer
     */
    public PeerServerHandler(ServerSocket serverSocket, String peerID) {
        this.serverSocket = serverSocket;
        this.peerID = peerID;
    }

    /**
     * This method is run everytime FileServer Thread starts. It accepts incoming socket connections
     * and starts threads to process messages.
     */
    @Override
    public void run() {
        while(true) {
            try{
                //accept incoming socket connections
                otherPeerSocket = serverSocket.accept();
                //start a thread to handle incoming messages
                otherPeerThread = new Thread(new PeerMessageHandler(otherPeerSocket, 0, peerID));
                peerProcess.serverThreads.add(otherPeerThread);
                otherPeerThread.start();
            }catch (IOException e) {

            }
        }
    }

    /**
     * This method is used to log a message in a log file and show it in console
     * @param message - message to be logged and showed in console
     */
    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }
}
