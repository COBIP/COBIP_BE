package com.cobip.infra.aws;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import com.cobip.domain.grammar.GrammarTemplateMediaType;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "image/gif"
    );

    private static final Set<String> VIDEO_CONTENT_TYPES = Set.of(
            "video/mp4",
            "video/webm",
            "video/quicktime"
    );

    private final ObjectProvider<S3Client> s3ClientProvider;
    private final String bucket;

    public S3Service(
        ObjectProvider<S3Client> s3ClientProvider,
        @Value("${cloud.aws.s3.bucket:}") String bucket
    ) {
        this.s3ClientProvider = s3ClientProvider;
        this.bucket = bucket;
    }

    public S3UploadResult uploadTemplateFile(Long templateId, MultipartFile file) {
        validateFile(file);
        return upload("templates/%d/main/%s".formatted(templateId, file.getOriginalFilename()), file);
    }

    public S3UploadResult uploadThumbnail(Long templateId, MultipartFile file) {
        validateFile(file);
        return upload("templates/%d/thumbnail/%s".formatted(templateId, file.getOriginalFilename()), file);
    }

    public S3UploadResult uploadGrammarTemplateMedia(
        Long templateId,
        GrammarTemplateMediaType mediaType,
        MultipartFile file
    ) {
        validateFile(file);
        validateGrammarTemplateMedia(mediaType, file);

        String folder = mediaType == GrammarTemplateMediaType.IMAGE ? "images" : "videos";
        return upload(
                "grammar-templates/%d/%s/%s".formatted(templateId, folder, safeFilename(file)),
                file
        );
    }

    private S3UploadResult upload(String key, MultipartFile file) {
        S3Client s3Client = s3ClientProvider.getIfAvailable();
        if (s3Client == null || bucket == null || bucket.isBlank()) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            // 템플릿 파일은 서버 로컬에 저장하지 않고 S3에 업로드한 뒤 URL만 DB에 저장한다.
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            String url = s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(key)).toString();
            return new S3UploadResult(key, url);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateGrammarTemplateMedia(GrammarTemplateMediaType mediaType, MultipartFile file) {
        if (mediaType == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
        boolean allowed = mediaType == GrammarTemplateMediaType.IMAGE
                ? IMAGE_CONTENT_TYPES.contains(normalizedContentType)
                : VIDEO_CONTENT_TYPES.contains(normalizedContentType);
        if (!allowed) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    private String safeFilename(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            return "upload";
        }

        String normalizedFilename = filename.replace('\\', '/');
        int lastSeparator = normalizedFilename.lastIndexOf('/');
        return lastSeparator >= 0 ? normalizedFilename.substring(lastSeparator + 1) : normalizedFilename;
    }
}
