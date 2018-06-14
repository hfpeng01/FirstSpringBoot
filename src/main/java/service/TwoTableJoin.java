package service;

import Entity.InfoField;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by LiPeng on 18/5/25.
 */
public class TwoTableJoin {
    public static class MapperClass extends
            Mapper<LongWritable, Text, Text, Text> {

        Configuration config = null;
        HashSet<String> idSet = new HashSet<String>();
        HashMap<String, String> cityIdNameMap = new HashMap<String, String>();
        Map<String, String> houseTypeMap = new HashMap<String, String>();

        public void setup(Mapper.Context context) {
            config = context.getConfiguration();
            if (config == null)
                return;
            String idStr = config.get("idStr");
            String[] idArr = idStr.split(",");
            for (String id : idArr) {
                idSet.add(id);
            }

            String cityIdNameStr = config.get("cityIdNameStr");
            String[] cityIdNameArr = cityIdNameStr.split(",");
            for (String cityIdName : cityIdNameArr) {
                cityIdNameMap.put(cityIdName.split("\t")[0],
                        cityIdName.split("\t")[1]);
            }

            houseTypeMap.put("8", "Test");

        }

        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            String[] info = value.toString().split("|");
            String insertDate = info[InfoField.InsertDate].split(" ")[0]
                    .split("-")[0]; // date: 2012-10-01
            insertDate = insertDate
                    + info[InfoField.InsertDate].split(" ")[0].split("-")[1]; // date:201210

            String userID = info[InfoField.UserID]; // userid
            if (!idSet.contains(userID)) {
                return;
            }

            String disLocalID = "";
            String[] disLocalIDArr = info[InfoField.DisLocalID].split(",");
            if (disLocalIDArr.length >= 2) {
                disLocalID = disLocalIDArr[1];
            } else {
                try {
                    disLocalID = disLocalIDArr[0];
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            String localValue = cityIdNameMap.get(disLocalID);
            disLocalID = localValue == null ? disLocalID : localValue; // city

            String[] cateIdArr = info[InfoField.CateID].split(",");
            String cateId = "";
            String secondType = "";
            if (cateIdArr.length >= 3) {
                cateId = cateIdArr[2];
                if (houseTypeMap.get(cateId) != null) {
                    secondType = houseTypeMap.get(cateId); // secondType
                } else {
                    return;
                }
            } else {
                return;
            }

            String upType = info[InfoField.UpType];
            String outKey = insertDate + "_" + userID + "_" + disLocalID + "_"
                    + secondType;
            String outValue = upType.equals("0") ? "1_1" : "1_0";
            context.write(new Text(outKey), new Text(outValue));
        }
    }

    public static class ReducerClass extends
            Reducer<Text, Text, NullWritable, Text> {

        public void reduce(Text key, Iterable<Text> values, Reducer.Context context)
                throws IOException, InterruptedException {
            int pv = 0;
            int uv = 0;

            for (Text val : values) {
                String[] tmpArr = val.toString().split("_");
                pv += Integer.parseInt(tmpArr[0]);
                uv += Integer.parseInt(tmpArr[1]);
            }

            String outValue = key + "_" + pv + "_" + uv;
            context.write(NullWritable.get(), new Text(outValue));

        }
    }

    public String getResource(String fileFullName) throws IOException {
        // 返回读取指定资源的输入流
        InputStream is = this.getClass().getResourceAsStream(fileFullName);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s = "";
        String res = "";
        while ((s = br.readLine()) != null)
            res = res.equals("") ? s : res + "," + s;
        return res;
    }

    public static void main(String[] args) throws IOException,
            InterruptedException, ClassNotFoundException {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args)
                .getRemainingArgs();
        if (otherArgs.length != 2) {
            System.exit(2);
        }

        String idStr = new TwoTableJoin().getResource("userIDList.txt");
        String cityIdNameStr = new TwoTableJoin().getResource("cityIdName.txt");
        conf.set("idStr", idStr);
        conf.set("cityIdNameStr", cityIdNameStr);
        Job job = new Job(conf, "test01");
        // job.setInputFormatClass(TextInputFormat.class);
        job.setJarByClass(TwoTableJoin.class);
        job.setMapperClass(TwoTableJoin.MapperClass.class);
        job.setReducerClass(TwoTableJoin.ReducerClass.class);
        job.setNumReduceTasks(25);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.setOutputPath(
                job, new Path(otherArgs[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
