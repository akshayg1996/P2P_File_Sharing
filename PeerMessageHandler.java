import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerMessageHandler implements Runnable {
    private Socket peerSocket = null;
    private int connType;
    String ownPeerId, remotePeerId;
    private InputStream socketInputStream;
    private OutputStream socketOutputStream;
    private HandshakeMessage handshakeMessage;

    public PeerMessageHandler(String add, int port, int connType, String ownPeerID) {
        try {
            this.connType = connType;
            this.ownPeerId = ownPeerID;
            this.peerSocket = new Socket(add, port);
        } catch (UnknownHostException e) {
            LogHelper.logAndShowInConsole(ownPeerID + " RemotePeerHandler : " + e.getMessage());
        } catch (IOException e) {
            LogHelper.logAndShowInConsole(ownPeerID + " RemotePeerHandler : " + e.getMessage());
        }
        this.connType = connType;

        try {
//            in = peerSocket.getInputStream();
//            out = peerSocket.getOutputStream();
        } catch (Exception ex) {
            LogHelper.logAndShowInConsole(ownPeerID + " RemotePeerHandler : " + ex.getMessage());
        }
    }

    public PeerMessageHandler(Socket socket, int connectionType, String serverPeerID) {
        peerSocket = socket;
        connType = connectionType;
        ownPeerId = serverPeerID;

        try {
            socketInputStream = peerSocket.getInputStream();
            socketOutputStream = peerSocket.getOutputStream();
        } catch (IOException e) {
            logAndShowInConsole(ownPeerId + " Error Occured while Opening socket input and output streams - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Socket getPeerSocket() {
        return peerSocket;
    }

    public void setPeerSocket(Socket peerSocket) {
        this.peerSocket = peerSocket;
    }

    public int getConnType() {
        return connType;
    }

    public void setConnType(int connType) {
        this.connType = connType;
    }

    public String getOwnPeerId() {
        return ownPeerId;
    }

    public void setOwnPeerId(String ownPeerId) {
        this.ownPeerId = ownPeerId;
    }

    public String getRemotePeerId() {
        return remotePeerId;
    }

    public void setRemotePeerId(String remotePeerId) {
        this.remotePeerId = remotePeerId;
    }

    public InputStream getSocketInputStream() {
        return socketInputStream;
    }

    public void setSocketInputStream(InputStream socketInputStream) {
        this.socketInputStream = socketInputStream;
    }

    public OutputStream getSocketOutputStream() {
        return socketOutputStream;
    }

    public void setSocketOutputStream(OutputStream socketOutputStream) {
        this.socketOutputStream = socketOutputStream;
    }

    public HandshakeMessage getHandshakeMessage() {
        return handshakeMessage;
    }

    public void setHandshakeMessage(HandshakeMessage handshakeMessage) {
        this.handshakeMessage = handshakeMessage;
    }

    @Override
    public void run() {
        byte[] handShakeMessageInBytes = new byte[32];
        byte[] dataBufferWithoutPayload = new byte[MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE];
        byte[] messageLengthInBytes;
        byte[] messageType;
        MessageDetails messageDetails = new MessageDetails();
        try {
            if (connType == MessageConstants.ACTIVE_CONNECTION) {
                if (handShakeMessageSent()) {
                    logAndShowInConsole(ownPeerId + " HANDSHAKE has been sent");
                } else {
                    logAndShowInConsole(ownPeerId + " HANDSHAKE sending failed");
                }

                while (true) {
                    socketInputStream.read(handShakeMessageInBytes);
                    handshakeMessage = HandshakeMessage.convertBytesToHandshakeMessage(handShakeMessageInBytes);

                }
            }
        } catch (Exception e) {
            logAndShowInConsole(ownPeerId + " Error occured while running process - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean handShakeMessageSent() {
        boolean messageSent = false;
        try {
            HandshakeMessage handshakeMessage = new HandshakeMessage(MessageConstants.HANDSHAKE_HEADER, this.ownPeerId);
            socketOutputStream.write(HandshakeMessage.convertHandshakeMessageToBytes(handshakeMessage));
            messageSent = true;
        } catch (IOException e) {
            logAndShowInConsole(ownPeerId + " Error Occured while sending handshake message - " + e.getMessage());
            e.printStackTrace();
        }
        return messageSent;
    }

    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }
}
