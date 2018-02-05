package com.example.parse_arsc;

import com.example.parse_arsc.type.ResChunkHeader;
import com.example.parse_arsc.type.ResStringPoolHeader;
import com.example.parse_arsc.type.ResTableHeader;

import java.util.ArrayList;

/**
 * Created by hzliyongan on 2018/2/5.
 */

public class ParseResourceUtils {
    private static int resStringPoolChunkOffset;//字符串池的偏移值
    private static int packageChunkOffset;//包内容的偏移值
    private static int keyStringPoolChunkOffset;//key字符串池的偏移值
    private static int typeStringPoolChunkOffset;//类型字符串池的偏移值

    private static ArrayList<String> resStringList = new ArrayList<>();//所有的字符串池

    public static ResTableHeader parseResTableHeaderChunk(byte[] src){
        ResTableHeader resTableHeader = new ResTableHeader();

        resTableHeader.header = parseResChunkHeader(src, 0);

        resStringPoolChunkOffset = resTableHeader.header.headerSize;

        byte[] packageCountByte = Utils.copyByte(src, resTableHeader.header.getHeaderSize(), 4);
        resTableHeader.packageCount = Utils.byte2int(packageCountByte);
        return resTableHeader;
    }

    private static ResChunkHeader parseResChunkHeader(byte[] src, int start){

        ResChunkHeader header = new ResChunkHeader();

        byte[] typeByte = Utils.copyByte(src, start, 2);
        header.type = Utils.byte2Short(typeByte);

        byte[] headerSizeByte = Utils.copyByte(src, start+2, 2);
        header.headerSize = Utils.byte2Short(headerSizeByte);

        byte[] tableSizeByte = Utils.copyByte(src, start+4, 4);
        header.size = Utils.byte2int(tableSizeByte);

        return header;
    }

    /**
     * 解析Resource.arsc文件中所有字符串内容
     * @param src
     */
    public static ResStringPoolHeader parseResStringPoolChunk(byte[] src){
        ResStringPoolHeader stringPoolHeader = parseStringPoolChunk(src, resStringList, resStringPoolChunkOffset);
        packageChunkOffset = resStringPoolChunkOffset + stringPoolHeader.header.size;
        return stringPoolHeader;
    }

    public static String getResStringPoolStrings() {
        StringBuilder stringBuilder = new StringBuilder("res string pool's strings:\n");
        for (String s: resStringList) {
            stringBuilder.append(s);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    private static ResStringPoolHeader parseStringPoolChunk(byte[] src, ArrayList<String> stringList, int stringOffset){
        ResStringPoolHeader stringPoolHeader = new ResStringPoolHeader();

        //解析头部信息
        stringPoolHeader.header = parseResChunkHeader(src, stringOffset);

        int offset = stringOffset + stringPoolHeader.header.getHeaderSize();

        //获取字符串的个数
        byte[] stringCountByte = Utils.copyByte(src, offset, 4);
        stringPoolHeader.stringCount = Utils.byte2int(stringCountByte);

        //解析样式的个数
        byte[] styleCountByte = Utils.copyByte(src, offset+4, 4);
        stringPoolHeader.styleCount = Utils.byte2int(styleCountByte);

        //这里表示字符串的格式:UTF-8/UTF-16
        byte[] flagByte = Utils.copyByte(src, offset+8, 4);
        stringPoolHeader.flags = Utils.byte2int(flagByte);

        //字符串内容的开始位置
        byte[] stringStartByte = Utils.copyByte(src, offset+12, 4);
        stringPoolHeader.stringsStart = Utils.byte2int(stringStartByte);

        //样式内容的开始位置
        byte[] sytleStartByte = Utils.copyByte(src, offset+16, 4);
        stringPoolHeader.stylesStart = Utils.byte2int(sytleStartByte);

        //获取字符串内容的索引数组和样式内容的索引数组
        int[] stringIndexAry = new int[stringPoolHeader.stringCount];
        int[] styleIndexAry = new int[stringPoolHeader.styleCount];

        int stringIndex = offset + 20;
        for(int i=0;i<stringPoolHeader.stringCount;i++){
            stringIndexAry[i] = Utils.byte2int(Utils.copyByte(src, stringIndex+i*4, 4));
        }

        int styleIndex = stringIndex + 4*stringPoolHeader.stringCount;
        for(int i=0;i<stringPoolHeader.styleCount;i++){
            styleIndexAry[i] = Utils.byte2int(Utils.copyByte(src,  styleIndex+i*4, 4));
        }

        //每个字符串的头两个字节的最后一个字节是字符串的长度
        //这里获取所有字符串的内容
        int stringContentIndex = styleIndex + stringPoolHeader.styleCount*4;
        int index = 0;
        while(index < stringPoolHeader.stringCount){
            byte[] stringSizeByte = Utils.copyByte(src, stringContentIndex, 2);
            int stringSize = (stringSizeByte[1] & 0x7F);
            if(stringSize != 0){
                String val = "";
                try{
                    val = new String(Utils.copyByte(src, stringContentIndex+2, stringSize), "utf-8");
                }catch(Exception e){
                    System.out.println("string encode error:"+e.toString());
                }
                stringList.add(val);
            }else{
                stringList.add("");
            }
            stringContentIndex += (stringSize+3);
            index++;
        }
        return stringPoolHeader;

    }
}
