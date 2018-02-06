package com.example.parse_arsc;

import com.example.parse_arsc.type.ResChunkHeader;
import com.example.parse_arsc.type.ResStringPoolHeader;
import com.example.parse_arsc.type.ResTableHeader;
import com.example.parse_arsc.type.ResTablePackage;

import java.util.ArrayList;

/**
 * Created by hzliyongan on 2018/2/5.
 */

public class ParseResourceUtils {
    private static int resStringPoolChunkOffset;//字符串池的偏移值
    private static int packageChunkOffset;//包内容的偏移值
    private static int keyStringPoolChunkOffset;//key字符串池的偏移值
    private static int typeStringPoolChunkOffset;//类型字符串池的偏移值
    private static int resTypeOffset;//解析资源的类型的偏移值

    //资源包的id和类型id
    private static int packId;
    private static int resTypeId;

    private static ArrayList<String> resStringList = new ArrayList<String>();//所有的字符串池
    private static ArrayList<String> keyStringList = new ArrayList<String>();//所有的资源key的值的池
    private static ArrayList<String> typeStringList = new ArrayList<String>();//所有类型的值的池

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

    public static String getTypeStringPoolStrings() {
        StringBuilder stringBuilder = new StringBuilder("resource type string pool's strings:\n");
        for (String s: typeStringList) {
            stringBuilder.append(s);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public static String getKeyStringPoolStrings() {
        StringBuilder stringBuilder = new StringBuilder("resource key string pool's strings:\n");
        for (String s: keyStringList) {
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

    /**
     * 解析Package信息
     * @param src
     */
    public static ResTablePackage parsePackage(byte[] src){
        ResTablePackage resTabPackage = new ResTablePackage();
        //解析头部信息
        resTabPackage.header = parseResChunkHeader(src, packageChunkOffset);

        int offset = packageChunkOffset + resTabPackage.header.getHeaderSize();

        //解析packId
        byte[] idByte = Utils.copyByte(src, offset, 4);
        resTabPackage.id = Utils.byte2int(idByte);
        packId = resTabPackage.id;

        //解析包名
        byte[] nameByte = Utils.copyByte(src, offset+4, 128*2);//这里的128是这个字段的大小，可以查看类型说明，是char类型的，所以要乘以2
        String packageName = new String(nameByte);
        packageName = Utils.filterStringNull(packageName);
        resTabPackage.packageName = packageName;

        //解析类型字符串的偏移值
        byte[] typeStringsByte = Utils.copyByte(src, offset+4+128*2, 4);
        resTabPackage.typeStrings = Utils.byte2int(typeStringsByte);

        //解析lastPublicType字段
        byte[] lastPublicType = Utils.copyByte(src, offset+8+128*2, 4);
        resTabPackage.lastPublicType = Utils.byte2int(lastPublicType);

        //解析keyString字符串的偏移值
        byte[] keyStrings = Utils.copyByte(src, offset+12+128*2, 4);
        resTabPackage.keyStrings = Utils.byte2int(keyStrings);
        System.out.println("keyString:"+resTabPackage.keyStrings);

        //解析lastPublicKey
        byte[] lastPublicKey = Utils.copyByte(src, offset+12+128*2, 4);
        resTabPackage.lastPublicKey = Utils.byte2int(lastPublicKey);

        //这里获取类型字符串的偏移值和类型字符串的偏移值
        keyStringPoolChunkOffset = (packageChunkOffset+resTabPackage.keyStrings);
        typeStringPoolChunkOffset = (packageChunkOffset+resTabPackage.typeStrings);
        return resTabPackage;
    }

    /**
     * 解析类型字符串内容
     * @param src
     */
    public static ResStringPoolHeader parseTypeStringPoolChunk(byte[] src){
        return parseStringPoolChunk(src, typeStringList, typeStringPoolChunkOffset);
    }

    /**
     * 解析key字符串内容
     * @param src
     */
    public static ResStringPoolHeader parseKeyStringPoolChunk(byte[] src){
        ResStringPoolHeader stringPoolHeader  = parseStringPoolChunk(src, keyStringList, keyStringPoolChunkOffset);
        //解析完key字符串之后，需要赋值给resType的偏移值,后续还需要继续解析
        resTypeOffset = (keyStringPoolChunkOffset+stringPoolHeader.header.size);
        return stringPoolHeader;
    }
}
