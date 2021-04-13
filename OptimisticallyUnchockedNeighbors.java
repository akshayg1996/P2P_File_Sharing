import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Set;
import java.util.TimerTask;
import java.util.Vector;

public class OptimisticallyUnchockedNeighbors extends TimerTask {
    @Override
    public void run() {
        peerProcess.updateOtherPeerDetails();
        if (!peerProcess.optimisticUnchokedNeighbors.isEmpty()) {
            peerProcess.optimisticUnchokedNeighbors.clear();
        }

        Set<String> keys = peerProcess.remotePeerDetailsMap.keySet();
        Vector<RemotePeerDetails> remotePeerDetailsVector = new Vector();
        for (String key : keys) {
            RemotePeerDetails remotePeerDetails = peerProcess.remotePeerDetailsMap.get(key);
            if (!key.equals(remotePeerDetails.getId()) && hasPeerHandShaked(remotePeerDetails)) {
                remotePeerDetailsVector.add(remotePeerDetails);
            }
        }

        if(!remotePeerDetailsVector.isEmpty()) {
            Collections.shuffle(remotePeerDetailsVector);
            RemotePeerDetails remotePeerDetails = remotePeerDetailsVector.firstElement();
            remotePeerDetails.setIsOptimisticallyUnchockedNeighbor(1);
            peerProcess.optimisticUnchokedNeighbors.put(remotePeerDetails.getId(), remotePeerDetails);
            logAndShowInConsole(peerProcess.currentPeerID + " has the optimistically unchoked neighbor " + remotePeerDetails.getId());

            if(remotePeerDetails.getIsChoked() == 1) {
                peerProcess.remotePeerDetailsMap.get(remotePeerDetails.getId()).setIsChoked(0);
                sendUnChokedMessage(peerProcess.peerToSocketMap.get(remotePeerDetails.getId()), remotePeerDetails.getId());
                sendHaveMessage(peerProcess.peerToSocketMap.get(remotePeerDetails.getId()), remotePeerDetails.getId());
                peerProcess.remotePeerDetailsMap.get(remotePeerDetails.getId()).setPeerState(3);
            }
        }
    }

    private boolean hasPeerHandShaked(RemotePeerDetails remotePeerDetails) {
        return remotePeerDetails.getIsComplete() == 0 &&
                remotePeerDetails.getIsChoked() == 0 && remotePeerDetails.getIsHandShaked() == 1;
    }

    private void sendUnChokedMessage(Socket socket, String remotePeerID) {
        logAndShowInConsole(peerProcess.currentPeerID + " sending a UNCHOKE message to Peer " + remotePeerID);
        Message message = new Message(MessageConstants.MESSAGE_UNCHOKE);
        byte[] messageInBytes = Message.convertMessageToByteArray(message);
        SendMessageToSocket(socket, messageInBytes);
    }

    private void sendHaveMessage(Socket socket, String peerID) {
        logAndShowInConsole(peerProcess.currentPeerID + " sending HAVE message to Peer " + peerID);
        byte[] bitFieldInBytes = peerProcess.bitFieldMessage.getBytes();
        Message message = new Message(MessageConstants.MESSAGE_HAVE, bitFieldInBytes);
        SendMessageToSocket(socket, Message.convertMessageToByteArray(message));
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
