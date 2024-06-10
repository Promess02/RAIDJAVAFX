package pk.wieik.raidjavafx;

import java.util.List;

public class ConsoleSim {

    public static void main(String[] args) {
        // Get the number of disks from the user
        int numberOfDiscs = Disc.numOfDiscs();
        // Get the bit sequence from the user
        String data = Disc.bitData();
        int originalSize = data.length();
        // Create a new Disc object with the provided number of disks and data
        Disc disc = new Disc(numberOfDiscs, data);
        // Get the index of the damaged disk from the user
        Disc discNew = new Disc(Disc.damagedDiscIndex());

        // Create a list of disks
        List<List<Bit>> listOfDiscs = Cluster.createDiscList(disc);
        // Convert the input data to a list of booleans
        List<Bit> list = Cluster.toList(disc);
        // Save the data to the disks, including the parity information
        Cluster.saveData(list, listOfDiscs);

        // Simulate a disk failure and replace it with an empty disk
        Cluster.simulateDamage(discNew, listOfDiscs);

        // Print the contents of each disk
        for (int i = 0; i < listOfDiscs.size(); i++) {
            System.out.println("Disc " + i + ": " + listOfDiscs.get(i));
        }

         List<Bit> recoveredData = Cluster.readData(listOfDiscs, originalSize);

        // Print the recovered data
        System.out.println("Recovered data: " + recoveredData);
    }
}
