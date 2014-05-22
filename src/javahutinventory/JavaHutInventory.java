package javahutinventory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 *
 * @author scott.walker
 */
public class JavaHutInventory {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        TreeMap<String, Integer> itemMap = new TreeMap<>();

        //Load itemMap with item numbers and counts
        Files.lines(Paths.get("JavaHut14Intake.txt", ""),
                Charset.forName("US-ASCII"))
                .filter((String s)
                        -> !s.equals("") && !s.contains("-"))
                .forEach((String s) -> {
                    if (itemMap.containsKey(s)) {
                        itemMap.replace(s, itemMap.get(s) + 1);
                    } else {
                        itemMap.put(s, 1);
                    }
                });

        //Write out itemMap to file
        Files.write(Paths.get("JavaHut14ItemList.txt", ""),
                (Iterable<String>) () -> itemMap.entrySet()
                .parallelStream()
                .map(e -> e.getKey()
                        + "\t"
                        + e.getValue().toString()).iterator(),
                Charset.forName("US-ASCII"),
                StandardOpenOption.TRUNCATE_EXISTING);

        TreeMap<String, ItemRecord> namePackMap = new TreeMap<>();

        //Populate namePackMap with IDs and inventory data 
        //  (item class / pack count)
        Files.lines(Paths.get("JavaHut14Inventory.dat", ""),
                Charset.forName("US-ASCII"))
                .forEach((String s) -> {
                    String[] parts = s.split(",");
                    namePackMap.put(parts[0],
                            new ItemRecord(parts[1],
                                    Integer.parseInt(parts[2].trim())));
                });

        //countMap contains the item class name as a key and an
        //  Integer count
        TreeMap<String, Integer> countMap = new TreeMap<>();

        //Load countMap by item class name within the ItemRecord and
        //  add up unit counts
        itemMap.forEach((String s, Integer i) -> {
            ItemRecord ir = namePackMap.get(s);
            if (countMap.containsKey(ir.name)) {
                int count = countMap.get(ir.name);
                countMap.replace(ir.name, count, count + i * ir.packSize);
            } else {
                countMap.put(ir.name, i * ir.packSize);
            }
        });

        //This ArrayList is to facilitate output
        ArrayList<String> itemCounts = countMap.entrySet().parallelStream()
                .map(e -> String.format("%1$-20s", e.getKey())
                        + "\t"
                        + e.getValue().toString())
                .collect(Collectors.toCollection(ArrayList::new));

        //Console and file output
        itemCounts.forEach(s -> System.out.println(s));

        Files.write(Paths.get("JavaHut14ItemCounts.txt", ""), itemCounts,
                Charset.forName("US-ASCII"),
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static class ItemRecord {

        String name;
        int packSize;

        public ItemRecord(String n, int ps) {
            name = n;
            packSize = ps;
        }

        @Override
        public String toString() {
            return name + "\t" + packSize + " count";
        }
    }

}
