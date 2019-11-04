package com.gj.dc.service.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.gj.dc.service.JsonService;
import com.google.gson.Gson;
import com.xiuye.util.cls.TypeUtil;
import com.xiuye.util.log.LogUtil;

@Service
public class JsonServiceImpl implements JsonService{
	
	@Resource
	private RedisTemplate<?, ?> redisTemplate;
	
	@Resource
	private Gson gson;
	
	@Override
	@Cacheable("json")
	public Map<String, Object> json() {

		Map<String, Object> res = TypeUtil.createMap();

		res.put("success", true);
		res.put("msg", "OK");
		res.put("data", "data");
		res.put("status", 200);

		LogUtil.log("Be called!!!");

		LogUtil.log("redisTemplate=",redisTemplate);
		
		return res;
	}
}
