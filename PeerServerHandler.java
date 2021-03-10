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

    }
}
