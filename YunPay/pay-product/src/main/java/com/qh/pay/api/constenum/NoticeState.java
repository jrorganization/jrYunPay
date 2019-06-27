package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

public enum NoticeState {

    init(0),succ(1);

    private int id;

    private  NoticeState(int id){this.id=id;}

    public int id(){
        return id;
    }
    /****支付订单回调状态描述****/
    private static final Map<Integer,String> descMap = new HashMap<>(8);
    static{
        descMap.put(init.id(), "回调中");
        descMap.put(succ.id(), "成功");
    }

    public static Map<Integer, String> desc() {
        return descMap;
    }

}
