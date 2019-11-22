package com.xy;

import java.util.HashMap;

public class Result extends HashMap<String, Object>{

	private static String SUCCESS = "success";
	private static String INFO = "info";
	private static String DATA = "data";
	private static String STATUS = "status";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4128214982381147306L;

	public Result() {
		this(true,"successful","successful",200);
	}
	
	public Result(boolean success,String info,Object data,int status) {
		this.put(SUCCESS , success);
		this.put(INFO , info);
		this.put(DATA , data);
		this.put(STATUS , status);
	}
	
	public static Result of(boolean success,String info,Object data,int status) {
		return new Result(success, info, data, status);
	}
	
	public static Result OK(String info,Object data) {
		return of(true,info,data,200);
	}
	
	public static Result OK() {
		return OK("successful","successful");
	}
	
	public static Result OK(Object data) {
		return OK("successful",data);
	}
	
	public static Result err(String info,Object data,int status) {
		return of(false,info,data,status);
	}

	public static Result err(String info,Object data) {
		return err(info,data,500);
	}
	
	public static Result err(Object data,int status) {
		return err("failure",data,status);
	}
	
	public static Result err(Object data) {
		return err(data,500);
	}
}
