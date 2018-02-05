package com.example.parse_arsc.type;

/**
 * Created by hzliyongan on 2018/2/5.
 */

public class ResTableHeader {
    public ResChunkHeader header;
    public int packageCount;

    public ResTableHeader(){
        header = new ResChunkHeader();
    }

    public int getHeaderSize(){
        return header.getHeaderSize() + 4;
    }

    @Override
    public String toString(){
        return "header:" + header.toString() + "\n" + "packageCount:" + packageCount + "\n";
    }
}
