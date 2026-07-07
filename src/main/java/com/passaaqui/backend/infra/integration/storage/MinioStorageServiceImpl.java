package com.passaaqui.backend.infra.integration.storage;

import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Profile("!test & !dev")
public class MinioStorageServiceImpl implements StorageService {

    private final MinioClient minioClient;
    private final String bucketName;
    private final String externalUrl;
    private final String accessKey;
    private final String secretKey;

    public MinioStorageServiceImpl(MinioClient minioClient,
                                   @Value("${minio.bucket.name}") String bucketName,
                                   @Value("${minio.url.external}") String externalUrl,
                                   @Value("${minio.access.key}") String accessKey,
                                   @Value("${minio.secret.key}") String secretKey) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
        this.externalUrl = externalUrl;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    @PostConstruct
    public void init() {
        try {
            boolean found = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing MinIO bucket", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            String fileName = folder + "_" + UUID.randomUUID();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize() ,- 1)
                            .contentType(file.getContentType())
                            .build());
            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Error uploading image", e);
        }
    }

    @Override
    public void deleteFile(String fileName) {

    }

    @Override
    public String getFileUrl(String fileName) {
        return generatePresignedUrl(fileName);
    }

    private String generatePresignedUrl(String fileName) {
        try {
            String region = "us-east-1";
            String service = "s3";
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
            String amzDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
            String dateStamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            int expires = 7200;

            String host = new java.net.URI(externalUrl).getHost();
            int port = new java.net.URI(externalUrl).getPort();
            String endpointHost = port > 0 ? host + ":" + port : host;

            String canonicalUri = "/" + bucketName + "/" + fileName;
            String signedHeaders = "host";

            String credentialScope = dateStamp + "/" + region + "/" + service + "/aws4_request";

            String canonicalQuery =
                    "X-Amz-Algorithm=AWS4-HMAC-SHA256" +
                            "&X-Amz-Credential=" + urlEncode(accessKey + "/" + credentialScope) +
                            "&X-Amz-Date=" + amzDate +
                            "&X-Amz-Expires=" + expires +
                            "&X-Amz-SignedHeaders=" + signedHeaders;

            String canonicalRequest =
                    "GET" + "\n" +
                            canonicalUri + "\n" +
                            canonicalQuery + "\n" +
                            "host:" + endpointHost + "\n" +
                            "\n" +
                            signedHeaders + "\n" +
                            "UNSIGNED-PAYLOAD";

            String stringToSign =
                    "AWS4-HMAC-SHA256" + "\n" +
                            amzDate + "\n" +
                            credentialScope + "\n" +
                            hex(sha256(canonicalRequest));

            byte[] signingKey = getSignatureKey(secretKey, dateStamp, region, service);
            String signature = hex(hmacSha256(stringToSign, signingKey));

            return externalUrl + canonicalUri + "?" + canonicalQuery + "&X-Amz-Signature=" + signature;

        } catch (Exception e) {
            throw new RuntimeException("Error generating presigned URL", e);
        }
    }

    private String urlEncode(String value) throws Exception {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private byte[] sha256(String text) throws Exception {
        return MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] hmacSha256(String data, byte[] key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws Exception {
        byte[] kDate = hmacSha256(dateStamp, ("AWS4" + key).getBytes(StandardCharsets.UTF_8));
        byte[] kRegion = hmacSha256(regionName, kDate);
        byte[] kService = hmacSha256(serviceName, kRegion);
        return hmacSha256("aws4_request", kService);
    }

    private String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
