package net.csibio.aird;


import net.csibio.aird.bean.BlockIndex;
import net.csibio.aird.parser.DIAParser;
import net.csibio.aird.util.CompressUtil;
import org.apache.lucene.util.RamUsageEstimator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class testStack {
    public static void main(String[] args) throws IOException {
//        List<File> files = AirdScanUtil.scanIndexFiles("\\\\ProproNas\\ProproNAS\\data\\Aird\\DIA\\ThermoQE");
//        if (files == null || files.size() == 0) {
//            return;
//        }
//        for (File indexFile : files) {
//            System.out.println(indexFile.getAbsolutePath());
//        }whether git is ready.
        String indexFilePath="/Users/jinyinwang/Documents/stackTestData/DIA/HYE110_TTOF6600_64var_lgillet_I160305_001.json";
        DIAParser DIAParser = new DIAParser(indexFilePath);
        List<BlockIndex> swathIndexList = DIAParser.getAirdInfo().getIndexList();
        String[][] record = new String[5][swathIndexList.size() + 1];
        record[0][0] = "sizeOrigin";
        record[1][0] = "size1";
        record[2][0] = "encodeTime1";
        record[3][0] = "size2";
        record[4][0] = "encodeTime2";

        for (int m = 0; m < 10; m++) {
            BlockIndex index = swathIndexList.get(m);
            List<Float> rts = index.getRts();
            int mzNum = rts.size();

            //取出一个block的数组
            List<int[]> mzGroup = new ArrayList<>();
            for (int i = 0; i < mzNum; i++) {
                int[] arr;
                arr = DIAParser.getSpectrumAsInteger(index, rts.get(i)).getMz();
                mzGroup.add(arr);
            }
            record[0][m + 1] = RamUsageEstimator.humanSizeOf(mzGroup);
//            String size1 = RamUsageEstimator.humanSizeOf(mzGroup);
//            System.out.println("原数组：" + size1);

            //计算一代压缩时间
            List<byte[]> comMZs = new LinkedList<>();
            long t1 = 0;
            for (int i = 0; i < mzNum; i++) {
                long tempT = System.currentTimeMillis();
                byte[] comMZ = CompressUtil.transToByte(CompressUtil.fastPforEncoder(mzGroup.get(i)));
                t1 += (System.currentTimeMillis() - tempT);
                comMZs.add(comMZ);
            }
            record[1][m + 1] = RamUsageEstimator.humanSizeOf(comMZs);
            record[2][m + 1] = t1 + "";
//            String size2 = RamUsageEstimator.humanSizeOf(comMZs);
//            System.out.println("一代：" + size2);
//            System.out.println("一代压缩时间：" + t1);

            for (int k = 8; k < 9; k++) {
                long t2 = System.currentTimeMillis();
                int arrNum = (int) Math.pow(2, k);
                int groupNum = (mzNum - 1) / arrNum + 1;
                List<stackData2Rep.Stack> stacks = new LinkedList<>();
                int fromIndex = 0;
                for (int i = 0; i < groupNum - 1; i++) {
                    List<int[]> arrGroup = mzGroup.subList(fromIndex, fromIndex + arrNum);
                    stackData2Rep.Stack stack = stackData2Rep.stackEncode(arrGroup, true);
                    stacks.add(stack);
                    fromIndex += arrNum;
                }
                //处理余数
                List<int[]> arrGroup = mzGroup.subList(fromIndex, mzNum);
                stackData2Rep.Stack stack = stackData2Rep.stackEncode(arrGroup, false);
                stacks.add(stack);
                long t3 = System.currentTimeMillis();
                record[3][m + 1] = RamUsageEstimator.humanSizeOf(stacks);
                record[4][m + 1] = (t3 - t2) + "";
//                String size3 = RamUsageEstimator.humanSizeOf(stacks);
//                System.out.println("二代：" + size3);
//                System.out.println("二代压缩时间：" + (t3 - t2));
                System.out.println("block"+m+" finished!");
            }
        }
        File file = new File(indexFilePath.replace("json","csv"));
        FileWriter out = new FileWriter(file);
        for (int i = 0; i < swathIndexList.size(); i++) {
            for (int j = 0; j < 5; j++) {
                out.write(record[j][i]+",");
            }
            out.write("\r\n");
        }
        out.close();
    }
}
