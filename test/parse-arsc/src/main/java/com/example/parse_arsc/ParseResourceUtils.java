package com.example.parse_arsc;

import com.example.parse_arsc.type.ResChunkHeader;
import com.example.parse_arsc.type.ResStringPoolHeader;
import com.example.parse_arsc.type.ResStringPoolRef;
import com.example.parse_arsc.type.ResTableConfig;
import com.example.parse_arsc.type.ResTableEntry;
import com.example.parse_arsc.type.ResTableHeader;
import com.example.parse_arsc.type.ResTableMap;
import com.example.parse_arsc.type.ResTableMapEntry;
import com.example.parse_arsc.type.ResTablePackage;
import com.example.parse_arsc.type.ResTableRef;
import com.example.parse_arsc.type.ResTableType;
import com.example.parse_arsc.type.ResTableTypeSpec;
import com.example.parse_arsc.type.ResValue;

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

    public static String getKeyString(int index){
        if(index >= keyStringList.size() || index < 0){
            return "";
        }
        return keyStringList.get(index);
    }

    public static String getResString(int index){
        if(index >= resStringList.size() || index < 0){
            return "";
        }
        return resStringList.get(index);
    }

    /**
     * 获取资源id
     * 这里高位是packid，中位是restypeid，地位是entryid
     * @param entryid
     * @return
     */
    public static int getResId(int entryid){
        return (((packId)<<24) | (((resTypeId) & 0xFF)<<16) | (entryid & 0xFFFF));
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

    public static boolean isEnd(int length){
        return resTypeOffset >= length;
    }


    /**
     * 判断是不是类型描述符
     * @param src
     * @return
     */
    public static boolean isTypeSpec(byte[] src){
        ResChunkHeader header = parseResChunkHeader(src, resTypeOffset);
        return header.type == 0x0202;
    }

    /**
     * 解析ResTypeSepc类型描述内容
     * @param src
     */
    public static ResTableTypeSpec parseResTypeSpec(byte[] src){
        ResTableTypeSpec typeSpec = new ResTableTypeSpec();
        //解析头部信息
        typeSpec.header = parseResChunkHeader(src, resTypeOffset);

        int offset = (resTypeOffset + typeSpec.header.getHeaderSize());

        //解析id类型
        byte[] idByte = Utils.copyByte(src, offset, 1);
        typeSpec.id = (byte)(idByte[0] & 0xFF);
        resTypeId = typeSpec.id;
        typeSpec.typeName = typeStringList.get(resTypeId - 1);

        //解析res0字段,这个字段是备用的，始终是0
        byte[] res0Byte = Utils.copyByte(src, offset+1, 1);
        typeSpec.res0 = (byte)(res0Byte[0] & 0xFF);

        //解析res1字段，这个字段是备用的，始终是0
        byte[] res1Byte = Utils.copyByte(src, offset+2, 2);
        typeSpec.res1 = Utils.byte2Short(res1Byte);

        //entry的总个数
        byte[] entryCountByte = Utils.copyByte(src, offset+4, 4);
        typeSpec.entryCount = Utils.byte2int(entryCountByte);

        //获取entryCount个int数组
        int[] intAry = new int[typeSpec.entryCount];
        int intAryOffset = resTypeOffset + typeSpec.header.headerSize;
        for(int i=0;i<typeSpec.entryCount;i++){
            int element = Utils.byte2int(Utils.copyByte(src, intAryOffset+i*4, 4));
            intAry[i] = element;
            typeSpec.entry += (element + ",");
        }
        typeSpec.entry += "\n";
        resTypeOffset += typeSpec.header.size;
        return typeSpec;
    }


    /**
     * 解析类型信息内容
     * @param src
     */
    public static ResTableType parseResTypeInfo(byte[] src){
        ResTableType type = new ResTableType();
        //解析头部信息
        type.header = parseResChunkHeader(src, resTypeOffset);

        int offset = (resTypeOffset + type.header.getHeaderSize());

        //解析type的id值
        byte[] idByte = Utils.copyByte(src, offset, 1);
        type.id = (byte)(idByte[0] & 0xFF);

        //解析res0字段的值，备用字段，始终是0
        byte[] res0 = Utils.copyByte(src, offset+1, 1);
        type.res0 = (byte)(res0[0] & 0xFF);

        //解析res1字段的值，备用字段，始终是0
        byte[] res1 = Utils.copyByte(src, offset+2, 2);
        type.res1 = Utils.byte2Short(res1);

        byte[] entryCountByte = Utils.copyByte(src, offset+4, 4);
        type.entryCount = Utils.byte2int(entryCountByte);

        byte[] entriesStartByte = Utils.copyByte(src, offset+8, 4);
        type.entriesStart = Utils.byte2int(entriesStartByte);

        ResTableConfig resConfig = new ResTableConfig();
        resConfig = parseResTableConfig(Utils.copyByte(src, offset+12, resConfig.getSize()));
        type.resConfig = resConfig;

        //先获取entryCount个int数组
        System.out.print("type int elements:");
        int[] intAry = new int[type.entryCount];
        for(int i=0;i<type.entryCount;i++){
            int element = Utils.byte2int(Utils.copyByte(src, resTypeOffset+type.header.headerSize+i*4, 4));
            intAry[i] = element;
            System.out.print(element+",");
        }
        System.out.println();

        //这里开始解析后面对应的ResEntry和ResValue
        int entryAryOffset = resTypeOffset + type.entriesStart;
        ResTableEntry[] tableEntryAry = new ResTableEntry[type.entryCount];
        ResValue[] resValueAry = new ResValue[type.entryCount];
        System.out.println("entry offset:"+Utils.bytesToHexString(Utils.int2Byte(entryAryOffset)));

        //这里存在一个问题就是如果是ResMapEntry的话，偏移值是不一样的，所以这里需要计算不同的偏移值
        int bodySize = 0, valueOffset = entryAryOffset;
        for(int i=0;i<type.entryCount;i++){
            int resId = getResId(i);
            if (i == 0) {

                System.out.println("resId:"+Utils.bytesToHexString(Utils.int2Byte(resId)));
            }
            ResTableEntry entry = new ResTableEntry();
            ResValue value = new ResValue();
            valueOffset += bodySize;
            if (i == 0) {

                System.out.println("valueOffset:"+Utils.bytesToHexString(Utils.int2Byte(valueOffset)));
            }
            entry = parseResEntry(Utils.copyByte(src, valueOffset, entry.getSize()));

            //这里需要注意的是，先判断entry的flag变量是否为1,如果为1的话，那就ResTable_map_entry
            if(entry.flags == 1){
                //这里是复杂类型的value
                ResTableMapEntry mapEntry = new ResTableMapEntry();
                mapEntry = parseResMapEntry(Utils.copyByte(src, valueOffset, mapEntry.getSize()));
                if (i == 0) {

                    System.out.println("map entry:"+mapEntry);
                }
                ResTableMap resMap = new ResTableMap();
                for(int j=0;j<mapEntry.count;j++){
                    int mapOffset = valueOffset + mapEntry.getSize() + resMap.getSize()*j;
                    resMap = parseResTableMap(Utils.copyByte(src, mapOffset, resMap.getSize()));
                    if (i == 0) {

                        System.out.println("map:"+resMap);
                    }
                }
                bodySize = mapEntry.getSize() + resMap.getSize()*mapEntry.count;
            }else{
                if (i == 0) {

                    System.out.println("entry:"+entry);
                }
                //这里是简单的类型的value
                value = parseResValue(Utils.copyByte(src, valueOffset+entry.getSize(), value.getSize()));
                if (i == 0) {

                    System.out.println("value:"+value);
                }
                bodySize = entry.getSize()+value.getSize();
            }

            tableEntryAry[i] = entry;
            resValueAry[i] = value;

            if (i == 0) {

                System.out.println("======================================");
            }
        }

        resTypeOffset += type.header.size;
        return type;
    }

    /**
     * 解析ResTableConfig配置信息
     * @param src
     * @return
     */
    public static ResTableConfig parseResTableConfig(byte[] src){
        ResTableConfig config = new ResTableConfig();

        byte[] sizeByte = Utils.copyByte(src, 0, 4);
        config.size = Utils.byte2int(sizeByte);

        //以下结构是Union
        byte[] mccByte = Utils.copyByte(src, 4, 2);
        config.mcc = Utils.byte2Short(mccByte);
        byte[] mncByte = Utils.copyByte(src, 6, 2);
        config.mnc = Utils.byte2Short(mncByte);
        byte[] imsiByte = Utils.copyByte(src, 4, 4);
        config.imsi = Utils.byte2int(imsiByte);

        //以下结构是Union
        byte[] languageByte = Utils.copyByte(src, 8, 2);
        config.language = languageByte;
        byte[] countryByte = Utils.copyByte(src, 10, 2);
        config.country = countryByte;
        byte[] localeByte = Utils.copyByte(src, 8, 4);
        config.locale = Utils.byte2int(localeByte);

        //以下结构是Union
        byte[] orientationByte = Utils.copyByte(src, 12, 1);
        config.orientation = orientationByte[0];
        byte[] touchscreenByte = Utils.copyByte(src, 13, 1);
        config.touchscreen = touchscreenByte[0];
        byte[] densityByte = Utils.copyByte(src, 14, 2);
        config.density = Utils.byte2Short(densityByte);
        byte[] screenTypeByte = Utils.copyByte(src, 12, 4);
        config.screenType = Utils.byte2int(screenTypeByte);

        //以下结构是Union
        byte[] keyboardByte = Utils.copyByte(src, 16, 1);
        config.keyboard = keyboardByte[0];
        byte[] navigationByte = Utils.copyByte(src, 17, 1);
        config.navigation = navigationByte[0];
        byte[] inputFlagsByte = Utils.copyByte(src, 18, 1);
        config.inputFlags = inputFlagsByte[0];
        byte[] inputPad0Byte = Utils.copyByte(src, 19, 1);
        config.inputPad0 = inputPad0Byte[0];
        byte[] inputByte = Utils.copyByte(src, 16, 4);
        config.input = Utils.byte2int(inputByte);

        //以下结构是Union
        byte[] screenWidthByte = Utils.copyByte(src, 20, 2);
        config.screenWidth = Utils.byte2Short(screenWidthByte);
        byte[] screenHeightByte = Utils.copyByte(src, 22, 2);
        config.screenHeight = Utils.byte2Short(screenHeightByte);
        byte[] screenSizeByte = Utils.copyByte(src, 20, 4);
        config.screenSize = Utils.byte2int(screenSizeByte);

        //以下结构是Union
        byte[] sdVersionByte = Utils.copyByte(src, 24, 2);
        config.sdVersion = Utils.byte2Short(sdVersionByte);
        byte[] minorVersionByte = Utils.copyByte(src, 26, 2);
        config.minorVersion = Utils.byte2Short(minorVersionByte);
        byte[] versionByte = Utils.copyByte(src, 24, 4);
        config.version = Utils.byte2int(versionByte);

        //以下结构是Union
        byte[] screenLayoutByte = Utils.copyByte(src, 28, 1);
        config.screenLayout = screenLayoutByte[0];
        byte[] uiModeByte = Utils.copyByte(src, 29, 1);
        config.uiMode = uiModeByte[0];
        byte[] smallestScreenWidthDpByte = Utils.copyByte(src, 30, 2);
        config.smallestScreenWidthDp = Utils.byte2Short(smallestScreenWidthDpByte);
        byte[] screenConfigByte = Utils.copyByte(src, 28, 4);
        config.screenConfig = Utils.byte2int(screenConfigByte);

        //以下结构是Union
        byte[] screenWidthDpByte = Utils.copyByte(src, 32, 2);
        config.screenWidthDp = Utils.byte2Short(screenWidthDpByte);
        byte[] screenHeightDpByte = Utils.copyByte(src, 34, 2);
        config.screenHeightDp = Utils.byte2Short(screenHeightDpByte);
        byte[] screenSizeDpByte = Utils.copyByte(src, 32, 4);
        config.screenSizeDp = Utils.byte2int(screenSizeDpByte);

        byte[] localeScriptByte = Utils.copyByte(src, 36, 4);
        config.localeScript = localeScriptByte;

        byte[] localeVariantByte = Utils.copyByte(src, 40, 8);
        config.localeVariant = localeVariantByte;
        return config;
    }

    /**
     * 解析ResEntry内容
     * @param src
     * @return
     */
    public static ResTableEntry parseResEntry(byte[] src){
        ResTableEntry entry = new ResTableEntry();

        byte[] sizeByte = Utils.copyByte(src, 0, 2);
        entry.size = Utils.byte2Short(sizeByte);

        byte[] flagByte = Utils.copyByte(src, 2, 2);
        entry.flags = Utils.byte2Short(flagByte);

        ResStringPoolRef key = new ResStringPoolRef();
        byte[] keyByte = Utils.copyByte(src, 4, 4);
        key.index = Utils.byte2int(keyByte);
        entry.key = key;

        return entry;
    }

    /**
     * 解析ResMapEntry内容
     * @param src
     * @return
     */
    public static ResTableMapEntry parseResMapEntry(byte[] src){
        ResTableMapEntry entry = new ResTableMapEntry();

        byte[] sizeByte = Utils.copyByte(src, 0, 2);
        entry.size = Utils.byte2Short(sizeByte);

        byte[] flagByte = Utils.copyByte(src, 2, 2);
        entry.flags = Utils.byte2Short(flagByte);

        ResStringPoolRef key = new ResStringPoolRef();
        byte[] keyByte = Utils.copyByte(src, 4, 4);
        key.index = Utils.byte2int(keyByte);
        entry.key = key;

        ResTableRef ref = new ResTableRef();
        byte[] identByte = Utils.copyByte(src, 8, 4);
        ref.ident = Utils.byte2int(identByte);
        entry.parent = ref;
        byte[] countByte = Utils.copyByte(src, 12, 4);
        entry.count = Utils.byte2int(countByte);

        return entry;
    }

    /**
     * 解析ResTableMap内容
     * @param src
     * @return
     */
    public static ResTableMap parseResTableMap(byte[] src){
        ResTableMap tableMap = new ResTableMap();

        ResTableRef ref = new ResTableRef();
        byte[] identByte = Utils.copyByte(src, 0, ref.getSize());
        ref.ident = Utils.byte2int(identByte);
        tableMap.name = ref;

        ResValue value = new ResValue();
        value = parseResValue(Utils.copyByte(src, ref.getSize(), value.getSize()));
        tableMap.value = value;

        return tableMap;

    }

    /**
     * 解析ResValue内容
     * @param src
     * @return
     */
    public static ResValue parseResValue(byte[] src){
        ResValue resValue = new ResValue();
        byte[] sizeByte = Utils.copyByte(src, 0, 2);
        resValue.size = Utils.byte2Short(sizeByte);

        byte[] res0Byte = Utils.copyByte(src, 2, 1);
        resValue.res0 = (byte)(res0Byte[0] & 0xFF);

        byte[] dataType = Utils.copyByte(src, 3, 1);
        resValue.dataType = (byte)(dataType[0] & 0xFF);

        byte[] data = Utils.copyByte(src, 4, 4);
        resValue.data = Utils.byte2int(data);

        return resValue;
    }
}
