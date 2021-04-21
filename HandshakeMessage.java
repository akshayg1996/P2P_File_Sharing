import java.io.UnsupportedEncodingException;

/**
 * This class is used to handle handshake message information
 */
public class HandshakeMessage {
    //Handshake header in bytes
    private byte[] headerInBytes = new byte[MessageConstants.HANDSHAKE_HEADER_LENGTH];
    //The peerID from where the handshake message is sent in bytes
    private byte[] peerIDInBytes = new byte[MessageConstants.HANDSHAKE_PEERID_LENGTH];
    //The zero bits to be padded at end in bytes
    private byte[] zeroBits = new byte[MessageConstants.HANDSHAKE_ZEROBITS_LENGTH];
    //Handshake header
    private String header;
    //The peerID from where the handshake message is sent
    private String peerID;

    /**
     * Empty constructor to create handshake object
     */
    public HandshakeMessage() {
    }

    /**
     * Constructor to create handshake with the header and peerID.
     * It sets headerInBytes, peerIDInBytes, zeroBits, header and peerID fields
     * @param header - Handshake header
     * @param peerID - The peerID from where the handshake message is sent
     */
    public HandshakeMessage(String header, String peerID) {
        try {
            this.header = header;
            this.headerInBytes = header.getBytes(MessageConstants.DEFAULT_CHARSET);
            if (this.headerInBytes.length > MessageConstants.HANDSHAKE_HEADER_LENGTH)
                throw new Exception("Handshake Header is too large");
            this.peerID = peerID;
            this.peerIDInBytes = peerID.getBytes(MessageConstants.DEFAULT_CHARSET);
            if (this.peerIDInBytes.length > MessageConstants.HANDSHAKE_PEERID_LENGTH)
                throw new Exception("Handshake PeerID is too large");
            this.zeroBits = "0000000000".getBytes(MessageConstants.DEFAULT_CHARSET);
        } catch (Exception e) {

        }
    }

    /**
     * This method is used to convert handshakeMessage to byte array
     * @param handshakeMessage - HandshakeMessage to be converted
     * @return byte array of the handshakeMessage
     */
    public static byte[] convertHandshakeMessageToBytes(HandshakeMessage handshakeMessage) {
        byte[] handshakeMessageInBytes = new byte[MessageConstants.HANDSHAKE_MESSAGE_LENGTH];
        try {
            if (handshakeMessage.getHeaderInBytes() == null ||
                    (handshakeMessage.getHeaderInBytes().length > MessageConstants.HANDSHAKE_HEADER_LENGTH || handshakeMessage.getHeaderInBytes().length == 0))
                throw new Exception("Handshake Message Header is Invalid");
            else
                System.arraycopy(handshakeMessage.getHeaderInBytes(), 0,
                        handshakeMessageInBytes, 0, handshakeMessage.getHeaderInBytes().length);

            if (handshakeMessage.getZeroBits() == null ||
                    (handshakeMessage.getZeroBits().length > MessageConstants.HANDSHAKE_ZEROBITS_LENGTH || handshakeMessage.getZeroBits().length == 0))
                throw new Exception("Handshake Message Zero Bits are Invalid");
            else
                System.arraycopy(handshakeMessage.getZeroBits(), 0,
                        handshakeMessageInBytes, MessageConstants.HANDSHAKE_HEADER_LENGTH, MessageConstants.HANDSHAKE_ZEROBITS_LENGTH - 1);

            if (handshakeMessage.getPeerIDInBytes() == null ||
                    (handshakeMessage.getPeerIDInBytes().length > MessageConstants.HANDSHAKE_PEERID_LENGTH || handshakeMessage.getPeerIDInBytes().length == 0))
                throw new Exception("Handshake Message Peer ID is Invalid");
            else
                System.arraycopy(handshakeMessage.getPeerIDInBytes(), 0, handshakeMessageInBytes,
                        MessageConstants.HANDSHAKE_HEADER_LENGTH + MessageConstants.HANDSHAKE_ZEROBITS_LENGTH,
                        handshakeMessage.getPeerIDInBytes().length);
        } catch (Exception e) {
            handshakeMessageInBytes = null;

        }

        return handshakeMessageInBytes;
    }

    /**
     * This method is used to convert byte array to handshakeMessage
     * @param handShakeMessage - byte array of HandshakeMessage
     * @return - Handshake message object
     */
    public static HandshakeMessage convertBytesToHandshakeMessage(byte[] handShakeMessage) {
        HandshakeMessage message = null;

        try {
            if (handShakeMessage.length != MessageConstants.HANDSHAKE_MESSAGE_LENGTH)
                throw new Exception("While Decoding Handshake message length is invalid");
            message = new HandshakeMessage();
            byte[] messageHeader = new byte[MessageConstants.HANDSHAKE_HEADER_LENGTH];
            byte[] messagePeerID = new byte[MessageConstants.HANDSHAKE_PEERID_LENGTH];

            System.arraycopy(handShakeMessage, 0, messageHeader, 0,
                    MessageConstants.HANDSHAKE_HEADER_LENGTH);
            System.arraycopy(handShakeMessage, MessageConstants.HANDSHAKE_HEADER_LENGTH
                            + MessageConstants.HANDSHAKE_ZEROBITS_LENGTH, messagePeerID, 0,
                    MessageConstants.HANDSHAKE_PEERID_LENGTH);

            message.setHeaderFromBytes(messageHeader);
            message.setPeerIDFromBytes(messagePeerID);

        } catch (Exception e) {

        }
        return message;
    }

    /**
     * This method is used to set peerID from byte array
     * @param messagePeerID - byte array of peerID
     */
    public void setPeerIDFromBytes(byte[] messagePeerID) {
        try {
            peerID = (new String(messagePeerID, MessageConstants.DEFAULT_CHARSET)).trim();
            peerIDInBytes = messagePeerID;
        } catch (UnsupportedEncodingException e) {
            logAndShowInConsole(e.getMessage());
        }
    }

    /**
     * This message is used to set handshake header from byte array
     * @param messageHeader - handshake header in bytes
     */
    public void setHeaderFromBytes(byte[] messageHeader) {
        try {
            header = (new String(messageHeader, MessageConstants.DEFAULT_CHARSET)).trim();
            headerInBytes = messageHeader;
        } catch (UnsupportedEncodingException e) {
            logAndShowInConsole(e.getMessage());
        }
    }

    /**
     * This method is used to get handshake header value in bytes
     * @return - headerInBytes
     */
    public byte[] getHeaderInBytes() {
        return headerInBytes;
    }

    /**
     * This method is used to set handshake header value in bytes
     * @param headerInBytes
     */
    public void setHeaderInBytes(byte[] headerInBytes) {
        this.headerInBytes = headerInBytes;
    }

    /**
     * This method is used to get handshake peerID in bytes
     * @return peerIDInBytes
     */
    public byte[] getPeerIDInBytes() {
        return peerIDInBytes;
    }

    /**
     * This method is used to set handshake peerID in bytes
     * @param peerIDInBytes
     */
    public void setPeerIDInBytes(byte[] peerIDInBytes) {
        this.peerIDInBytes = peerIDInBytes;
    }

    /**
     * This method is used to get handshake zero bits
     * @return zeroBits
     */
    public byte[] getZeroBits() {
        return zeroBits;
    }

    /**
     * This method is used to set handshake zero bits
     * @param zeroBits
     */
    public void setZeroBits(byte[] zeroBits) {
        this.zeroBits = zeroBits;
    }

    /**
     * This method is used to get handshake header
     * @return header
     */
    public String getHeader() {
        return header;
    }

    /**
     * This method is used to set handshake header
     * @param header
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * This method is used to get handshake peerID
     * @return peerID
     */
    public String getPeerID() {
        return peerID;
    }

    /**
     * This method is used to set handshake peerID
     * @param peerID
     */
    public void setPeerID(String peerID) {
        this.peerID = peerID;
    }

    /**
     * This method is used to log a message in a log file and show it in console
     * @param message - message to be logged and showed in console
     */
    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }
}
