import org.testng.annotations.Test;
import pk.wieik.raidjavafx.Bit;
import pk.wieik.raidjavafx.Cluster;
import pk.wieik.raidjavafx.Disc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

public class ClusterTest {

    @Test
    public void testSaveAndReadData() {
        // Input string of 50 characters
        String inputData = "11101010101010101010101010101010101010101010101010";
        assertEquals("Input data should be 50 characters long", 50, inputData.length());

        // Convert input string to list of booleans
        List<Bit> inputList = new ArrayList<>();
        for (char c : inputData.toCharArray()) {
            inputList.add(c == '1' ? new Bit(true) : new Bit(false));
        }

        // Number of discs for RAID 5 (minimum 3 for RAID 5 to work)
        int numberOfDiscs = 5;

        // Create Disc object
        Disc disc = new Disc(numberOfDiscs, inputData);

        // Create list of discs
        List<List<Bit>> listOfDiscs = Cluster.createDiscList(disc);

        // Save data to the discs
        Cluster.saveData(inputList, listOfDiscs);

        // Read data back from the discs
        List<Bit> outputList = Cluster.readData(listOfDiscs, inputData.length());

        //removing padding values
        while (inputList.size() > outputList.size()) {
            inputList.remove(inputList.size() - 1);
        }

        // Compare input and output lists
        assertEquals("The input data should match the output data", inputList, outputList);
    }

    @Test
    public void testRecoverData() {
        // Input string of 50 characters
        String inputData = "11101010101010101010101010101010101010101010101010";

        // Convert input string to list of booleans
        List<Bit> inputList = new ArrayList<>();
        for (char c : inputData.toCharArray()) {
            inputList.add(c == '1' ? new Bit(true) : new Bit(false));
        }

        // Number of discs for RAID 5 (minimum 3 for RAID 5 to work)
        int numberOfDiscs = 5;

        // Create Disc object
        Disc disc = new Disc(numberOfDiscs, inputData);

        // Create list of discs
        List<List<Bit>> listOfDiscs = Cluster.createDiscList(disc);

        // Save data to the discs
        Cluster.saveData(inputList, listOfDiscs);

        // Simulate damage to a disc
        int damagedDiscIndex = 2; // Assume disc at index 2 is damaged
        // Compare recovered data with data in the specified disc
        List<Bit> actualDataInDisc = listOfDiscs.get(damagedDiscIndex);
        listOfDiscs.set(damagedDiscIndex, new ArrayList<>());

        // Recover data from damaged disc
        List<Bit> recoveredData = Cluster.recoverData(listOfDiscs, damagedDiscIndex, inputData.length());
        List<Boolean> cleanedRecovered = cleanParityInfo(recoveredData);
        List<Boolean> cleanOriginal = cleanParityInfo(actualDataInDisc);

        assertEquals("Recovered data should match actual data in the disc", cleanedRecovered, cleanOriginal);
    }

    // Correct number of discs is created based on user input
    @Test
    public void test_correct_number_of_discs_created() {
        int numberOfDiscs = 5;
        Disc disc = new Disc(numberOfDiscs, "10101");
        List<List<Bit>> listOfDiscs = Cluster.createDiscList(disc);
        assertEquals(numberOfDiscs, listOfDiscs.size());
    }

    // Data is correctly converted to a list of booleans
    @Test
    public void test_data_conversion_to_boolean_list() {
        Disc disc = new Disc(5, "10101");
        List<Bit> booleanList = Cluster.toList(disc);
        List<Boolean> expectedList = new ArrayList<>(){
            {
                add(Boolean.TRUE);
                add(Boolean.FALSE);
                add(Boolean.TRUE);
                add(Boolean.FALSE);
                add(Boolean.TRUE);
            }};
        List<Boolean> cleanedList = cleanParityInfo(booleanList);
        assertEquals(expectedList, cleanedList);
    }

    // Data is correctly saved across multiple discs
    @Test
    public void test_data_saved_across_discs() {
        Disc disc = new Disc(4, "10101");
        List<List<Bit>> listOfDiscs = Cluster.createDiscList(disc);
        List<Bit> dataList = Cluster.toList(disc);
        Cluster.saveData(dataList, listOfDiscs);
        assertFalse(listOfDiscs.get(0).isEmpty());
        assertFalse(listOfDiscs.get(1).isEmpty());
        assertFalse(listOfDiscs.get(2).isEmpty());
    }

    // Parity bits are correctly calculated and saved
    @Test
    public void test_parity_bits_calculation() {
        Disc disc = new Disc(4, "10101");
        List<List<Bit>> listOfDiscs = Cluster.createDiscList(disc);
        List<Bit> dataList = Cluster.toList(disc);
        Cluster.saveData(dataList, listOfDiscs);
        boolean parityBit = listOfDiscs.get(3).get(0).getBit();
        assertFalse(parityBit);
    }

    @Test
    public void test_removes_padding_when_data_length_is_multiple() {
        List<List<Bit>> listOfDiscs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            listOfDiscs.add(new ArrayList<>());
        }
        List<Bit> data = new ArrayList<>(Collections.nCopies(6, new Bit(true)));
        Cluster.saveData(data, listOfDiscs);
        for (List<Bit> disc : listOfDiscs) {
            assertEquals(2, disc.size());
        }
    }

    @Test
    public void test_handles_empty_data_input_gracefully() {
        List<List<Bit>> listOfDiscs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            listOfDiscs.add(new ArrayList<>());
        }
        List<Bit> data = new ArrayList<>();
        Cluster.saveData(data, listOfDiscs);
        for (List<Bit> disc : listOfDiscs) {
            assertTrue(disc.isEmpty());
        }
    }

    // User inputs the minimum number of discs (3)
    @Test
    public void test_minimum_number_of_discs() {
        int numberOfDiscs = 3;
        Disc disc = new Disc(numberOfDiscs, "101");
        List<List<Bit>> listOfDiscs = Cluster.createDiscList(disc);
        assertEquals(numberOfDiscs, listOfDiscs.size());
    }

    // User inputs the maximum number of discs (9)
    @Test
    public void test_maximum_number_of_discs() {
        int numberOfDiscs = 9;
        Disc disc = new Disc(numberOfDiscs, "101010101");
        List<List<Bit>> listOfDiscs = Cluster.createDiscList(disc);
        assertEquals(numberOfDiscs, listOfDiscs.size());
    }


    // User inputs an invalid disc index for damage simulation
    @Test
    public void test_invalid_disc_index_for_damage_simulation() {
        int numberOfDiscs = 5;
        Disc disc = new Disc(numberOfDiscs, "10101");
        List<List<Bit>> listOfDiscs = Cluster.createDiscList(disc);
        List<Bit> dataList = Cluster.toList(disc);
        Cluster.saveData(dataList, listOfDiscs);

        int invalidIndex = 6;
        try {
            Disc damagedDisc = new Disc(invalidIndex);
            Cluster.simulateDamage(damagedDisc, listOfDiscs);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Test passes
        }
    }


    @Test
    public void testAdjustSkipIndex() {
        int numberOfDiscs = 5;

        // Test case 1: decrementing is true and skipIndex is positive
        int skipIndex = 4;
        boolean decrementing = true;
        Cluster.Result result = Cluster.adjustSkipIndex(skipIndex, numberOfDiscs, decrementing);
        assertEquals(3, result.skipIndex);
        assertTrue(result.decrementing);

        // Test case 2: decrementing is true and skipIndex becomes negative
        skipIndex = 0;
        decrementing = true;
        result = Cluster.adjustSkipIndex(skipIndex, numberOfDiscs, decrementing);
        assertEquals(1, result.skipIndex);
        assertFalse(result.decrementing);

        // Test case 3: decrementing is false and skipIndex is less than numberOfDiscs
        skipIndex = 1;
        decrementing = false;
        result = Cluster.adjustSkipIndex(skipIndex, numberOfDiscs, decrementing);
        assertEquals(2, result.skipIndex);
        assertFalse(result.decrementing);

        // Test case 4: decrementing is false and skipIndex reaches numberOfDiscs
        skipIndex = 4;
        decrementing = false;
        result = Cluster.adjustSkipIndex(skipIndex, numberOfDiscs, decrementing);
        assertEquals(3, result.skipIndex);
        assertTrue(result.decrementing);
    }

    private List<Boolean> cleanParityInfo(List<Bit> list){
        List<Boolean> returnList = new ArrayList<>();
        for(Bit bit:list){
            returnList.add(bit.getBit());
        }
        return returnList;
    }
}
