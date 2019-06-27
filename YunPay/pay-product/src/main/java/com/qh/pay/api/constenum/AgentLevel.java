package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * 代理级别
 * @author Swell
 *
 */
public enum AgentLevel {
	/**一级代理**/
	one(1),
	/**二级代理**/
	two(2),
	/**三级代理**/
	three(3),
	/**四级代理**/
	four(4),
	/**五级代理**/
	five(5);
	/**** 描述 ****/
	private static final Map<Integer, String> descMap = new HashMap<>(4);


    static {
		descMap.put(one.id(), "一级代理");
		descMap.put(two.id(), "二级代理");
		descMap.put(three.id(), "三级代理");
		descMap.put(four.id(), "四级代理");
		descMap.put(five.id(), "五级代理");
	}
	public static Map<Integer, String> desc() {
		return descMap;
	}
	
	private int id;

	private AgentLevel(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}

	/***目前最大的代理层级****/
	public static final int max_level = 3;
	/******/
	public static final String prev = "level_";
}
