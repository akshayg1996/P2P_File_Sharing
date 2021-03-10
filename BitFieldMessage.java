public class BitFieldMessage {

    private FilePiece[] filePieces;
    private int numberOfPieces;

    public BitFieldMessage() {
        Double fileSize = Double.parseDouble(String.valueOf(CommonConfiguration.fileSize));
        Double pieceSize = Double.parseDouble(String.valueOf(CommonConfiguration.pieceSize));
        numberOfPieces = (int) Math.ceil(fileSize / pieceSize);
        filePieces = new FilePiece[numberOfPieces];

        for(int i=0; i< numberOfPieces; i++) {
            filePieces[i] = new FilePiece();
        }
    }

    public FilePiece[] getFilePieces() {
        return filePieces;
    }

    public void setFilePieces(FilePiece[] filePieces) {
        this.filePieces = filePieces;
    }

    public int getNumberOfPieces() {
        return numberOfPieces;
    }

    public void setNumberOfPieces(int numberOfPieces) {
        this.numberOfPieces = numberOfPieces;
    }

    public void setPieceDetails(String peerId, int hasFile) {
        for (FilePiece filePiece : filePieces) {
            filePiece.setIsPresent(hasFile == 1 ? 1 : 0);
            filePiece.setFromPeerID(peerId);
        }
    }

    public byte[] getBytes() {
        int s = numberOfPieces / 8;
        if (numberOfPieces % 8 != 0)
            s = s + 1;
        byte[] iP = new byte[s];
        int tempInt = 0;
        int count = 0;
        int Cnt;
        for (Cnt = 1; Cnt <= numberOfPieces; Cnt++) {
            int tempP = filePieces[Cnt - 1].getIsPresent();
            tempInt = tempInt << 1;
            if (tempP == 1) {
                tempInt = tempInt + 1;
            } else
                tempInt = tempInt + 0;

            if (Cnt % 8 == 0 && Cnt != 0) {
                iP[count] = (byte) tempInt;
                count++;
                tempInt = 0;
            }

        }
        if ((Cnt - 1) % 8 != 0) {
            int tempShift = ((numberOfPieces) - (numberOfPieces / 8) * 8);
            tempInt = tempInt << (8 - tempShift);
            iP[count] = (byte) tempInt;
        }
        return iP;
    }

    public static BitFieldMessage decodeMessage(byte[] bitField) {
        BitFieldMessage bitFieldMessage = new BitFieldMessage();
        for (int i = 0; i < bitField.length; i++) {
            int count = 7;
            while (count >= 0) {
                int test = 1 << count;
                if (i * 8 + (8 - count - 1) < bitFieldMessage.getNumberOfPieces()) {
                    if ((bitField[i] & (test)) != 0)
                        bitFieldMessage.getFilePieces()[i * 8 + (8 - count - 1)].setIsPresent(1);
                    else
                        bitFieldMessage.getFilePieces()[i * 8 + (8 - count - 1)].setIsPresent(0);
                }
                count--;
            }
        }

        return bitFieldMessage;
    }

    public synchronized boolean containsInterestingPieces(BitFieldMessage bitFieldMessage) {
        int numberOfPieces = bitFieldMessage.getNumberOfPieces();
        boolean hasInterestingPieces = false;

        for (int i = 0; i < numberOfPieces; i++) {
            if (bitFieldMessage.getFilePieces()[i].getIsPresent() == 1
                    && this.getFilePieces()[i].getIsPresent() == 0) {
                hasInterestingPieces = true;
                break;
            } else
                continue;
        }

        return hasInterestingPieces;
    }

}
