package com.xy.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.xiuye.util.cls.XType;
import com.xiuye.util.log.XLog;
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
		Files.copy(file.getInputStream(), Paths.get(path + filename));
		Map<String, Object> fileInfo = XType.map();
		fileInfo.put("date", date);
		fileInfo.put("filename", filename);
		fileInfo.put("path", path);
		return fileInfo;
	}

	@RequestMapping("singleFileUpload")
	public Result singleFileUpload(@RequestParam("file") MultipartFile file, HttpServletRequest req)
			throws IOException {
		List<Map<String, Object>> list = XType.list();
		list.add(this.uploadFileHandler(file));
		return Result.OK(list);
	}

	@RequestMapping("multipleFileUpload")
	public Result multipleFileUpload(@RequestParam("files") MultipartFile[] files, HttpServletRequest req)
			throws IOException {
		List<Map<String, Object>> r = XType.list();
		for (MultipartFile file : files) {
			r.add(this.uploadFileHandler(file));
		}
		return Result.OK(r);
	}

	@RequestMapping("moveFile")
	public Result moveFile(@RequestBody Map<String, Object> params) throws IOException {
		List<Map<String, Object>> ps = XType.list();
		List<Map<String, Object>> fileInfos = XType.cast(params.get("data"));
		for (Map<String, Object> file : fileInfos) {
			String date = XType.cast(file.get("date"));
			String filename = XType.cast(file.get("filename"));
			String path = XType.cast(file.get("path"));

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

	@RequestMapping("requestFile")
	public void requestFile(@RequestParam Map<String, Object> params, HttpServletResponse res) throws IOException {

		String filename = XType.cast(params.get("filename"));
		OutputStream os = res.getOutputStream();
		Path path = Paths.get(filename);
		if (Files.exists(path)) {
			XLog.log(Files.readAllBytes(path));
			os.write(Files.readAllBytes(path));
		}

	}

	@RequestMapping("clearTmp")
	public Result clearTmp(@RequestParam Map<String, Object> params) throws IOException {

		Files.walkFileTree(Paths.get(TMP), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				super.visitFile(file, attrs);

				XLog.log(file, attrs);
				Files.deleteIfExists(file);

				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				super.postVisitDirectory(dir, exc);
				Files.deleteIfExists(dir);
				return FileVisitResult.CONTINUE;
			}
		});

		return Result.OK();
	}

}
