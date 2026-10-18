package com.example.School.Management.System.repository;

import java.time.LocalDateTime;

public interface ExamFileInfo {
    Long getId();
    String getFileName();
    String getContentType();
    Long getSize();
    LocalDateTime getUploadedAt();
    String getUploadedBy();
    Boolean getPublished();
}
