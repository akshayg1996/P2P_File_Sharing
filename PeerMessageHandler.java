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

    public PeerMessageHandler(String address, int port, int connectionType, String serverPeerID) {
        try {
            this.connType = connectionType;
            this.ownPeerId = serverPeerID;
            this.peerSocket = new Socket(address, port);
            System.out.println("PeerID: "+ ownPeerId  +" Socket created for address: " + address + " port: " + port);
        } catch (UnknownHostException e) {
            LogHelper.logAndShowInConsole(serverPeerID + " RemotePeerHandler : " + e.getMessage());
        } catch (IOException e) {
            LogHelper.logAndShowInConsole(serverPeerID + " RemotePeerHandler : " + e.getMessage());
        }
        this.connType = connType;

        try {
            socketInputStream = peerSocket.getInputStream();
            socketOutputStream = peerSocket.getOutputStream();
        } catch (Exception ex) {
            LogHelper.logAndShowInConsole(serverPeerID + " RemotePeerHandler : " + ex.getMessage());
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
        byte[] messageTypeInBytes;
        MessageDetails messageDetails = new MessageDetails();
        try {
            if (connType == MessageConstants.ACTIVE_CONNECTION) {
                if (handShakeMessageSent()) {
                    logAndShowInConsole(ownPeerId + " HANDSHAKE has been sent");
                } else {
                    logAndShowInConsole(ownPeerId + " HANDSHAKE sending failed");
                    System.exit(0);
                }

                while (true) {
                    socketInputStream.read(handShakeMessageInBytes);
                    handshakeMessage = HandshakeMessage.convertBytesToHandshakeMessage(handShakeMessageInBytes);
                    if (handshakeMessage.getHeader().equals(MessageConstants.HANDSHAKE_HEADER)) {
                        remotePeerId = handshakeMessage.getPeerID();
                        logAndShowInConsole(ownPeerId + " makes a connection to Peer " + remotePeerId);
                        logAndShowInConsole(ownPeerId + " Received a HANDSHAKE message from Peer " + remotePeerId);
                        //populate peerID to socket mapping
                        peerProcess.peerToSocketMap.put(remotePeerId, this.peerSocket);
                        break;
                    } else
                        continue;
                }

                // Sending BitField...
                Message d = new Message(MessageConstants.MESSAGE_BITFIELD, peerProcess.bitFieldMessage.getBytes());
                byte[] b = Message.convertMessageToByteArray(d);
                socketOutputStream.write(b);
                peerProcess.remotePeerDetailsMap.get(remotePeerId).setPeerState(8);
            }

            //Passive connection
            else {
                while (true) {
                    socketInputStream.read(handShakeMessageInBytes);
                    handshakeMessage = HandshakeMessage.convertBytesToHandshakeMessage(handShakeMessageInBytes);
                    if (handshakeMessage.getHeader().equals(MessageConstants.HANDSHAKE_HEADER)) {
                        remotePeerId = handshakeMessage.getPeerID();

                        logAndShowInConsole(ownPeerId + " makes a connection to Peer " + remotePeerId);
                        logAndShowInConsole(ownPeerId + " Received a HANDSHAKE message from Peer " + remotePeerId);

                        //populate peerID to socket mapping
                        peerProcess.peerToSocketMap.put(remotePeerId, this.peerSocket);
                        break;
                    } else {
                        continue;
                    }
                }
                if (handShakeMessageSent()) {
                    logAndShowInConsole(ownPeerId + " HANDSHAKE message has been sent successfully.");

                } else {
                    logAndShowInConsole(ownPeerId + " HANDSHAKE message sending failed.");
                    System.exit(0);
                }

                peerProcess.remotePeerDetailsMap.get(remotePeerId).setPeerState(2);
            }

            while (true) {
                int headerBytes = socketInputStream.read(dataBufferWithoutPayload);
                if (headerBytes == -1)
                    break;
                messageLengthInBytes = new byte[MessageConstants.MESSAGE_LENGTH];
                messageTypeInBytes = new byte[MessageConstants.MESSAGE_TYPE];
                System.arraycopy(dataBufferWithoutPayload, 0, messageLengthInBytes, 0, MessageConstants.MESSAGE_LENGTH);
                System.arraycopy(dataBufferWithoutPayload, MessageConstants.MESSAGE_LENGTH, messageTypeInBytes, 0, MessageConstants.MESSAGE_TYPE);
                Message message = new Message();
                message.setMessageLength(messageLengthInBytes);
                message.setMessageType(messageTypeInBytes);
                String messageType = message.getType();
                if (messageType.equals(MessageConstants.MESSAGE_INTERESTED) || messageType.equals(MessageConstants.MESSAGE_NOT_INTERESTED) ||
                        messageType.equals(MessageConstants.MESSAGE_CHOKE) || messageType.equals(MessageConstants.MESSAGE_UNCHOKE)) {
                    messageDetails.setMessage(message);
                    messageDetails.setFromPeerID(remotePeerId);
                    MessageQueue.addMessageToMessageQueue(messageDetails);
                }
                else if (messageType.equals(MessageConstants.MESSAGE_DOWNLOADED)) {
                    messageDetails.setMessage(message);
                    messageDetails.setFromPeerID(remotePeerId);
                    int peerState = peerProcess.remotePeerDetailsMap.get(remotePeerId).getPeerState();
                    peerProcess.remotePeerDetailsMap.get(remotePeerId).setPreviousPeerState(peerState);
                    peerProcess.remotePeerDetailsMap.get(remotePeerId).setPeerState(15);
                    MessageQueue.addMessageToMessageQueue(messageDetails);
                }
                else {
                    int bytesAlreadyRead = 0;
                    int bytesRead;
                    byte[] dataBuffPayload = new byte[message.getMessageLengthAsInteger() - 1];
                    while (bytesAlreadyRead < message.getMessageLengthAsInteger() - 1) {
                        bytesRead = socketInputStream.read(dataBuffPayload, bytesAlreadyRead, message.getMessageLengthAsInteger() - 1 - bytesAlreadyRead);
                        if (bytesRead == -1)
                            return;
                        bytesAlreadyRead += bytesRead;
                    }

                    byte[] dataBuffWithPayload = new byte[message.getMessageLengthAsInteger() + MessageConstants.MESSAGE_LENGTH];
                    System.arraycopy(dataBufferWithoutPayload, 0, dataBuffWithPayload, 0, MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE);
                    System.arraycopy(dataBuffPayload, 0, dataBuffWithPayload, MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE, dataBuffPayload.length);

                    Message dataMsgWithPayload = Message.convertByteArrayToMessage(dataBuffWithPayload);
                    messageDetails.setMessage(dataMsgWithPayload);
                    messageDetails.setFromPeerID(remotePeerId);
                    MessageQueue.addMessageToMessageQueue(messageDetails);
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
