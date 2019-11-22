package com.xy.controller;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xy.service.JsonService;


@RestController
public class JsonController {
	
	@Resource
	private JsonService js;

	@RequestMapping("json")
	public Map<String,Object> json(){
		
		return js.json();
	}
	
}
