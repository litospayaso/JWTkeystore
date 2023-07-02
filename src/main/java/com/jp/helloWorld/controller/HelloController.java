package com.jp.helloWorld.controller;

import com.jp.helloWorld.service.FileUploadService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.text.MessageFormat;

@RestController
public class HelloController {

	@Autowired
	FileUploadService fileUploadService;

	String header = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
	String claimTemplate = "'{'\"iss\": \"{0}\", \"sub\": \"{1}\", \"aud\": \"{2}\", \"exp\": \"{3}\"'}'";

	@GetMapping("/")
	public String getApplicationRoot(){
		String data = "Send a POST request to generate the token";
		return data;
	}


	@PostMapping
	public String postApplication(
			@RequestParam("file") MultipartFile file,
			@RequestParam("data") String data,
			@RequestParam("password") String password,
			@RequestParam("alias") String alias
	) throws IOException {
		try {
			fileUploadService.uploadFile(file);
			String dataWithTime = data + "," + Long.toString(System.currentTimeMillis() / 1000L + 300L);
			String[] claimArray = dataWithTime.split(",");
			StringBuffer token = new StringBuffer();
			token.append(Base64.encodeBase64URLSafeString(header.getBytes("UTF-8")));
			token.append(".");
			MessageFormat claims = new MessageFormat(claimTemplate);
			String payload = claims.format(claimArray);
			token.append(Base64.encodeBase64URLSafeString(payload.getBytes("UTF-8")));
			KeyStore keystore = KeyStore.getInstance("JKS");
			FileInputStream jwtKeystore = new FileInputStream("src/JWTkeystore.jks");
			keystore.load(jwtKeystore, password.toCharArray());
			PrivateKey privateKey = (PrivateKey)keystore.getKey(alias, password.toCharArray());
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initSign(privateKey);
			signature.update(token.toString().getBytes("UTF-8"));
			String signedPayload = Base64.encodeBase64URLSafeString(signature.sign());
			token.append(".");
			token.append(signedPayload);
			jwtKeystore.close();
			Path fileToDelete = Paths.get("src/JWTkeystore.jks");
			Files.deleteIfExists(fileToDelete);

			return token.toString();

		} catch (Exception var11) {
			var11.printStackTrace();
			return System.getProperty("user.dir") + "\n\n" +var11.getMessage();
		}
	}
}
