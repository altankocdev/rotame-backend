package com.altankoc.rotame.core.s3;

public interface S3Service {
    String uploadFile(String fileName, byte[] data, String contentType);
    void deleteFile(String fileName);
    String generatePresignedUrl(String fileName);
}