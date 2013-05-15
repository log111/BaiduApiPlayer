package com.baidu.openapi.map;

import android.util.SparseArray;

public class StatusCode {
	private static SparseArray<String> map
		= new SparseArray<String>();
	
	static{
		map.put(0, "正常");	
		map.put(1, "服务器内部错误");
		map.put(2, "请求参数非法");
		map.put(3, "权限校验失败");
		map.put(4, "配额校验失败");	
		map.put(5, "ak不存在或者非法");
		map.put(101, "服务禁用");
		map.put(102, "不通过白名单或者安全码不对");
		map.put(200, "无权限");
		map.put(300, "配额错误");
	}
	
	public static String getMessage(int statusCode){
		int i = statusCode/100;
		if(2 == i){
			statusCode = 200;
		}else if(3 == i){
			statusCode = 300;
		}
		return map.get(statusCode, "");
	}
}
