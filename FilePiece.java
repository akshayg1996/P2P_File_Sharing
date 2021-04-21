/**
 * This class is used to handle file piece information
 */
public class FilePiece {
    //If piece is present
    private int isPresent;
    //Peer from where piece is retrieved
    private String fromPeerID;
    //The content of the piece
    private byte[] content;
    //The index of the piece in the list of pieces
    private int pieceIndex;

    /**
     * Constructor to initialize file piece information
     */
    public FilePiece() {
        content = new byte[CommonConfiguration.pieceSize];
        pieceIndex = -1;
        isPresent = 0;
        fromPeerID = null;
    }

    /**
     * This method is used to check if piece is present or not
     * @return 0 - piece not present; 1 - piece present
     */
    public int getIsPresent() {
        return isPresent;
    }

    /**
     * This method is used to set piece present value
     * @param isPresent - piece present value
     */
    public void setIsPresent(int isPresent) {
        this.isPresent = isPresent;
    }

    /**
     * This method is used to get peerID from where the piece is received
     * @return peerID from where the piece is received
     */
    public String getFromPeerID() {
        return fromPeerID;
    }

    /**
     * This method is used to set the peerID from where the piece is received
     * @param fromPeerID - peerID from where the piece is received
     */
    public void setFromPeerID(String fromPeerID) {
        this.fromPeerID = fromPeerID;
    }

    /**
     * This method is used to get the content of piece
     * @return - content of the piece
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * This method is used to set the content of piece
     * @param - content of the piece
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * This method is used to get the index of piece
     * @return - index of the piece
     */
    public int getPieceIndex() {
        return pieceIndex;
    }

    /**
     * This method is used to set the index of piece
     * @param - index of the piece
     */
    public void setPieceIndex(int pieceIndex) {
        this.pieceIndex = pieceIndex;
    }

    /**
     * This method is used to convert a file piece byte aray to File piece object
     * @param payloadInBytes - byte array of file piece
     * @return - FilePiece object
     */
    public static FilePiece convertByteArrayToFilePiece(byte[] payloadInBytes) {
        byte[] indexInBytes = new byte[MessageConstants.PIECE_INDEX_LENGTH];
        FilePiece filePiece = new FilePiece();
        System.arraycopy(payloadInBytes, 0, indexInBytes, 0, MessageConstants.PIECE_INDEX_LENGTH);
        filePiece.setPieceIndex(PeerProcessUtils.convertByteArrayToInt(indexInBytes));
        filePiece.setContent(new byte[payloadInBytes.length - MessageConstants.PIECE_INDEX_LENGTH]);
        System.arraycopy(payloadInBytes, MessageConstants.PIECE_INDEX_LENGTH, filePiece.getContent(), 0, payloadInBytes.length - MessageConstants.PIECE_INDEX_LENGTH);
        return filePiece;
    }
}
