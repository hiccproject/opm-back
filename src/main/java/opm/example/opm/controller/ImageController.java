package opm.example.opm.controller;

import lombok.RequiredArgsConstructor;
import opm.example.opm.service.S3Service;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final S3Service s3Service;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadFile(@RequestPart("file") MultipartFile file) {
        try {
            String imageUrl = s3Service.uploadImage(file);
            return "File uploaded successfully! imageUrl: " + imageUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return "File upload failed!";
        }
    }
}