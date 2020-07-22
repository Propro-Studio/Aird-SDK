package com.westlake.aird.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPCompressUtil {

    public static byte[] transToByte(float[] target) {
        FloatBuffer fbTarget = FloatBuffer.wrap(target);
        ByteBuffer bbTarget = ByteBuffer.allocate(fbTarget.capacity() * 4);
        bbTarget.asFloatBuffer().put(fbTarget);
        byte[] compressed = gzipCompress(bbTarget.array());
        return compressed;
    }

    public static float[] transToFloat(byte[] target){
        byte[] decompressed = gzipDecompress(target);
        ByteBuffer byteBuffer = ByteBuffer.wrap(decompressed);
        FloatBuffer floats = byteBuffer.asFloatBuffer();
        float[] floatValues = new float[floats.capacity()];
        for (int i = 0; i < floats.capacity(); i++) {
            floatValues[i] = floats.get(i);
        }

        byteBuffer.clear();
        return floatValues;
    }

    public static byte[] gzipCompress(byte[] target) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try(out;GZIPOutputStream gzip = new GZIPOutputStream(out);) {
            gzip.write(target);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    public static byte[] gzipDecompress(byte[] target) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (out; ByteArrayInputStream in = new ByteArrayInputStream(target); GZIPInputStream ginzip = new GZIPInputStream(in)) {
            byte[] buffer = new byte[1024];
            int offset = -1;
            while ((offset = ginzip.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

}