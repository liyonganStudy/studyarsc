package com.example.parse_arsc.type;

/**
 * Created by hzliyongan on 2018/2/6.
 */

public class ResTableTypeSpec {

    public final static int SPEC_PUBLIC = 0x40000000;

    public ResChunkHeader header;
    public byte id;
    public byte res0;
    public short res1;
    public int entryCount;
    public String typeName;
    public String entry = "";

    public ResTableTypeSpec(){
        header = new ResChunkHeader();
    }

    @Override
    public String toString(){
        return "header:"+header.toString()+"\nid:"+id+"\nres0:"+res0+"\nres1:"+res1+"\nentryCount:"+entryCount+"\ntypeName:" + typeName +"\nelement list:" + entry;
    }

}
