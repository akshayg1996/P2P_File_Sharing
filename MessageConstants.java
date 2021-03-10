public class MessageConstants {

    // Message Encoding style
    public static final String DEFAULT_CHARSET = "UTF8";

    // For Handshake message header
    public static final int HANDSHAKE_MESSAGE_LENGTH = 32;

    public static final int HANDSHAKE_HEADER_LENGTH = 18;

    public static final int HANDSHAKE_ZEROBITS_LENGTH = 10;

    public static final int HANDSHAKE_PEERID_LENGTH = 4;

    // For Handshake message header
    public static final int MESSAGE_LENGTH = 4;

    public static final int MESSAGE_TYPE = 1;

    public static final String MESSAGE_CHOKE = "0";

    public static final String MESSAGE_UNCHOKE = "1";

    public static final String MESSAGE_INTERESTED = "2";

    public static final String MESSAGE_NOT_INTERESTED = "3";

    public static final String MESSAGE_HAVE = "4";

    public static final String MESSAGE_BITFIELD = "5";

    public static final String MESSAGE_REQUEST = "6";

    public static final String MESSAGE_PIECE = "7";

    public static final String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";

    public static final int PIECE_INDEX_LENGTH = 4;

    public static final int ACTIVE_CONNECTION = 1;

    public static final int PASSIVE_CONNECTION = 0;

}
