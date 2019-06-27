package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * 是否
 * @author Swell
 *
 */
public enum YesNoType {
	/**是**/
	yes(1),
	/**否**/
	not(0);

	/**** 描述 ****/
	private static final Map<Integer, String> descMap = new HashMap<>(4);
	static {
		descMap.put(yes.id(), "是");
		descMap.put(not.id(), "否");
	}

	public static Map<Integer, String> desc() {
		return descMap;
	}
	private static final Map<Integer, String> descOnOffMap = new HashMap<>(4);

	static {
		descOnOffMap.put(not.id(), "关闭");
		descOnOffMap.put(yes.id(), "开启");
	}
	public static Map<Integer, String> descOnOff() {
		return descOnOffMap;
	}

	/**** 描述 ****/
	private static final Map<Integer, String> descStatusMap = new HashMap<>(4);
	static {
		descStatusMap.put(yes.id(), "启用");
		descStatusMap.put(not.id(), "禁用");
	}
	public static Map<Integer, String> descStatus() {
		return descStatusMap;
	}
	
	/**** 描述 ****/
	private static final Map<Integer, String> noticeStatusMap = new HashMap<>(4);
	static {
		noticeStatusMap.put(yes.id(), "已通知");
		noticeStatusMap.put(not.id(), "通知中");
	}
	public static Map<Integer, String> noticeStatus() {
		return noticeStatusMap;
	}


	/**** 描述 ****/
	private static final Map<Integer, String> receiveStatusMap = new HashMap<>(4);
	static {
		receiveStatusMap.put(yes.id(), "接单中");
		receiveStatusMap.put(not.id(), "未接单");
	}
	public static Map<Integer, String> receiveStatus() {
		return receiveStatusMap;
	}

	private int id;

	private YesNoType(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}

}
