package com.example.parse_arsc.type;

/**
 * Created by hzliyongan on 2018/2/7.
 */

public class ResTableType {
    public ResChunkHeader header;

    public final static int NO_ENTRY = 0xFFFFFFFF;

    public byte id;
    public byte res0;
    public short res1;
    public int entryCount;
    public int entriesStart;

    public ResTableConfig resConfig;

    public ResTableType(){
        header = new ResChunkHeader();
        resConfig = new ResTableConfig();
    }

    public int getSize(){
        return header.getHeaderSize() + 1 + 1 + 2 + 4 + 4;
    }

    @Override
    public String toString(){
        return "header:"+header.toString()+"\nid:"+id+"\nres0:"+res0+"\nres1:"+res1+"\nentryCount:"+entryCount+"\nentriesStart:"+entriesStart+"\n" + resConfig + "\n";
    }

}
