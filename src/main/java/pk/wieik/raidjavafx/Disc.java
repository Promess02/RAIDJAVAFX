package pk.wieik.raidjavafx;

import java.util.*;

public class Disc {

    private static int numberOfDiscs;
    private String data;
    private int discIndex;

    public Disc(int numberOfDiscs, String data) {
        Disc.numberOfDiscs = numberOfDiscs;
        this.data = data;
    }

    public Disc(int discIndex) {
        this.discIndex = discIndex;
    }

    public int getNumberOfDiscs() {
        return numberOfDiscs;
    }

    public String getData() {
        return data;
    }

    public int getDamagedDiscIndex() {
        return discIndex;
    }

    public static int damagedDiscIndex() {
        Scanner scanner = new Scanner(System.in);
        String data;
        String regex = ("[0-" + (Disc.numberOfDiscs - 1) + "]");
        while (true) {
            System.out.println("Please insert damaged disc's index in RAID5 data storage");
            data = scanner.nextLine();

            if (data.matches(regex)) {
                System.out.println("Index of damaged disc: " + data);
                break;
            } else {
                System.out.println("Invalid disc's index. Choose between 0 and " + (Disc.numberOfDiscs - 1) + ". Please try again.");
            }
        }
        return Integer.parseInt(data);
    }

    public static String bitData() {
        Scanner scanner = new Scanner(System.in);
        String data;
        while (true) {
            System.out.println("Please insert bit sequence you want to save in a RAID5 data storage");
            data = scanner.nextLine();

            if (data.matches("1[01]+")) {
                System.out.println("Sequence accepted: " + data);
                break;
            } else {
                System.out.println("Invalid sequence. The sequence must start with 1. Please try again.");
            }
        }
        return data;
    }

    public static int numOfDiscs() {
        Scanner scanner = new Scanner(System.in);
        String data;
        while (true) {
            System.out.println("Please insert number of discs you want to use in a RAID5 data storage");
            data = scanner.nextLine();

            if (data.matches("[3-9]")) {
                System.out.println("Number of discs: " + data);
                break;
            } else {
                System.out.println("Invalid number of discs. Choose between 3 and 9. Please try again.");
            }
        }
        return Integer.parseInt(data);
    }
}