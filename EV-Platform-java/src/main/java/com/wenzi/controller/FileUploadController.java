package com.wenzi.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.wenzi.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    // 假设你把图片存在项目根目录的 uploads 文件夹下
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/image")
    public Result<String> uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }
        try {
            // 确保目录存在
            if (!FileUtil.exist(UPLOAD_DIR)) {
                FileUtil.mkdir(UPLOAD_DIR);
            }
            // 生成唯一文件名，防止覆盖
            String originalFilename = file.getOriginalFilename();
            String extName = FileUtil.extName(originalFilename);
            String newFileName = IdUtil.fastSimpleUUID() + "." + extName;

            // 保存文件
            File dest = new File(UPLOAD_DIR + newFileName);
            file.transferTo(dest);

            // 返回可供前端访问的 URL 路径 (配合后面的 WebConfig 映射)
            String url = "http://localhost:8080/uploads/" + newFileName;
            return Result.success(url);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    @PostMapping("/video")
    public Result<String> uploadVideo(MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("视频文件为空");
        }
        try {
            if (!FileUtil.exist(UPLOAD_DIR)) {
                FileUtil.mkdir(UPLOAD_DIR);
            }
            String originalFilename = file.getOriginalFilename();
            String extName = FileUtil.extName(originalFilename);
            String newFileName = IdUtil.fastSimpleUUID() + "." + extName;

            File dest = new File(UPLOAD_DIR + newFileName);
            file.transferTo(dest);

            // 同样返回静态映射的 URL
            String url = "http://localhost:8080/uploads/" + newFileName;
            return Result.success(url);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("视频上传失败：" + e.getMessage());
        }
    }

}