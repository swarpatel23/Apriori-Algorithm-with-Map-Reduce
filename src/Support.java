import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.fs.FileSystem;

public class Support {

    public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text item = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString(), ",");
            while (itr.hasMoreTokens()) {
                item.set(itr.nextToken().replace(" ", ""));
                context.write(item, one);
            }
        }
    }

    public static class KTokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text item = new Text();
        // private static ArrayList<String> kpairs;
        private static HashSet<String> previousItemSet;

        @Override
        public void setup(Context context) throws IOException {
            int passNum = context.getConfiguration().getInt("iteration", 2);
            // path of previous map-reduce output file
            String lastPassOutputFile = "/user/swar/lab5/output" + (passNum - 1) + "/part-r-00000";
            previousItemSet = new HashSet<>();
            System.err.println("setup is called for iteration" + passNum);
            try {
                Path path = new Path(lastPassOutputFile);
                FileSystem fs = FileSystem.get(context.getConfiguration());
                BufferedReader fis = new BufferedReader(new InputStreamReader(fs.open(path)));
                String currLine;
                while ((currLine = fis.readLine()) != null) {
                    currLine = currLine.replace("[", "");
                    currLine = currLine.replace("]", "");
                    String[] words = currLine.split("[\\s\\t]+");
                    if (words.length < 2) {
                        continue;
                    }
                    String finalWord = words[words.length - 1];
                    int support = Integer.parseInt(finalWord);
                    ArrayList<String> temp = new ArrayList<>();
                    for (int k = 0; k < words.length - 1; k++) {
                        String s = words[k];
                        s = s.replace(",", "");
                        s = s.trim();
                        temp.add(s);
                    }
                    previousItemSet.add(temp.toString());
                }
                System.out.println(previousItemSet.toString());
            } catch (Exception e) {
            }
        }

        /*
         * if given itemset's all subset of size = pair_size - 1; present in previous
         * item then only then it return true otherwise return false
         */
        private static boolean isValidItemSet(String itemset, int pair_size) {
            itemset.replace("[", "");
            itemset.replace("]", "");
            String[] arr = itemset.split(",");
            ArrayList<String> items = new ArrayList<>();
            for (int i = 0; i < arr.length; i++) {
                arr[i] = arr[i].trim().replace("[", "").replace("]", "");
                items.add(arr[i]);
            }

            ArrayList<String> temp = new ArrayList<>();
            ArrayList<String> subset_of_itemset = new ArrayList<>();
            makeCombination(items, items.size(), 0, pair_size - 1, temp, subset_of_itemset);

            for (int i = 0; i < subset_of_itemset.size(); i++) {
                if (!previousItemSet.contains(subset_of_itemset.get(i))) {
                    // System.out.println("fails");
                    return false;
                }
            }
            // System.out.println("pass");
            subset_of_itemset.clear();
            temp.clear();
            return true;
        }

        /*
         * function to generate combination of size of k of n length arraylist. (a,b,c)
         * => if k = 2 then o/p: (a,b) (a,c) and (b,c)
         */
        private static void makeCombination(ArrayList<String> str_list, int len, int ind, int k, ArrayList<String> temp,
                ArrayList<String> kpairs) {
            if (k == 0) {
                kpairs.add(temp.toString());
                return;
            }

            for (int i = ind; i < len; i++) {
                temp.add(str_list.get(i));
                makeCombination(str_list, len, i + 1, k - 1, temp, kpairs);
                temp.remove(temp.size() - 1);
            }
        }

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString(), ",");
            ArrayList<String> str_list = new ArrayList<String>();
            while (itr.hasMoreTokens()) {
                str_list.add(itr.nextToken().replace(" ", ""));
            }
            // sorting is require
            // so that (a,b) and (b,a) not considered differnt
            // due to sort (b,a) never generated it always generate (a,b)
            str_list.sort((a, b) -> a.compareTo(b));
            int str_list_size = str_list.size();
            Integer pair_size = context.getConfiguration().getInt("iteration", 1);
            ArrayList<String> kpairs = new ArrayList<String>();
            ArrayList<String> temp = new ArrayList<String>();
            makeCombination(str_list, str_list_size, 0, pair_size, temp, kpairs);
            for (String s : kpairs) {
                String st = s;
                if (isValidItemSet(st, pair_size)) {
                    item.set(s);
                    context.write(item, one);
                }
            }
            kpairs.clear();
            temp.clear();
        }
    }

    public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            Integer minimumSupport = context.getConfiguration().getInt("minimumSupport", 10);
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            if (sum > minimumSupport) {
                context.write(key, result);
            }
            return;
        }
    }

    public static void main(String[] args) throws Exception {
        int minimumSupport = Integer.parseInt(args[2]);
        for (int i = 1; i <= 4; i++) {
            Configuration conf = new Configuration();
            conf.setInt("minimumSupport", minimumSupport);
            conf.setInt("iteration", i);
            Job job = Job.getInstance(conf, "Item set iteration:" + i);
            job.setJarByClass(Support.class);
            if (i == 1) {
                job.setMapperClass(TokenizerMapper.class);
            } else {
                job.setMapperClass(KTokenizerMapper.class);
            }
            job.setCombinerClass(IntSumReducer.class);
            job.setReducerClass(IntSumReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(IntWritable.class);
            FileInputFormat.addInputPath(job, new Path(args[0]));
            FileOutputFormat.setOutputPath(job, new Path(args[1] + i));
            boolean isSucess = job.waitForCompletion(true);
            if (isSucess == false) {
                System.err.println(i + " iteration fail");
                break;
            }
        }
    }
}