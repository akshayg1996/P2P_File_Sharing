public class HandshakeMessage {
    private byte[] headerInBytes = new byte[MessageConstants.HANDSHAKE_HEADER_LENGTH];
    private byte[] peerIDInBytes = new byte[MessageConstants.HANDSHAKE_PEERID_LENGTH];
    private byte[] zeroBits = new byte[MessageConstants.HANDSHAKE_ZEROBITS_LENGTH];
    private String header;
    private String peerID;

    public HandshakeMessage() {
    }

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
            logAndShowInConsole(peerID + " Error Occured while initializing handshake message constructor - " + e.getMessage());
            e.printStackTrace();
        }
    }

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
            logAndShowInConsole(handshakeMessage.getPeerID() + " Error Occured while converting handshake message into bytes - " + e.getMessage());
            e.printStackTrace();
        }

        return handshakeMessageInBytes;
    }

    public static HandshakeMessage convertBytesToHandshakeMessage(byte[] handShakeMessage) {
        HandshakeMessage message = null;

        try {
          if(handShakeMessage.length != MessageConstants.HANDSHAKE_MESSAGE_LENGTH)
              throw new Exception("While Decoding Handshake message length is invalid");
          message = new HandshakeMessage();

        }catch (Exception e) {

        }

        return message;
    }

    public byte[] getHeaderInBytes() {
        return headerInBytes;
    }

    public void setHeaderInBytes(byte[] headerInBytes) {
        this.headerInBytes = headerInBytes;
    }

    public byte[] getPeerIDInBytes() {
        return peerIDInBytes;
    }

    public void setPeerIDInBytes(byte[] peerIDInBytes) {
        this.peerIDInBytes = peerIDInBytes;
    }

    public byte[] getZeroBits() {
        return zeroBits;
    }

    public void setZeroBits(byte[] zeroBits) {
        this.zeroBits = zeroBits;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getPeerID() {
        return peerID;
    }

    public void setPeerID(String peerID) {
        this.peerID = peerID;
    }

    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }
}
