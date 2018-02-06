package com.example.parse_arsc.type;

/**
 * Created by hzliyongan on 2018/2/6.
 */

public class ResTablePackage {

    public ResChunkHeader header;
    public int id;
    public char[] name = new char[128];
    public int typeStrings;
    public int lastPublicType;
    public int keyStrings;
    public int lastPublicKey;
    public String packageName;

    public ResTablePackage(){
        header = new ResChunkHeader();
    }

    @Override
    public String toString(){
        return "header:"+header.toString()+"\nid="+id+ " 16进制：0x" + Integer.toHexString(id) + "\npackageName:"+ packageName +"\ntypeStrings:"+typeStrings+"\nlastPublicType:"+lastPublicType+"\nkeyStrings:"+keyStrings+"\nlastPublicKey:"+lastPublicKey+"\n";
    }

}

