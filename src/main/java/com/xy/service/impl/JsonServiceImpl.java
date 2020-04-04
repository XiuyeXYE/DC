package com.xy.service.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.xiuye.util.cls.XType;
import com.xiuye.util.log.XLog;
import com.xy.service.JsonService;

@Service
public class JsonServiceImpl implements JsonService{
	
//	@Resource
//	private RedisTemplate<?, ?> redisTemplate;
	
	@Resource
	private Gson gson;
	
	@Override
	@Cacheable("json")
	public Map<String, Object> json() {

		Map<String, Object> res = XType.map();

		res.put("success", true);
		res.put("msg", "OK");
		res.put("data", "data");
		res.put("status", 200);

		XLog.log("Be called!!!");

//		XLog.log("redisTemplate=",redisTemplate);
		
		return res;
	}
}
