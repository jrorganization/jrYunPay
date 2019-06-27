package com.qh.common.utils;


import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 */
public class EmptyUtil {

    /***
     *
     * @Description 判断内容是否为空
     * @return
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        } else if (obj instanceof String) {
            return StringUtils.isEmpty((String) obj);
        }
        return false;
    }

    /***
     *
     * @Description 判断内容是否不为空
     * @return
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /***
     *
     * @Description 判断是否含有Key且不为空
     * @return
     */
    public static boolean mapHasKey(Map map,String[] keys) {
        if(map == null ||map.size() == 0){
            return false;
        }
        boolean flag = true;
        for(String key:keys){
            if(!map.containsKey(key)){
                flag = false;
                break;
            }

            if(isEmpty(map.get(key))){
                flag =false;
                break;
            }
        }
        return flag;
    }

    public static boolean isEmpty(Object... patterns){
        List<Object> objList = Arrays.asList(patterns);
        for(Object obj:objList){
            if(isEmpty(obj)){
                return true;
            }
        }
        return false;
    }

    public static void  trimValueDelete(Map<String, Object> resultMap) {
        Iterator<?> entries = resultMap.entrySet().iterator();
        Map.Entry<String, Object> entry;
        while (entries.hasNext()) {
            entry = (Map.Entry<String, Object>) entries.next();
            Object valueObj = entry.getValue();
            if (isEmpty(valueObj)) {
                entries.remove();
            }else if(valueObj instanceof  String){
                entry.setValue(valueObj.toString().trim());
            }
        }
    }


    public static void  trimValue(Map<String, Object> resultMap) {
        Iterator<?> entries = resultMap.entrySet().iterator();
        Map.Entry<String, Object> entry;
        while (entries.hasNext()) {
            entry = (Map.Entry<String, Object>) entries.next();
            Object valueObj = entry.getValue();
            if (isEmpty(valueObj)) {
                entry.setValue(null);
            }else if(valueObj instanceof  String){
                entry.setValue(valueObj.toString().trim());
            }
        }
    }
}
