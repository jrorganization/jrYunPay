package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付币种
 */
public enum PayCoin {
    CNY;
    private static final Map<String,String> descMap = new HashMap<String,String>(4);
    static{
        descMap.put(CNY.name(), "CNY");
    }
    public static Map<String, String> desc() {
        return descMap;
    }
}
