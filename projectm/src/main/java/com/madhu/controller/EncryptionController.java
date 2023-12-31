package com.madhu.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.madhu.exception.EncryptionException;
import com.madhu.service.EncryptionService;

@Controller
public class EncryptionController {

	@Autowired
	private EncryptionService eService;

	@Value("${file.location}")
	private String uploadLocation;

	@GetMapping(value = { "/", "/home" })
	public String homePage() {
		return "index";
	}

	@PostMapping("/encryptTextProcess")
	public String encryptText(@RequestParam String plainText, @RequestParam(required = false) String secretKey,
			Model model) throws EncryptionException, Exception {

		System.out.println("Inside Encryption of Text with text " + plainText + " and key " + secretKey);

		String result = eService.encryptText(plainText, secretKey);
		System.out.println("The encrypted text is " + result);

		model.addAttribute("encryptedData", result);

		return "index";

	}

	@PostMapping("/decryptTextProcess")
	public String decryptText(@RequestParam String encryptedText, @RequestParam(required = false) String secretKey,
			Model model) throws EncryptionException, Exception {

		System.out.println("Inside Decryption of Text");

		String result = eService.decryptText(encryptedText, secretKey);

		model.addAttribute("decryptedData", result);

		return "index";
	}

	@PostMapping("/encryptFileProcess")
	@ResponseBody
	public ResponseEntity<Resource> encryptFile(@RequestParam("file") MultipartFile plainFile,
			@RequestParam(required = false) String secretKey, Model model) throws EncryptionException, Exception {

		System.out.println("Inside Encryption of file");

		var file = eService.encryptFile(plainFile, secretKey).getFile();

		System.out.println("the file name is " + file.getAbsolutePath());

		System.out.println("file exist ? " + file.exists());

		try {
			// Load the file as a resource
			Resource resource = new UrlResource(file.toURI());

			if (resource.exists()) {
				// Set response headers to trigger the download

				HttpHeaders headers = new HttpHeaders();
				headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename());

				// Return the file as a ResponseEntity with appropriate headers
				return ResponseEntity.ok().headers(headers).body(resource);
			} else {
				// Handle the case where the file does not exist
				return ResponseEntity.notFound().build();
			}
		} catch (IOException e) {
			// Handle exceptions, e.g., file not found or other errors
			return ResponseEntity.status(500).body(null);
		}

	}

	@PostMapping("/decryptFileProcess")
	ResponseEntity<Resource> decryptFile(@RequestParam("file") MultipartFile encryptedFile,
			@RequestParam(required = false) String secretKey, Model model) throws EncryptionException, Exception {

		System.out.println("Inside Decryption of file");

		System.out.println("Encrypted File is " + encryptedFile.getOriginalFilename());

		var file = eService.decryptFile(encryptedFile, secretKey).getFile();

		var filePath = Path.of(file.getPath());

		System.out.println("the file path is " + filePath);

		try {
			// Load the file as a resource
			Resource resource = new UrlResource(filePath.toUri());

			System.out.println("the resource is " + resource);

			System.out.println(resource.getFilename());

			System.out.println("the resource 2 is " + resource.getFilename());
			if (resource.exists()) {
				// Set response headers to trigger the download
				HttpHeaders headers = new HttpHeaders();
				headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename());

				// Return the file as a ResponseEntity with appropriate headers
				return ResponseEntity.ok().headers(headers).body(resource);
			} else {
				// Handle the case where the file does not exist
				System.out.println("inside not found ");
				return ResponseEntity.notFound().build();
			}
		} catch (IOException e) {
			// Handle exceptions, e.g., file not found or other errors
			System.out.println("Inside catch block");
			return ResponseEntity.status(500).body(null);
		}

	}

}
