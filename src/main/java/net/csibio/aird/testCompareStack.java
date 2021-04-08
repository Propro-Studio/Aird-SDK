package net.csibio.aird;

import net.csibio.aird.bean.BlockIndex;
import net.csibio.aird.parser.DIAParser;
import net.csibio.aird.util.CompressUtil;
import org.apache.lucene.util.RamUsageEstimator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class testCompareStack {
    public static void main(String[] args) throws IOException {
        String indexFilePath = "/Users/jinyinwang/Documents/stackTestData/DIA/HYE110_TTOF6600_64var_lgillet_I160305_001.json";
        DIAParser DIAParser = new DIAParser(indexFilePath);
        List<BlockIndex> swathIndexList = DIAParser.getAirdInfo().getIndexList();
        long sizeOrigin = 0;
        long tAird1 = 0;
        long sizeAird1 = 0;
        long[][] recordSize = new long[9][swathIndexList.size()];//记录2的幂次堆叠每个block的size
        long[][] recordEncodeTime = new long[9][swathIndexList.size()];//记录2的幂次堆叠每个block的EncodeTime

        for (int m = 0; m < swathIndexList.size(); m++) {
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
            sizeOrigin += RamUsageEstimator.sizeOf(mzGroup);
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
            sizeAird1 = RamUsageEstimator.sizeOf(comMZs);
            tAird1 = t1;
//            String size2 = RamUsageEstimator.humanSizeOf(comMZs);
//            System.out.println("一代：" + size2);
//            System.out.println("一代压缩时间：" + t1);
            for (int k = 2; k < 11; k++) {
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
                recordSize[k - 2][m] = RamUsageEstimator.sizeOf(stacks);
                recordEncodeTime[k - 2][m] = (t3 - t2);
//                String size3 = RamUsageEstimator.humanSizeOf(stacks);
//                System.out.println("二代：" + size3);
//                System.out.println("二代压缩时间：" + (t3 - t2));
                System.out.println("block" + m + " finished!");
            }
        }

        File file = new File(indexFilePath.replace("json", "csv"));
        FileWriter out = new FileWriter(file);
        out.write("sizeOrigin" + "," + RamUsageEstimator.humanReadableUnits(sizeOrigin) + "\r\n");
        out.write("k" + "," + "size" + "," + "encodeTime" + "\r\n");
        out.write("0" + "," + RamUsageEstimator.humanReadableUnits(sizeAird1) + ",");
        out.write(Long.toString(tAird1));
        for (int i = 0; i < 9; i++) {
            out.write("\r\n");
            long stackSize = 0;
            long stackTime = 0;
            for (int j = 0; j < swathIndexList.size(); j++) {
                stackSize += recordSize[i][j];
                stackTime += recordEncodeTime[i][j];
            }
            out.write((i + 2) + ",");
            out.write(RamUsageEstimator.humanReadableUnits(stackSize) + ",");
            out.write(Long.toString(stackTime));
        }
        out.close();

    }
}
