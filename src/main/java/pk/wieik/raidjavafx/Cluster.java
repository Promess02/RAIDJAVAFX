package pk.wieik.raidjavafx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cluster {

    /**
     * Simulates a disk failure by removing the specified disk and adding a new empty disk in its place.
     */
    public static void simulateDamage(Disc discNew, List<List<Bit>> listOfDiscs) {
        int discIndex = discNew.getDamagedDiscIndex();
        listOfDiscs.remove(discIndex);
        List<Bit> newDisc = new ArrayList<>(
                Collections.nCopies(listOfDiscs.get(0).size(), new Bit(false)));
        listOfDiscs.add(discIndex, newDisc);
    }

    /**
     * Calculates and stores the parity bits for RAID5.
     */
    public static void sumControl(List<List<Bit>> listOfDiscs) {
        int numberOfDiscs = listOfDiscs.size();
        int dataSize = listOfDiscs.get(0).size();
        int skipIndex = numberOfDiscs - 1;
        boolean decrementing = true;

        for (int i = 0; i < dataSize; i++) {
            boolean parityBit = false;
            for (int j = 0; j < numberOfDiscs; j++) {
                if (j != skipIndex) {
                    boolean dataBit = listOfDiscs.get(j).get(i).getBit();
                    parityBit ^= dataBit; // XOR operation for parity calculation
                }
            }
            Bit parityBitObj = new Bit(parityBit);
            parityBitObj.setParityBit(true);
            listOfDiscs.get(skipIndex).set(i, parityBitObj);

            Result result = adjustSkipIndex(skipIndex, numberOfDiscs, decrementing);
            // Alternate skipIndex to distribute parity bits across disks
            skipIndex = result.skipIndex;
            decrementing = result.decrementing;
        }
    }

    /**
     * Saves the data and calculates the parity for RAID5.
     */
    public static void saveData(List<Bit> list, List<List<Bit>> listOfDiscs) {
        int numberOfDiscs = listOfDiscs.size();
        int dataLength = list.size();
        int totalLength = dataLength + (numberOfDiscs - 1 - (dataLength % (numberOfDiscs - 1)));

        for (int i = dataLength; i < totalLength; i++) {
            list.add(new Bit(false));
        }

        int currentIndex = 0;
        boolean decrementing = true;
        int skipIndex = numberOfDiscs - 1;

        while (currentIndex < totalLength) {
            int currentIterationStartIndex = currentIndex;
            for (int i = 0; i < numberOfDiscs; i++) {
                if (i == skipIndex) {
                    listOfDiscs.get(i).add(new Bit(false));
                } else {
                    if (currentIndex < totalLength) {
                        Bit bit = list.get(currentIndex);
                        listOfDiscs.get(i).add(bit);
                        currentIndex++;
                    }
                }
            }

            if (currentIndex == currentIterationStartIndex) {
                break;
            }

            // Alternate skipIndex to distribute parity bits across disks
            Result result = adjustSkipIndex(skipIndex, numberOfDiscs, decrementing);
            skipIndex = result.skipIndex;
            decrementing = result.decrementing;
        }

        // Remove extra padding if the data length is a multiple of (numberOfDiscs - 1)
        if (dataLength % (listOfDiscs.size() - 1) == 0) {
            for (List<Bit> disc : listOfDiscs) {
                disc.remove(disc.size() - 1);
            }
        }

        // Calculate and store the parity bits
        sumControl(listOfDiscs);
    }

    public static Result adjustSkipIndex(int skipIndex, int numberOfDiscs, boolean decrementing) {
        if (decrementing) {
            skipIndex--;
            if (skipIndex < 0) {
                skipIndex = 1;
                decrementing = false;
            }
        } else {
            skipIndex++;
            if (skipIndex >= numberOfDiscs) {
                skipIndex = numberOfDiscs - 2;
                decrementing = true;
            }
        }
        return new Result(skipIndex,decrementing);
    }

    public static class Result {
        public int skipIndex;
        public boolean decrementing;

        public Result(int skipIndex, boolean decrementing) {
            this.skipIndex = skipIndex;
            this.decrementing = decrementing;
        }
    }

    /**
     * Creates a list of empty disks.
     */
    public static List<List<Bit>> createDiscList(Disc disc) {
        List<List<Bit>> listOfDiscs = new ArrayList<>();
        int numberOfDiscs = disc.getNumberOfDiscs();
        for (int i = 0; i < numberOfDiscs; i++) {
            List<Bit> list = new ArrayList<>();
            listOfDiscs.add(list);
        }
        return listOfDiscs;
    }

    /**
     * Converts the input data string to a list of Bits.
     */
    public static List<Bit> toList(Disc disc) {
        String data = disc.getData();
        List<Bit> list = new ArrayList<>();

        for (int i = 0; i < data.length(); i++) {
            char character = data.charAt(i);
            list.add(new Bit(character == '1'));
        }
        return list;
    }

    public static List<Bit> readData(List<List<Bit>> listOfDiscs, int originalDataSize) {
        List<Bit> recoveredData = new ArrayList<>();
        int numberOfDiscs = listOfDiscs.size();
        int skipIndex = numberOfDiscs - 1;
        boolean decrementing = true;

        int currentIndex = 0;
        while (recoveredData.size() < originalDataSize) {
            for (int i = 0; i < numberOfDiscs; i++) {
                if (i != skipIndex && currentIndex < listOfDiscs.get(i).size()) {
                    recoveredData.add(listOfDiscs.get(i).get(currentIndex));
                    if (recoveredData.size() == originalDataSize) {
                        break;
                    }
                }
            }

            currentIndex++;

            Result result = adjustSkipIndex(skipIndex, numberOfDiscs, decrementing);
            skipIndex = result.skipIndex;
            decrementing = result.decrementing;
        }

        // Remove padding false values
        while (recoveredData.size() > originalDataSize) {
            recoveredData.remove(recoveredData.size() - 1);
        }
        return recoveredData;
    }

    public static List<Bit> recoverData(List<List<Bit>> listOfDiscs, int discIndex, int originalDataSize) {
        int numberOfDiscs = listOfDiscs.size();
        int dataSize = listOfDiscs.get(0).size();

        // List to store recovered data
        List<Bit> recoveredData = new ArrayList<>(dataSize);
        int skipIndex = numberOfDiscs - 1;
        boolean decrementing = true;

        // Calculate parity bit for each index
        for (int i = 0; i < dataSize; i++) {
            boolean parityBit = false;
            for (int j = 0; j < numberOfDiscs; j++) {
                if (j != discIndex) {
                    boolean dataBit = listOfDiscs.get(j).get(i).getBit();
                    parityBit ^= dataBit;
                }
            }
            // Set the recovered bit based on parity
            Bit bit = new Bit(parityBit);
//            Result result = adjustSkipIndex(skipIndex, numberOfDiscs, decrementing);
//            skipIndex = result.skipIndex;
//            decrementing = result.decrementing;
//            int adjustedSkipIndex = (skipIndex+1)%numberOfDiscs;
//            if (adjustedSkipIndex == discIndex) {
//                bit.setParityBit(true);
//            }
            recoveredData.add(bit);
        }

        listOfDiscs.set(discIndex, recoveredData);
        sumControl(listOfDiscs);
        recoveredData = listOfDiscs.get(discIndex);
        return recoveredData;
    }
}
