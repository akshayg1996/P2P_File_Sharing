import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerMessageHandler implements Runnable
{
    private Socket peerSocket = null;
    private int connType;
    String ownPeerId, remotePeerId;

    public PeerMessageHandler(String add, int port, int connType, String ownPeerID)
    {
        try
        {
            this.connType = connType;
            this.ownPeerId = ownPeerID;
            this.peerSocket = new Socket(add, port);
        }
        catch (UnknownHostException e)
        {
            LogHelper.logAndShowInConsole(ownPeerID + " RemotePeerHandler : " + e.getMessage());
        }
        catch (IOException e)
        {
            LogHelper.logAndShowInConsole(ownPeerID + " RemotePeerHandler : " + e.getMessage());
        }
        this.connType = connType;

        try
        {
//            in = peerSocket.getInputStream();
//            out = peerSocket.getOutputStream();
        }
        catch (Exception ex)
        {
            LogHelper.logAndShowInConsole(ownPeerID + " RemotePeerHandler : " + ex.getMessage());
        }
    }


    @Override
    public void run() {

    }
}
