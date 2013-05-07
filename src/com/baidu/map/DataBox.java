package com.baidu.map;

import java.util.Date;

public class DataBox {
	
	public int id;
	public String chineseName;
	public String englishName;
	
	public static enum Type{
		NONE,
		POINT,
		LINE,
		AREA
	}
	
	public Type elementType = Type.NONE;
	public Date createTime;
	public Date modifyTime;
	public long userId;

}
