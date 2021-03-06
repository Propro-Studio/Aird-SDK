/*
 * Copyright (c) 2020 CSi Biotech
 * Aird and AirdPro are licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package net.csibio.aird.parser;

import net.csibio.aird.bean.BlockIndex;
import net.csibio.aird.bean.MsCycle;
import net.csibio.aird.bean.MzIntensityPairs;
import net.csibio.aird.exception.ScanException;
import net.csibio.aird.util.FileUtil;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * DDA模式的转换器
 * The parser for DDA acquisition mode. The index is group like MS1-MS2 Group
 * DDA reader using the strategy of loading all the information into the memory
 */
public class DDAParser extends BaseParser{

    public DDAParser(String indexFilePath) throws ScanException {
        super(indexFilePath);
    }

    /**
     * DDA文件采用一次性读入内存的策略
     * DDA reader using the strategy of loading all the information into the memory
     *
     * @return DDA文件中的所有信息,以MsCycle的模型进行存储 the mz-intensity pairs read from the aird. And store as MsCycle in the memory
     * @throws Exception exception when reading the file
     */
    public List<MsCycle> parseToMsCycle() throws Exception {
        RandomAccessFile raf = new RandomAccessFile(airdFile.getPath(), "r");
        List<MsCycle> cycleList = new ArrayList<>();
        List<BlockIndex> indexList = getAirdInfo().getIndexList();
        TreeMap<Double, MzIntensityPairs> ms1Map = parseBlockValue(raf, indexList.get(0));
        List<Integer> ms1ScanNumList = indexList.get(0).getNums();
        List<Double> rtList = new ArrayList<>(ms1Map.keySet());

        //将ms2 rt单位转换为分钟
        for (BlockIndex blockIndex : indexList) {
            List<Float> rts = blockIndex.getRts();
            for (int i = 0; i < rts.size(); i++) {
                rts.set(i, rts.get(i) / 60f);
            }
        }

        for (int i = 0; i < rtList.size(); i++) {
            MsCycle tempMsc = new MsCycle();

            //将ms1 rt单位转换为分钟
            tempMsc.setRt(rtList.get(i));
            tempMsc.setMs1Spectrum(ms1Map.get(rtList.get(i)));
            for (int tempBlockNum = 1; tempBlockNum < indexList.size(); tempBlockNum++) {
                BlockIndex tempBlockIndex = indexList.get(tempBlockNum);
                if (tempBlockIndex.getNum().equals(ms1ScanNumList.get(i))) {
                    tempMsc.setRangeList(tempBlockIndex.getRangeList());
                    tempMsc.setRts(tempBlockIndex.getRts());

                    TreeMap<Double, MzIntensityPairs> ms2Map = parseBlockValue(raf, tempBlockIndex);
                    List<MzIntensityPairs> ms2Spectrums = new ArrayList<>(ms2Map.values());
                    tempMsc.setMs2Spectrums(ms2Spectrums);
                    break;
                }
            }
            cycleList.add(tempMsc);
        }
        FileUtil.close(raf);
        return cycleList;
    }
}
