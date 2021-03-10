import java.util.LinkedList;
import java.util.Queue;

public class MessageQueue {

    public static Queue<MessageDetails> messageDetailsQueue = new LinkedList();

    public static synchronized void addMessageToMessageQueue(MessageDetails messageDetails)
    {
        messageDetailsQueue.add(messageDetails);
    }

    public static synchronized MessageDetails getMessageFromQueue() {
        return  !messageDetailsQueue.isEmpty() ? messageDetailsQueue.remove() : null;
    }
}
