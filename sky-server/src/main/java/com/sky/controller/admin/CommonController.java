package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/admin/common")
public class CommonController {

    private final AliOssUtil aliOssUtil;
    @Autowired
    public CommonController(AliOssUtil aliOssUtil) {
        this.aliOssUtil = aliOssUtil;
    }

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传:{}",file);
        try {
            String originalFilename = file.getOriginalFilename();
            log.info("原始文件名:{}",originalFilename);
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            log.info("文件后缀:{}",suffix);
            String fileName = System.currentTimeMillis() + suffix;
            log.info("新文件名:{}",fileName);
            //上传文件到阿里云OSS
            String filePath = aliOssUtil.upload(file.getBytes(),fileName);
            return Result.success(filePath);
        }catch (IOException e){
            log.error("文件上传失败",e);
            return Result.error("文件上传失败");
        }
    }
}
