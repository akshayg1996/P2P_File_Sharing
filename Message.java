import java.io.UnsupportedEncodingException;

public class Message {

    private String type;
    private String length;
    private int dataLength = MessageConstants.MESSAGE_TYPE;
    private byte[] typeInBytes = null;
    private byte[] lengthInBytes = null;
    private byte[] payload = null;

    public Message() {
    }

    public Message(String messageType) {
        try {
            if (messageType == MessageConstants.MESSAGE_INTERESTED || messageType == MessageConstants.MESSAGE_NOT_INTERESTED) {
                setMessageLength(1);
                setMessageType(messageType);
                this.payload = null;
            } else {
                logAndShowInConsole("Error Occurred while initialzing Message constructor");
                throw new Exception("Message Constructor - Wrong constructor selected");
            }
        } catch (Exception e) {
            logAndShowInConsole(e.getMessage());
        }
    }

    public Message(String messageType, byte[] payload) {
        try {
            if (payload != null) {
                setMessageLength(payload.length + 1);
                if (lengthInBytes.length > MessageConstants.MESSAGE_LENGTH) {
                    logAndShowInConsole("Error Occurred while initialzing Message constructor");
                    throw new Exception("Message Constructor - Message Length is too large");
                }
                setPayload(payload);
            } else {
                if (messageType == MessageConstants.MESSAGE_INTERESTED || messageType == MessageConstants.MESSAGE_NOT_INTERESTED) {
                    setMessageLength(1);
                    this.payload = null;
                } else {
                    logAndShowInConsole("Error Occurred while initialzing Message constructor");
                    throw new Exception("Message Constructor - Message Payload should not be null");
                }
            }
            setMessageType(messageType);
            if (lengthInBytes.length > MessageConstants.MESSAGE_TYPE) {
                logAndShowInConsole("Error Occurred while initialzing Message constructor");
                throw new Exception("Message Constructor - Message Type length is too large");
            }
        } catch (Exception e) {
            logAndShowInConsole("Error Occurred while initialzing Message constructor - " + e.getMessage());
        }
    }

    public void setMessageType(String messageType) {
        type = messageType.trim();
        try {
            typeInBytes = messageType.getBytes(MessageConstants.DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            logAndShowInConsole(e.getMessage());
            e.printStackTrace();
        }
    }

    public void setMessageLength(int messageLength) {
        dataLength = messageLength;
        length = ((Integer) messageLength).toString();
        lengthInBytes = PeerProcessUtils.convertIntToByteArray(messageLength);
    }

    public static byte[] convertMessageToByteArray(Message message) {
        byte[] messageInByteArray = null;
        try {
            int messageType = Integer.parseInt(message.getType());
            if (message.getLengthInBytes().length > MessageConstants.MESSAGE_LENGTH)
                throw new Exception("Message Length is Invalid.");
            else if (messageType < 0 || messageType > 7)
                throw new Exception("Message Type is Invalid.");
            else if (message.getTypeInBytes() == null)
                throw new Exception("Message Type is Invalid.");
            else if (message.getLengthInBytes() == null)
                throw new Exception("Message Length is Invalid.");

            if (message.getPayload() != null) {
                messageInByteArray = new byte[MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE + message.getPayload().length];
                System.arraycopy(message.getLengthInBytes(), 0, messageInByteArray, 0, message.getLengthInBytes().length);
                System.arraycopy(message.getTypeInBytes(), 0, messageInByteArray, MessageConstants.MESSAGE_LENGTH, MessageConstants.MESSAGE_TYPE);
                System.arraycopy(message.getPayload(), 0, messageInByteArray,
                        MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE, message.getPayload().length);
            } else {
                messageInByteArray = new byte[MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE];
                System.arraycopy(message.getLengthInBytes(), 0, messageInByteArray, 0, message.getLengthInBytes().length);
                System.arraycopy(message.getTypeInBytes(), 0, messageInByteArray, MessageConstants.MESSAGE_LENGTH, MessageConstants.MESSAGE_TYPE);
            }
        } catch (Exception e) {
            logAndShowInConsole("Error occured - " + e.getMessage());
            messageInByteArray = null;
        }

        return messageInByteArray;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public byte[] getTypeInBytes() {
        return typeInBytes;
    }

    public void setTypeInBytes(byte[] typeInBytes) {
        this.typeInBytes = typeInBytes;
    }

    public byte[] getLengthInBytes() {
        return lengthInBytes;
    }

    public void setLengthInBytes(byte[] lengthInBytes) {
        this.lengthInBytes = lengthInBytes;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }

}
