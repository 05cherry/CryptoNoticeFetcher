package com.example.noticeproject.enums;

import lombok.Data;

@Data
public class NoticeInfo {
    private String link;
    private String exchangeCd;
    private String exchangeNm;
    private String category;
    private String title;
    private String regDt;
    private String status;
    private String statusRegDt;
    private String code;
    private String content;
}
