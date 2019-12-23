package com.xy.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.xiuye.util.cls.TypeUtil;
import com.xiuye.util.log.LogUtil;
import com.xy.bean.Result;

@RestController
public class FileJsonController {

	private static final String ROOT = "/var/files/";
	private static final String TMP = "/tmp/files/";

	private String randomFileName() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	private String suffix(String filename) {
		int idx = filename.lastIndexOf(".");
		idx = idx == -1 ? filename.length() : idx;
		return filename.substring(idx);
	}

	private Map<String, Object> uploadFileHandler(MultipartFile file) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
		String date = sdf.format(new Date());
		Path directory = Paths.get(TMP + date);
		if (Files.notExists(directory)) {
			directory = Files.createDirectories(directory);
		}
		String sfx = this.suffix(file.getOriginalFilename());
		String filename = this.randomFileName();
		filename = filename + sfx;
		String path = directory + File.separator;
		Files.createFile(Paths.get(path + filename));
		Map<String, Object> fileInfo = TypeUtil.createMap();
		fileInfo.put("date", date);
		fileInfo.put("filename", filename);
		fileInfo.put("path", path);
		return fileInfo;
	}

	@RequestMapping("singleFileUpload")
	public Result singleFileUpload(@RequestParam("file") MultipartFile file, HttpServletRequest req)
			throws IOException {
		List<Map<String, Object>> list = TypeUtil.createList();
		list.add(this.uploadFileHandler(file));
		return Result.OK(list);
	}

	@RequestMapping("multipleFileUpload")
	public Result multipleFileUpload(@RequestParam("files") MultipartFile[] files, HttpServletRequest req)
			throws IOException {
		List<Map<String, Object>> r = TypeUtil.createList();
		for (MultipartFile file : files) {
			r.add(this.uploadFileHandler(file));
		}
		return Result.OK(r);
	}

	@RequestMapping("moveFile")
	public Result moveFile(@RequestBody Map<String, Object> params) throws IOException {
		List<Map<String, Object>> ps = TypeUtil.createList();
		List<Map<String, Object>> fileInfos = TypeUtil.dynamic_cast(params.get("data"));
		for (Map<String, Object> file : fileInfos) {
			String date = TypeUtil.dynamic_cast(file.get("date"));
			String filename = TypeUtil.dynamic_cast(file.get("filename"));
			String path = TypeUtil.dynamic_cast(file.get("path"));

			String directoryDir = ROOT + date;
			Path directory = Paths.get(directoryDir);
			if (Files.notExists(directory)) {
				Files.createDirectories(directory);
			}

			Path source = Paths.get(path + filename);
			file.put("moved", false);
			if (Files.exists(source)) {
				Files.move(source, Paths.get(directoryDir + File.separator + filename));
				file.put("moved", true);
			}

			file.put("path", directoryDir);
			ps.add(file);
		}
		return Result.OK(ps);
	}
	
	

}
