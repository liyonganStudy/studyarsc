package com.example.parse_arsc.type;

/**
 * Created by hzliyongan on 2018/2/7.
 */

/**
 struct ResStringPool_ref
 {
 uint32_t index;
 };

 * @author i
 *
 */
public class ResStringPoolRef {

    public int index;

    public int getSize(){
        return 4;
    }

    @Override
    public String toString(){
        return "index:"+index;
    }

}
