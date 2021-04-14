import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BitFieldMessage {

    private FilePiece[] filePieces;
    private int numberOfPieces;

    public BitFieldMessage() {
        Double fileSize = Double.parseDouble(String.valueOf(CommonConfiguration.fileSize));
        Double pieceSize = Double.parseDouble(String.valueOf(CommonConfiguration.pieceSize));
        numberOfPieces = (int) Math.ceil(fileSize / pieceSize);
        filePieces = new FilePiece[numberOfPieces];

        for (int i = 0; i < numberOfPieces; i++) {
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

    public int getNumberOfPiecesPresent() {
        int count = 0;
        for (FilePiece filePiece : filePieces) {
            if (filePiece.getIsPresent() == 1) {
                count++;
            }
        }

        return count;
    }

    public boolean isFileDownloadComplete() {
        boolean isFileDownloaded = true;
        for (FilePiece filePiece : filePieces) {
            if (filePiece.getIsPresent() == 0) {
                isFileDownloaded = false;
                break;
            }
        }

        return isFileDownloaded;
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

    public synchronized int getFirstDifferentPieceIndex(BitFieldMessage bitFieldMessage) {
        int firstPieces = numberOfPieces;
        int secondPieces = bitFieldMessage.getNumberOfPieces();
        int pieceIndex = -1;

        if (secondPieces >= firstPieces) {
            for (int i = 0; i < firstPieces; i++) {
                if (filePieces[i].getIsPresent() == 0 && bitFieldMessage.getFilePieces()[i].getIsPresent() == 1) {
                    pieceIndex = i;
                    break;
                }
            }
        } else {
            for (int i = 0; i < secondPieces; i++) {
                if (filePieces[i].getIsPresent() == 0 && bitFieldMessage.getFilePieces()[i].getIsPresent() == 1) {
                    pieceIndex = i;
                    break;
                }
            }
        }

        return pieceIndex;
    }

    public void updateBitFieldInformation(String peerID, FilePiece filePiece) {
        int pieceIndex = filePiece.getPieceIndex();
        try {
            if (isPieceAlreadyPresent(pieceIndex)) {
                logAndShowInConsole(peerID + " Piece already received!!");
            } else {
                String fileName = CommonConfiguration.fileName;

                File file = new File(peerProcess.currentPeerID, fileName);
                int offSet = pieceIndex * CommonConfiguration.pieceSize;
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                byte[] pieceToWrite = filePiece.getContent();
                randomAccessFile.seek(offSet);
                randomAccessFile.write(pieceToWrite);

                filePieces[pieceIndex].setIsPresent(1);
                filePieces[pieceIndex].setFromPeerID(peerID);
                randomAccessFile.close();
                logAndShowInConsole(peerProcess.currentPeerID + " has downloaded the PIECE " + pieceIndex
                        + " from Peer " + peerID + ". Now the number of pieces it has is "
                        + peerProcess.bitFieldMessage.getNumberOfPiecesPresent());

                if (peerProcess.bitFieldMessage.isFileDownloadComplete()) {
                    peerProcess.remotePeerDetailsMap.get(peerID).setIsInterested(0);
                    peerProcess.remotePeerDetailsMap.get(peerID).setIsComplete(1);
                    peerProcess.remotePeerDetailsMap.get(peerID).setIsChoked(0);
                    peerProcess.remotePeerDetailsMap.get(peerID).updatePeerDetails(peerProcess.currentPeerID, 1);
                    logAndShowInConsole(peerProcess.currentPeerID + " has DOWNLOADED the complete file.");
                }
            }
        } catch (IOException e) {
            logAndShowInConsole(peerProcess.currentPeerID + " EROR in updating bitfield " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isPieceAlreadyPresent(int pieceIndex) {
        return peerProcess.bitFieldMessage.getFilePieces()[pieceIndex].getIsPresent() == 1;
    }

    private static void logAndShowInConsole(String message) {
        LogHelper.logAndShowInConsole(message);
    }
}
