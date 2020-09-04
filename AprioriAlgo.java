
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class AprioriAlgo {
    private static HashMap<String, Integer> supportmap;
    private static float confindence_threshold;

    private static void findConfidence_of_inference(ArrayList<String> antecedent, ArrayList<String> items) {
        ArrayList<String> consequent = new ArrayList<>(items);

        for (String temp : antecedent) {
            consequent.remove(temp);
        }
        float confindence = (float) supportmap.get(items.toString()) / supportmap.get(antecedent.toString());
        // float lift = (float) supportmap.get(items.toString()) /
        // (supportmap.get(antecedent.toString()) *
        // supportmap.get(consequent.toString()));
        // System.out.println(antecedent.toString() + "==>" + consequent.toString() +
        // "[" + confindence * 100 + "]" + "["+ lift * 100000 + "]");
        if (Float.compare(confindence * 100, confindence_threshold) > 0) {
            System.out.println(antecedent.toString() + "==>" + consequent.toString() + "[" + confindence * 100 + "]");
        }
    }

    private static void findSubsetUtil(ArrayList<String> items, ArrayList<String> subsets, ArrayList<String> temp,
            int ind) {
        if (temp.size() != 0 && temp.size() != items.size()) {
            subsets.add(temp.toString());
            findConfidence_of_inference(temp, items);
        }
        for (int i = ind; i < items.size(); i++) {
            temp.add(items.get(i));
            findSubsetUtil(items, subsets, temp, i + 1);
            temp.remove(temp.size() - 1);
        }
    }

    private static ArrayList<String> findSubset(ArrayList<String> items) {
        ArrayList<String> subsets = new ArrayList<>();
        ArrayList<String> temp = new ArrayList<>();
        int ind = 0;
        findSubsetUtil(items, subsets, temp, ind);
        return subsets;
    }

    private static void findRules(ArrayList<String> lastarm) {

        int len = lastarm.size();

        for (int i = 0; i < len; i++) {
            String temp = lastarm.get(i);
            temp = temp.replace("[", "").replace("]", "");
            String[] arr = temp.split(",");
            ArrayList<String> items = new ArrayList<>();
            for (int j = 0; j < arr.length; j++) {
                items.add(arr[j].trim());
            }

            ArrayList<String> subsets = findSubset(items);
            // System.out.println(subsets);
        }
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        supportmap = new HashMap<String, Integer>();
        ArrayList<String> lastarm = new ArrayList<>();
        confindence_threshold = 25;
        for (int i = 1; i <= 3; i++) {
            BufferedReader reader = new BufferedReader(
                    new FileReader("D:\\hadoop\\lab5\\output\\output" + i + "\\part-r-00000"));
            String line = reader.readLine();
            while (line != null) {
                String[] arr;
                String itemset;
                if (i == 1) {
                    arr = line.split("\\s");
                    itemset = "[" + arr[0] + "]";
                } else {
                    arr = line.split("]");
                    itemset = arr[0] + "]";
                    if (i == 3) {
                        lastarm.add(itemset);
                    }
                }
                int support = Integer.parseInt(arr[1].trim());
                supportmap.put(itemset, support);
                line = reader.readLine();
            }
            reader.close();
        }
        // System.out.println(supportmap.toString());
        // System.out.println(lastarm.toString());
        findRules(lastarm);
    }
}