package com.abilix.explainer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class ByteUtils {
    public static byte[] float2byte(float f) {

        int fbit = Float.floatToIntBits(f);

        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (fbit >> (24 - i * 8));
        }

        int len = b.length;
        byte[] dest = new byte[len];
        System.arraycopy(b, 0, dest, 0, len);
        byte temp;
        for (int i = 0; i < len / 2; ++i) {
            temp = dest[i];
            dest[i] = dest[len - i - 1];
            dest[len - i - 1] = temp;
        }

        return dest;

    }

    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    public static float byte2float(byte[] b, int index) {
        int l;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);

        return Float.intBitsToFloat(l);
    }


    // 低位在后高位在前。
    public static int bytesToInt2(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF) << 24) | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8) | (src[offset + 3] & 0xFF));
        return value;
    }

    public static int byte2int_2byte(byte[] b, int index) {
        int l;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= (0 << 16);
        l &= 0xffffff;
        l |= (0 << 24);
        return l;
    }

    public static int byte2int_2byteHL(byte[] b, int index) {
        int l;
        l = b[index + 1];
        l &= 0xff;
        l |= ((long) b[index + 0] << 8);
        l &= 0xffff;
        l |= (0 << 16);
        l &= 0xffffff;
        l |= (0 << 24);
        return l;
    }

    public static int byteAray2Int(byte[] b) {
        return b[3] & 0xFF | (b[2] & 0XFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
    }

    public static int byteAray2IntLH(byte[] b) {
        return ((b[0] & 0xFF) | ((b[1] << 8) & 0xFF00) | ((b[2] << 16) & 0xFF0000) | ((b[3] << 24) & 0xFF000000));
    }

    public static String bytesToString(byte[] buf, int len) {
        if(buf == null){
            return "null";
        }
        if (LogMgr.getLogLevel() == LogMgr.NOLOG || len > 64) {
            return "len = " + len;
        }
        return bytesToHexString(buf, false);
    }

    public static String bytesToString(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }
        if (LogMgr.getLogLevel() == LogMgr.NOLOG || bytes.length > 64) {
            return "len = " + bytes.length;
        }
        return bytesToString(bytes, bytes.length);
    }

    /**
     * The digits for every supported radix.
     */
    private static final char[] DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'
    };

    private static final char[] UPPER_CASE_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    public static String bytesToHexString(byte[] bytes, boolean upperCase) {
        char[] digits = upperCase ? UPPER_CASE_DIGITS : DIGITS;
        char[] buf = new char[bytes.length * 3];
        int c = 0;
        for (byte b : bytes) {
            buf[c++] = digits[(b >> 4) & 0xf];
            buf[c++] = digits[b & 0xf];
            buf[c++] = ' ';
        }
        return new String(buf);
    }

    public static Long getCRC32(String fileUri) {
        CRC32 crc32 = new CRC32();
        FileInputStream fileinputstream = null;
        CheckedInputStream checkedinputstream = null;
        Long crc = null;
        try {
            fileinputstream = new FileInputStream(new File(fileUri));
            checkedinputstream = new CheckedInputStream(fileinputstream, crc32);
            while (checkedinputstream.read() != -1) {
            }
            // crc = Long.toHexString(crc32.getValue()).toUpperCase();
            crc = crc32.getValue();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileinputstream != null) {
                try {
                    fileinputstream.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            if (checkedinputstream != null) {
                try {
                    checkedinputstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return crc;
    }

    /**
     * 将int数值转换为占两个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。 和bytesToInt（）配套使用
     *
     * @param value 要转换的int值
     * @return byte数组
     */
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[2];
        src[0] = (byte) ((value >> 8) & 0xFF);
        src[1] = (byte) (value & 0xFF);
        return src;
    }


    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。 和bytesToInt（）配套使用
     *
     * @param value 要转换的int值
     * @return byte数组
     */
    public static byte[] intTo4Bytes(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    public static byte[] readFile(String fileName) throws IOException {
        byte[] buf = null;
        try {
            FileInputStream fin = new FileInputStream(new File(fileName));
            //LogMgr.e("readFile(" + fileName + ")  fin.fin.getFD()" + fin.getFD().toString());
            int length = fin.available();
            buf = null;
            buf = new byte[length];
            fin.read(buf);
            fin.close();
        } catch (Exception e) {
            LogMgr.e("read file error::" + e);
            e.printStackTrace();
        }
        return buf;
    }

    public static byte[] floatsToByte(float[] values) {
        byte[] byteX = ByteUtils.float2byte(values[0]);
        byte[] byteY = ByteUtils.float2byte(values[1]);
        byte[] byteZ = ByteUtils.float2byte(values[2]);
        byte[] byte2 = ByteUtils.byteMerger(byteX, byteY);
        byte[] byte3 = ByteUtils.byteMerger(byte2, byteZ);
        return byte3;
    }


    /**
     * 从字节数据指定4字节中获取int
     *
     * @param source 数据源
     * @param start  开始位置
     * @return
     * @throws Exception
     */
    public static int getIntFromByteArray(byte[] source, int start) throws Exception {
        if (start + 4 > source.length) {
            throw new Exception();
        }
        int result;
        result = (int) ((source[start] << 24) | (source[start + 1] << 16) | (source[start + 2] << 8) | (source[start + 3]));
        return result;
    }

    /**
     * 获取现在时间
     *
     * @return 返回时间类型 yyyy-MM-dd HH:mm:ss
     */
    public static String getNowDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
//		ParsePosition pos = new ParsePosition(8);
//		Date currentTime_2 = formatter.parse(dateString, pos);
//		return currentTime_2;
    }

    /**
     * 获取现在时间
     *
     * @return 返回时间类型 MM_dd_HH_mm
     */
    public static String getNowDate1() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM_dd_HH_mm");
        String dateString = formatter.format(currentTime);
        return dateString;
//		ParsePosition pos = new ParsePosition(8);
//		Date currentTime_2 = formatter.parse(dateString, pos);
//		return currentTime_2;
    }
}
