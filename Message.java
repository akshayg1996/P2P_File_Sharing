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
            if (messageType == MessageConstants.MESSAGE_INTERESTED || messageType == MessageConstants.MESSAGE_NOT_INTERESTED ||
                    messageType == MessageConstants.MESSAGE_CHOKE || messageType == MessageConstants.MESSAGE_UNCHOKE
                    || messageType == MessageConstants.MESSAGE_DOWNLOADED
                   ) {
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
                if (messageType == MessageConstants.MESSAGE_INTERESTED || messageType == MessageConstants.MESSAGE_NOT_INTERESTED
                        || messageType == MessageConstants.MESSAGE_CHOKE || messageType == MessageConstants.MESSAGE_UNCHOKE
                        || messageType == MessageConstants.MESSAGE_DOWNLOADED ) {
                    setMessageLength(1);
                    this.payload = null;
                } else {
                    logAndShowInConsole("Error Occurred while initialzing Message constructor");
                    throw new Exception("Message Constructor - Message Payload should not be null");
                }
            }
            setMessageType(messageType);
            if (typeInBytes.length > MessageConstants.MESSAGE_TYPE) {
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

    public void setMessageLength(byte[] len) {

        Integer l = PeerProcessUtils.convertByteArrayToInt(len);
        this.length = l.toString();
        this.lengthInBytes = len;
        this.dataLength = l;
    }

    public void setMessageType(byte[] type) {
        try {
            this.type = new String(type, MessageConstants.DEFAULT_CHARSET);
            this.typeInBytes = type;
        } catch (UnsupportedEncodingException e) {
            logAndShowInConsole(e.toString());
        }
    }

    public int getMessageLengthAsInteger() {
        return this.dataLength;
    }

    public static byte[] convertMessageToByteArray(Message message) {
        byte[] messageInByteArray = null;
        try {
            int messageType = Integer.parseInt(message.getType());
            if (message.getLengthInBytes().length > MessageConstants.MESSAGE_LENGTH)
                throw new Exception("Message Length is Invalid.");
            else if (messageType < 0 || messageType > 8)
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

    public static Message convertByteArrayToMessage(byte[] message) {

        Message msg = new Message();
        byte[] msgLength = new byte[MessageConstants.MESSAGE_LENGTH];
        byte[] msgType = new byte[MessageConstants.MESSAGE_TYPE];
        byte[] payLoad = null;
        int len;

        try {
            if (message == null)
                throw new Exception("Invalid data.");
            else if (message.length < MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE)
                throw new Exception("Byte array length is too small...");


            System.arraycopy(message, 0, msgLength, 0, MessageConstants.MESSAGE_LENGTH);
            System.arraycopy(message, MessageConstants.MESSAGE_LENGTH, msgType, 0, MessageConstants.MESSAGE_TYPE);

            msg.setMessageLength(msgLength);
            msg.setMessageType(msgType);

            len = PeerProcessUtils.convertByteArrayToInt(msgLength);

            if (len > 1) {
                payLoad = new byte[len - 1];
                System.arraycopy(message, MessageConstants.MESSAGE_LENGTH + MessageConstants.MESSAGE_TYPE, payLoad, 0, message.length - MessageConstants.MESSAGE_LENGTH - MessageConstants.MESSAGE_TYPE);
                msg.setPayload(payLoad);
            }

            payLoad = null;
        } catch (Exception e) {
            LogHelper.logAndShowInConsole(e.toString());
            msg = null;
        }
        return msg;
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
