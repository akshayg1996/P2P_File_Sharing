import java.util.LinkedList;
import java.util.Queue;

/**
 * This class creates message queue which is used to process messages received from socket
 */
public class MessageQueue {

    /**
     * Queue to process messages from socket
     */
    public static Queue<MessageDetails> messageDetailsQueue = new LinkedList();

    /**
     * This method is used to add a message to message queue
     * @param messageDetails - message to be added
     */
    public static synchronized void addMessageToMessageQueue(MessageDetails messageDetails)
    {
        messageDetailsQueue.add(messageDetails);
    }

    /**
     * This method is used to get message from message queue
     * @return message added in the queue
     */
    public static synchronized MessageDetails getMessageFromQueue() {
        return  !messageDetailsQueue.isEmpty() ? messageDetailsQueue.remove() : null;
    }
}
