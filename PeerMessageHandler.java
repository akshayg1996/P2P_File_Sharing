import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * This class is used to write/read messages from socket
 */
public class PeerMessageHandler implements Runnable {
    //socket from where message are read/written
    private Socket peerSocket = null;
    //The type of connection established
    private int connType;
    //The peerID of the current host
    String ownPeerId;
    //The peerID of the remote host
    String remotePeerId;
    //The input stream of the socket
    private InputStream socketInputStream;
    //The output stream of the socket
    private OutputStream socketOutputStream;
    //The handshake message received
    private HandshakeMessage handshakeMessage;

    /**
     * This constructor initializes the PeerMessage Handler object setting up the required fields
     * @param address - address of the remote host to be connected to
     * @param port - port of the remote host
     * @param connectionType - type of connection established
     * @param serverPeerID - peer ID of the remote host
     */
    public PeerMessageHandler(String address, int port, int connectionType, String serverPeerID) {
        try {
            connType = connectionType;
            ownPeerId = serverPeerID;
            peerSocket = new Socket(address, port);
            socketInputStream = peerSocket.getInputStream();
            socketOutputStream = peerSocket.getOutputStream();
        } catch (IOException e) {
        }
    }

    /**
     * This constructor initializes the PeerMessage Handler object setting up the required fields
     * @param socket - the socket connection created for the remote host
     * @param connectionType - type of connection established
     * @param serverPeerID - peer ID of the remote host
     */
    public PeerMessageHandler(Socket socket, int connectionType, String serverPeerID) {
        try {
            peerSocket = socket;
            connType = connectionType;
            ownPeerId = serverPeerID;
            socketInputStream = peerSocket.getInputStream();
            socketOutputStream = peerSocket.getOutputStream();
        } catch (IOException e) {

        }
    }

    /**
     * This method is used to get the socket instance
     * @return socket
     */
    public Socket getPeerSocket() {
        return peerSocket;
    }

    /**
     * This method is used to set socket instance
     * @param peerSocket - socket to be set
     */
    public void setPeerSocket(Socket peerSocket) {
        this.peerSocket = peerSocket;
    }

    /**
     * This method is used to get the connection type established
     * @return type of connection established
     */
    public int getConnType() {
        return connType;
    }

    /**
     * This method is used to set the connection type established
     * @param connType - connection type to be set
     */
    public void setConnType(int connType) {
        this.connType = connType;
    }

    /**
     * This method is used to get the current host peerID
     * @return current host peerID
     */
    public String getOwnPeerId() {
        return ownPeerId;
    }

    /**
     * This method is used to set the current host peerID
     * @param ownPeerId - current host peerID
     */
    public void setOwnPeerId(String ownPeerId) {
        this.ownPeerId = ownPeerId;
    }

    /**
     * This method is used to get the remote host peerID
     * @return remote host peerID
     */
    public String getRemotePeerId() {
        return remotePeerId;
    }

    /**
     * This method is used to set the remote host peerID
     * @param remotePeerId - remote host peerID
     */
    public void setRemotePeerId(String remotePeerId) {
        this.remotePeerId = remotePeerId;
    }

    /**
     * This method is used to get the socket input stream
     * @return socket input stream
     */
    public InputStream getSocketInputStream() {
        return socketInputStream;
    }

    /**
     * This method is used to set the socket input stream
     * @param socketInputStream - socket input stream
     */
    public void setSocketInputStream(InputStream socketInputStream) {
        this.socketInputStream = socketInputStream;
    }

    /**
     * This method is used to get the socket output stream
     * @return socket output stream
     */
    public OutputStream getSocketOutputStream() {
        return socketOutputStream;
    }

    /**
     * This method is used to set the socket output stream
     * @param socketOutputStream - socket output stream
     */
    public void setSocketOutputStream(OutputStream socketOutputStream) {
        this.socketOutputStream = socketOutputStream;
    }

    /**
     * This method is used to get the handshake message
     * @return handshake message
     */
    public HandshakeMessage getHandshakeMessage() {
        return handshakeMessage;
    }

    /**
     * This method is used to set the handshake message
     * @param handshakeMessage - handshake message
     */
    public void setHandshakeMessage(HandshakeMessage handshakeMessage) {
        this.handshakeMessage = handshakeMessage;
    }

    /**
     * This method is run everytime PeerMessageHandler thread is started.
     * It supports 2 types of connection - Active Connection : It performs initial handshake and bitfield messages sending to socket
     * Passive Connection : It reads messages from socket and adds them to message queue.
     */
    @Override
    public void run() {
        byte[] handShakeMessageInBytes = new byte[32];
        byte[] dataBufferWithoutPayload = new byte[MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE];
        byte[] messageLengthInBytes;
        byte[] messageTypeInBytes;
        MessageDetails messageDetails = new MessageDetails();
        try {
            //Initial connection of file receivers. Sending handshake and bitfield message
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
                    }
                }

                // Sending BitField...
                Message d = new Message(MessageConstants.MESSAGE_BITFIELD, peerProcess.bitFieldMessage.getBytes());
                byte[] b = Message.convertMessageToByteArray(d);
                socketOutputStream.write(b);
                peerProcess.remotePeerDetailsMap.get(remotePeerId).setPeerState(8);
            }

            //This type is used to send and receive messages and add received messages to message queue
            else {
                while (true) {
                    socketInputStream.read(handShakeMessageInBytes);
                    handshakeMessage = HandshakeMessage.convertBytesToHandshakeMessage(handShakeMessageInBytes);
                    if (handshakeMessage.getHeader().equals(MessageConstants.HANDSHAKE_HEADER)) {
                        remotePeerId = handshakeMessage.getPeerID();
                        logAndShowInConsole(ownPeerId + " is connected from Peer " + remotePeerId);
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
                } else if (messageType.equals(MessageConstants.MESSAGE_DOWNLOADED)) {
                    messageDetails.setMessage(message);
                    messageDetails.setFromPeerID(remotePeerId);
                    int peerState = peerProcess.remotePeerDetailsMap.get(remotePeerId).getPeerState();
                    peerProcess.remotePeerDetailsMap.get(remotePeerId).setPreviousPeerState(peerState);
                    peerProcess.remotePeerDetailsMap.get(remotePeerId).setPeerState(15);
                    MessageQueue.addMessageToMessageQueue(messageDetails);
                } else {
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
                    dataBuffWithPayload = null;
                    dataBuffPayload = null;
                    bytesAlreadyRead = 0;
                    bytesRead = 0;
                }
            }

        } catch (Exception e) {
        }
    }

    /**
     * This method sends handshake message to socket and determines if the message is sent successfully
     * @return true - message sent successfully; false - message not sent successfully
     */
    public boolean handShakeMessageSent() {
        boolean messageSent = false;
        try {
            HandshakeMessage handshakeMessage = new HandshakeMessage(MessageConstants.HANDSHAKE_HEADER, this.ownPeerId);
            socketOutputStream.write(HandshakeMessage.convertHandshakeMessageToBytes(handshakeMessage));
            messageSent = true;
        } catch (IOException e) {
        }
        return messageSent;
    }

    /**
     * This method is used to log a message in a log file and show it in console
     * @param message - message to be logged and showed in console
     */
    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }
}
