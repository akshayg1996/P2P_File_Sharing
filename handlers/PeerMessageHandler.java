package handlers;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import static utils.LogFormatter.getFormattedMessage;
import static utils.LogHelper.logAndShowInConsole;

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
            logAndShowInConsole(ownPeerID + " RemotePeerHandler : " + e.getMessage());
        }
        catch (IOException e)
        {
            logAndShowInConsole(ownPeerID + " RemotePeerHandler : " + e.getMessage());
        }
        this.connType = connType;

        try
        {
//            in = peerSocket.getInputStream();
//            out = peerSocket.getOutputStream();
        }
        catch (Exception ex)
        {
            logAndShowInConsole(ownPeerID + " RemotePeerHandler : " + ex.getMessage());
        }
    }


    @Override
    public void run() {

    }
}
