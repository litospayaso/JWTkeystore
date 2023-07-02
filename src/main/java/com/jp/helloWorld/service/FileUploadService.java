package com.jp.helloWorld.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class FileUploadService {
    public void uploadFile(MultipartFile file) throws IOException {
        String dir = System.getProperty("user.dir");
        System.out.println(dir);
        file.transferTo(new File(dir + "/JWTkeystore.jks"));
    }
}
