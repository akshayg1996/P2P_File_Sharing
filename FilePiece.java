public class FilePiece {

    private int isPresent;
    private String fromPeerID;
    private byte[] content;
    private int pieceIndex;

    public FilePiece() {
        content = new byte[CommonConfiguration.pieceSize];
        pieceIndex = -1;
        isPresent = 0;
        fromPeerID = null;
    }

    public int getIsPresent() {
        return isPresent;
    }

    public void setIsPresent(int isPresent) {
        this.isPresent = isPresent;
    }

    public String getFromPeerID() {
        return fromPeerID;
    }

    public void setFromPeerID(String fromPeerID) {
        this.fromPeerID = fromPeerID;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public int getPieceIndex() {
        return pieceIndex;
    }

    public void setPieceIndex(int pieceIndex) {
        this.pieceIndex = pieceIndex;
    }

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
