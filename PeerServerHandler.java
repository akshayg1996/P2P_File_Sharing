import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerServerHandler implements Runnable {

    private ServerSocket serverSocket;
    private String peerID;
    private Socket otherPeerSocket;
    private Thread otherPeerThread;

    public PeerServerHandler(ServerSocket serverSocket, String peerID) {
        this.serverSocket = serverSocket;
        this.peerID = peerID;
    }

    @Override
    public void run() {
        while(true) {
            try{
                otherPeerSocket = serverSocket.accept();
                otherPeerThread = new Thread(new PeerMessageHandler(otherPeerSocket, 0, peerID));
                logAndShowInConsole(peerID + " Connection is Established");
                P2PProcess.serverThreads.add(otherPeerThread);
                otherPeerThread.start();
            }catch (IOException e) {
                logAndShowInConsole(peerID + " Error Occured while Accepting Socket Connection - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }
}
