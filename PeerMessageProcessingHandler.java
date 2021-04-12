import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class PeerMessageProcessingHandler implements Runnable {

    private static String currentPeerID;

    public PeerMessageProcessingHandler(String peerID) {
        currentPeerID = peerID;
    }

    public PeerMessageProcessingHandler() {
        currentPeerID = null;
    }

    @Override
    public void run() {
        MessageDetails messageDetails = MessageQueue.getMessageFromQueue();
        while (messageDetails == null) {
            Thread.currentThread();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logAndShowInConsole(currentPeerID + " ERROR occured while thread is interrupted  : " + e.getMessage());
                e.printStackTrace();
            }
            messageDetails = MessageQueue.getMessageFromQueue();
        }

        String messageType = messageDetails.getMessage().getType();
        String remotePeerID = messageDetails.getFromPeerID();
        int peerState = peerProcess.remotePeerDetailsMap.get(remotePeerID).getPeerState();

        if (messageType.equals(MessageConstants.MESSAGE_HAVE) && peerState != 14) {
            logAndShowInConsole(currentPeerID + " received HAVE message from Peer " + remotePeerID);
            if (isPeerInterested(messageDetails.getMessage(), remotePeerID)) {
                sendInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(9);
            } else {
                sendNotInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(13);
            }
        } else {
            switch (peerState) {
                case 2:
                    if (messageType.equals(MessageConstants.MESSAGE_BITFIELD)) {
                        logAndShowInConsole(currentPeerID + " received a BITFIELD message from Peer " + remotePeerID);
                        sendBitFieldMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(3);
                    }
                    break;

                case 3:
                    if(messageType.equals(MessageConstants.MESSAGE_INTERESTED)) {
                        logAndShowInConsole(currentPeerID + " receieved an INTERESTED message from Peer " + remotePeerID);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsInterested(1);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsHandShaked(1);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(4);
                    }else if(messageType.equals(MessageConstants.MESSAGE_NOT_INTERESTED)) {
                        logAndShowInConsole(currentPeerID + " receieved an NOT INTERESTED message from Peer " + remotePeerID);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsInterested(0);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsHandShaked(1);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(5);
                    }
                    break;
                case 8:
                   if(messageType.equals(MessageConstants.MESSAGE_BITFIELD)) {
                       if(isPeerInterested(messageDetails.getMessage(), remotePeerID)) {
                           sendInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                           peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(9);
                       }else{
                           sendNotInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                           peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(13);
                       }
                   }
                   break;
                case 14:
                    if(messageType.equals(MessageConstants.MESSAGE_HAVE)) {
                        if(isPeerInterested(messageDetails.getMessage(), remotePeerID)) {
                            sendInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(9);
                        }else{
                            sendNotInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(13);
                        }
                    }
                    break;
            }
        }
    }

    private void sendNotInterestedMessage(Socket socket, String remotePeerID) {
        logAndShowInConsole(currentPeerID + " sending a NOT INTERESTED message to Peer " + remotePeerID);
        Message message = new Message(MessageConstants.MESSAGE_NOT_INTERESTED);
        byte[] messageInBytes = Message.convertMessageToByteArray(message);
        SendMessageToSocket(socket, messageInBytes);
    }

    private void sendInterestedMessage(Socket socket, String remotePeerID) {
        logAndShowInConsole(currentPeerID + " sending an INTERESTED message to Peer " + remotePeerID);
        Message message = new Message(MessageConstants.MESSAGE_INTERESTED);
        byte[] messageInBytes = Message.convertMessageToByteArray(message);
        SendMessageToSocket(socket, messageInBytes);
    }

    private void sendBitFieldMessage(Socket socket, String remotePeerID) {
        logAndShowInConsole(currentPeerID + " sending a BITFIELD message to Peer " + remotePeerID);
        byte[] bitFieldMessageInByteArray = peerProcess.bitFieldMessage.getBytes();
        Message message = new Message(MessageConstants.MESSAGE_BITFIELD, bitFieldMessageInByteArray);
        byte[] messageInBytes = Message.convertMessageToByteArray(message);
        SendMessageToSocket(socket, messageInBytes);
    }

    private boolean isPeerInterested(Message message, String remotePeerID) {
        BitFieldMessage bitField = BitFieldMessage.decodeMessage(message.getPayload());
        peerProcess.remotePeerDetailsMap.get(remotePeerID).setBitFieldMessage(bitField);

        if (peerProcess.bitFieldMessage.containsInterestingPieces(bitField))
            return true;
        return false;
    }

    private void SendMessageToSocket(Socket socket, byte[] messageInBytes) {
        try {
            OutputStream out = socket.getOutputStream();
            out.write(messageInBytes);
        } catch (IOException e) {
            logAndShowInConsole("Error occured - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }
}
