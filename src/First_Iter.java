import java.io.IOException;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class First_Iter {

    public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text item = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString(), ",");
            while (itr.hasMoreTokens()) {
                item.set(itr.nextToken());
                context.write(item, one);
            }
        }
    }

    public static class KTokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text item = new Text();
        private static ArrayList<String> kpairs;

        private static void makeCombination(ArrayList<String> str_list, int len, int ind, int k,
                ArrayList<String> temp) {
            if (k == 0) {
                kpairs.add(temp.toString());
                return;
            }

            for (int i = ind; i < len; i++) {
                temp.add(str_list.get(i));
                makeCombination(str_list, len, i + 1, k - 1, temp);
                temp.remove(temp.size() - 1);
            }
        }

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString(), ",");
            ArrayList<String> str_list = new ArrayList<String>();
            while (itr.hasMoreTokens()) {
                str_list.add(itr.nextToken().replace(" ", ""));
            }
            str_list.sort((a, b) -> a.compareTo(b));

            int str_list_size = str_list.size();
            Integer pair_size = context.getConfiguration().getInt("iteration", 1);
            kpairs = new ArrayList<String>();
            ArrayList<String> temp = new ArrayList<String>();

            makeCombination(str_list, str_list_size, 0, pair_size, temp);

            for (String s : kpairs) {
                item.set(s);
                context.write(item, one);
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
            job.setJarByClass(First_Iter.class);
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