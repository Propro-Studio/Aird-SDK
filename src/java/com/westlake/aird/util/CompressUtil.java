
/*
 * Copyright (c) 2020 Propro Studio
 * Aird and AirdPro are licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.westlake.aird.util;

import com.westlake.aird.bean.Compressor;
import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.differential.IntegratedBinaryPacking;
import me.lemire.integercompression.differential.IntegratedVariableByte;
import me.lemire.integercompression.differential.SkippableIntegratedComposition;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressUtil {

    public static byte[] zlibDecoder(byte[] data) {
        byte[] output = null;

        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data);

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[2048];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        decompresser.end();
        return output;
    }

    public static int[] fastPForDecoder(int[] compressedInts) {
        SkippableIntegratedComposition codec = new SkippableIntegratedComposition(new IntegratedBinaryPacking(), new IntegratedVariableByte());
        int size = compressedInts[0];
        // output vector should be large enough...
        int[] recovered = new int[size];
        IntWrapper inPoso = new IntWrapper(1);
        IntWrapper outPoso = new IntWrapper(0);
        IntWrapper recoffset = new IntWrapper(0);
        codec.headlessUncompress(compressedInts, inPoso, compressedInts.length, recovered, recoffset, size, outPoso);

        return recovered;
    }

    public static int[] fastPForEncoder(int[] sortedInt) {
        SkippableIntegratedComposition codec = new SkippableIntegratedComposition(new IntegratedBinaryPacking(), new IntegratedVariableByte());
        int[] compressed = new int[sortedInt.length + 1024];
        IntWrapper inputoffset = new IntWrapper(0);
        IntWrapper outputoffset = new IntWrapper(1);
        codec.headlessCompress(sortedInt, inputoffset, sortedInt.length, compressed, outputoffset, new IntWrapper(0));
        compressed[0] = sortedInt.length;
        compressed = Arrays.copyOf(compressed, outputoffset.intValue());

        return compressed;
    }

    public static Compressor getMzCompressor(List<Compressor> compressors){
        if(compressors == null){
            return null;
        }
        for(Compressor compressor : compressors){
            if (compressor.getTarget().equals(Compressor.TARGET_MZ)){
                return compressor;
            }
        }
        return null;
    }

    public static Compressor getIntCompressor(List<Compressor> compressors){
        if(compressors == null){
            return null;
        }
        for(Compressor compressor : compressors){
            if (compressor.getTarget().equals(Compressor.TARGET_INTENSITY)){
                return compressor;
            }
        }
        return null;
    }

    //byte[]压缩为byte[]
    public static byte[] zlibCompress(byte[] data) {
        byte[] output;

        Deflater compresser = new Deflater();

        compresser.reset();
        compresser.setInput(data);
        compresser.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!compresser.finished()) {
                int i = compresser.deflate(buf);
                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        compresser.end();
        return output;
    }

    public static byte[] zlibDecompress(byte[] data) {
        byte[] output = null;

        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data);

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[2048];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        decompresser.end();
        return output;
    }

    public static String transToString(float[] target) {
        FloatBuffer fbTarget = FloatBuffer.wrap(target);
        ByteBuffer bbTarget = ByteBuffer.allocate(fbTarget.capacity() * 4);
        bbTarget.asFloatBuffer().put(fbTarget);
        byte[] targetArray = bbTarget.array();
        byte[] compressedArray = CompressUtil.zlibCompress(targetArray);
        String targetStr = new String(new Base64().encode(compressedArray));
        return targetStr;
    }

    public static String transToString(int[] target) {
        IntBuffer ibTarget = IntBuffer.wrap(target);
        ByteBuffer bbTarget = ByteBuffer.allocate(ibTarget.capacity() * 4);
        bbTarget.asIntBuffer().put(ibTarget);
        byte[] targetArray = bbTarget.array();
        byte[] compressedArray = CompressUtil.zlibCompress(targetArray);
        String targetStr = new String(new Base64().encode(compressedArray));
        return targetStr;
    }

    public static byte[] transToByte(int[] target) {
        IntBuffer ibTarget = IntBuffer.wrap(target);
        ByteBuffer bbTarget = ByteBuffer.allocate(ibTarget.capacity() * 4);
        bbTarget.asIntBuffer().put(ibTarget);
        byte[] targetArray = bbTarget.array();
        byte[] compressedArray = CompressUtil.zlibCompress(targetArray);
        return compressedArray;
    }

    public static byte[] transToByte(float[] target) {
        FloatBuffer fbTarget = FloatBuffer.wrap(target);
        ByteBuffer bbTarget = ByteBuffer.allocate(fbTarget.capacity() * 4);
        bbTarget.asFloatBuffer().put(fbTarget);
        byte[] targetArray = bbTarget.array();
        byte[] compressedArray = CompressUtil.zlibCompress(targetArray);
        return compressedArray;
    }

    public static float[] transToFloat(byte[] value){
        ByteBuffer byteBuffer = ByteBuffer.wrap(value);
        byteBuffer = ByteBuffer.wrap(CompressUtil.zlibDecompress(byteBuffer.array()));

        FloatBuffer floats = byteBuffer.asFloatBuffer();
        float[] floatValues = new float[floats.capacity()];
        for (int i = 0; i < floats.capacity(); i++) {
            floatValues[i] = floats.get(i);
        }

        byteBuffer.clear();
        return floatValues;
    }


}
