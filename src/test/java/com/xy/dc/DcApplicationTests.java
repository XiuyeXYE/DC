package com.xy.dc;

import com.xiuye.compiler.util.XYCompiler;
import com.xiuye.util.cls.TypeUtil;
import com.xiuye.util.log.LogUtil;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DcApplicationTests {

	@Test
	void contextLoads() {
		LogUtil.log(123);
		XYCompiler.compileCode("",TypeUtil.createMap());
	}

}
