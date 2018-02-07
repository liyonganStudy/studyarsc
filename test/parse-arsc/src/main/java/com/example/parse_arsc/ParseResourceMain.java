package com.example.parse_arsc;

import com.example.parse_arsc.type.ResTableHeader;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

public class ParseResourceMain{

    public static String parseResourceFile(InputStream inputStream) {
        byte[] srcByte = null;
        ByteArrayOutputStream bos = null;
        try{
            bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while((len=inputStream.read(buffer)) != -1){
                bos.write(buffer, 0, len);
            }
            srcByte = bos.toByteArray();
        }catch(Exception e){
            System.out.println("read res file error:"+e.toString());
        }finally{
            try{
                inputStream.close();
                bos.close();
            }catch(Exception e){
                System.out.println("close file error:"+e.toString());
            }
        }

        if(srcByte == null){
            System.out.println("get src error...");
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder("parse restable header...\n");
        ResTableHeader resTableHeader = ParseResourceUtils.parseResTableHeaderChunk(srcByte);
        stringBuilder.append(resTableHeader.toString());
        stringBuilder.append("++++++++++++++++++++++++++++++++++++++\n");

        stringBuilder.append("parse resstring pool chunk...\n");
        stringBuilder.append(ParseResourceUtils.parseResStringPoolChunk(srcByte).toString());
        stringBuilder.append("++++++++++++++++++++++++++++++++++++++\n");

        stringBuilder.append("parse package chunk...\n");
        stringBuilder.append(ParseResourceUtils.parsePackage(srcByte).toString());
        stringBuilder.append("++++++++++++++++++++++++++++++++++++++\n");

        stringBuilder.append("parse typestring pool chunk...\n");
        stringBuilder.append(ParseResourceUtils.parseTypeStringPoolChunk(srcByte).toString());
        stringBuilder.append("++++++++++++++++++++++++++++++++++++++\n");

        stringBuilder.append("parse keystring pool chunk...\n");
        stringBuilder.append(ParseResourceUtils.parseKeyStringPoolChunk(srcByte).toString());
        stringBuilder.append("++++++++++++++++++++++++++++++++++++++\n");

        int resCount = 0;
        while(!ParseResourceUtils.isEnd(srcByte.length)){
            resCount++;
            boolean isSpec = ParseResourceUtils.isTypeSpec(srcByte);
            if(isSpec){
                stringBuilder.append("parse restype spec chunk...");
                stringBuilder.append(ParseResourceUtils.parseResTypeSpec(srcByte).toString());
                stringBuilder.append("++++++++++++++++++++++++++++++++++++++\n");
            }else{
                stringBuilder.append("parse restype info chunk...");
                stringBuilder.append(ParseResourceUtils.parseResTypeInfo(srcByte).toString());
                stringBuilder.append("++++++++++++++++++++++++++++++++++++++\n");
            }
        }
        stringBuilder.append("res count:");
        stringBuilder.append(resCount);

        return stringBuilder.toString();
    }

    public static String showStringPoolStrings() {
        return ParseResourceUtils.getResStringPoolStrings();
    }

    public static String showTypePoolStrings() {
        return ParseResourceUtils.getTypeStringPoolStrings();
    }

    public static String showKeyPoolStrings() {
        return ParseResourceUtils.getKeyStringPoolStrings();
    }
}
