package com.baidu.openapi.map.lbs;

import java.util.Date;

public class DataBoxMeta {
	
	private DataBox owner;
	private String name;
	private String key;
	
	public static enum Type{
		INT32,
		INT64,
		FLOAT,
		DOUBLE,
		STRING
	}
	
	private Type type;
	private boolean isSort;
	private String apiKey;
	private String signed;
	private Date timestamp;
	
	public DataBoxMeta(DataBox dbox){
		owner = dbox;
	}
}
