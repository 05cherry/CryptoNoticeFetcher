package com.example.noticeproject.Task;


import com.example.noticeproject.Service.CrawlingService;
import com.example.noticeproject.Service.NoticeApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
@EnableScheduling
public class NoticeTask {

    @Autowired
    private NoticeApiService noticeApiService;

    @Autowired
    private CrawlingService crawlingService;

//   @Scheduled(cron = "0/30 * * * * *")
   public void collectNewNoticeUseAPI_UU() throws Exception {
        log.info("UU 새공지 조회");
        noticeApiService.queryUUNewNotice();
        log.info("============[ 끝 ]============");
   }

//    @Scheduled(cron = "0/30 * * * * *")
//    @Scheduled(cron = "30 0-1,3-23 * * *")
    @Scheduled(cron="0 10 21 * * * ") //5분마다
    public void collectTotalNoticeUseAPI_UB() throws Exception {
        log.info("UU 전체 조회");
        noticeApiService.queryUUNoticeTotal();
        log.info("============[ 끝 ]============");
    }

//   @Scheduled(cron = "0/30 * * * * *")
   public void collectNewNoticeUseAPI_CC() throws Exception {
        log.info("CC 새공지 조회");
//        noticeApiService.coinoneNewNotice();
//        crowlingService.enterCoineoneNoticePage();
        log.info("============[ 끝 ]============");
   }

   //   @Scheduled(cron = "0/30 * * * * *")
   public void collectTotalNoticeUseAPI_CC() throws Exception {
        log.info("CC 전체 조회");
        log.info("============[ 끝 ]============");
   }
}
