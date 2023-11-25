package com.example.noticeproject.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.noticeproject.enums.NoticeInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("dev")
public class NoticeApiServiceImpl implements NoticeApiService {
    @Value("${telegram_1.chIdSys}")
    private String chIdSys;
    @Value("${telegram_2.chIdMng}")
    private String chIdMng;
    @Value("${telegram_3.chIdMngSub}")
    private String chIdMngSub;
    @Value("${spring.profiles.active}")
    private String envMode;   // 환경

//    @Value("${webdriver.path}")
//    private String WEB_DRIVER_PATH;

    @Value("${DBItemUrl}")
    private String notiItemUrl;
    @Value("${cryptoExchUrl.UU_API}")
    private String UU_API;
    @Value("${cryptoExchUrl.CC_API}")
    private String CC_API;

    /**
     * [UU]
     * Regularly Updating a Single Page
     */
    @Override
    public void queryUUNewNotice() {
        String apiURL = "";

        try {
            RestTemplate restTemplate = new RestTemplate();
            JSONParser parser = new JSONParser();

            // 1.요청전
            // 1-1. URL 빌드
            UriComponents uri = UriComponentsBuilder
                    .fromUriString(UU_API)
                    //새 공지는 1페이지만 참조
                    .queryParam("page", "1")
                    .queryParam("per_page", "20")
                    .queryParam("thread_name", "general")
                    .build();
            apiURL = uri.toString();
            // 1-2. Headers 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> reqHttpEntity = new HttpEntity<>(headers);

            // 1-3. GET으로 보내기
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    apiURL,
                    HttpMethod.GET,
                    reqHttpEntity,
                    String.class
            );
//         log.debug("responseEntity ::: {}", responseEntity.toString());

            // 2.요청후
            String responseBody = responseEntity.getBody();
            boolean responseStats = responseEntity.getStatusCode().isError();
            log.debug("responseBody ::: {}", responseBody);

            // 3. 응답갑 분리
            if (responseStats) {
                log.error("UU_reqPage_{}_responseStats's_isError_is_TRUE", "1");
                throw new Exception("responseStats_isError");
            }
            JSONObject extract = (JSONObject) parser.parse(responseBody);
            boolean isSuccess = (boolean) extract.get("success");
            if (isSuccess) {
                JSONObject data = (JSONObject) extract.get("data");
                JSONArray curPageNotiList = (JSONArray) data.get("list");
                // 3. 현재페이지의 모든 공지 리스트 하나하나 분리
                for (Object obj : curPageNotiList) {
                    log.debug("공지ITEM ::: {}", obj.toString());

                    JSONObject item = (JSONObject) obj;
                    String regDt = (String) item.get("created_at");
                    String link = notiItemUrl + String.valueOf(item.get("id"));
                    String title = (String) item.get("title");
                    String viewCount = String.valueOf(item.get("view_count"));
                    String status = "";
                    String statusDate = "";
                    String aboutCode = "";
                    String detail = "";
                    // 3-2. category는 정규식을 활용해 title의 대괄호안에서 뽑아냄
                    String category = "";
                    Pattern pattern = Pattern.compile("\\[(.*?)\\]");
                    Matcher matcher = pattern.matcher(title);
                    // 매칭 결과가 있다면 해당 값을 가져오기
                    if (matcher.find()) {
                        category = matcher.group(1);
                    } else {
                        log.debug("매칭된 값이 없습니다.");
                    }

                    // 3-3.
                    //TODO : Querying information from the database
                    // (if exist > continue : insert into DB AND send telegram msg)
                    NoticeInfo newNoti = new NoticeInfo();
                    if (newNoti != null) {
                        log.debug("UU - 이미 등록된 공지입니다.");
                        continue;
                    }
                    NoticeInfo ni = new NoticeInfo();
                    ni.setLink(link);
                    ni.setExchangeCd("UU");
                    ni.setExchangeNm("UU");
                    ni.setCategory(category);
                    ni.setTitle(title);
                    ni.setRegDt(regDt);
                    ni.setStatus(status);
                    ni.setStatusRegDt(statusDate);
                    ni.setCode(aboutCode);
                    ni.setContent(detail);
//                    noticeCheckService.newNotiDataInsert(ni, "UU");

                    log.info("===================[UU 새공지]===================");
                    log.info("exchange : " + "UPBIT");
                    log.info("href : " + link);
                    log.info("title : " + title);
                    log.info("regDt : " + regDt);
                    log.info("count : " + viewCount);
                    log.info("category: " + category);
                    log.info("content : " + detail);
                    log.info("status: " + status);
                    log.info("code: " + aboutCode);
                    log.info("statusDate: " + statusDate);
                    log.info("==================================================");
                    log.info("UU_NEW_NOTICE_INSERT_SUCCESS_");
                } // for

            } else {// isSuccess -> False
                log.error("UU_NEWNOTI_responseBody's_success_is_FALSE");
            }
            log.info("[{}]UU_page_load_success", "1");
        } catch (HttpClientErrorException e) {
            String errMsg = "새공지_" + "1" + "페이지 HttpClientErrorException ERR ";
            if (e.getStatusCode().equals(HttpStatusCode.valueOf(400))) {
                log.error("[UU_reqPage_{}] 요청에러 400 ::::: " + "{}", "1", e.getStackTrace()[0]);
                errMsg += "400";
            } else if (e.getStatusCode().equals(HttpStatusCode.valueOf(500))) {
                log.error("[UU_reqPage_{}] 500 ::::: " + "{}", "1", e.getStackTrace()[0]);
                errMsg += "500";
            } else {
                log.error("UU_reqPage_{} ::: " + e.getStackTrace()[0], "1");
            }
            sendErrMsg(errMsg, "UU", chIdSys);

        } catch (Exception e) {
            String errMsg = "새공지_" + "1" + "페이지 " + e.getMessage();
            sendErrMsg(errMsg, "UU", chIdSys);
            log.error("UU_reqPage_{} ::: " + e.getStackTrace()[0], "1");
//         StackTraceElement[] stackTrace = e.getStackTrace();
//          for (StackTraceElement element : stackTrace) {
//             log.error(element.toString());
//          }
        } finally {

        }

    }

    /**
     * [UU]
     * 매일 한번 `기준일` 이후 모든 공지를 DB에 새로 INSERT
     */
    @Override
    public void queryUUNoticeTotal() {
        String responseBody = "";
        int reqPage = 1;
        int totalPage = 3;   // 임시값

        try {
            // 1. 전체페이지 도는 while문
            while (reqPage != totalPage + 1) {

                // 공지 넘 많아서 50페이지까지만 가져오도록 설정
                if (reqPage == 50)
                    break;

                // 2. API 호출해서 응답값 받기
                responseBody = connnectUUNotice(reqPage);
                // 3. 응답값이 있으면 파싱 진행
                if ("".equals(responseBody)) {
                    throw new Exception("API응답없음");
                }
                JSONParser parser = new JSONParser();
                JSONObject extract = (JSONObject) parser.parse(responseBody);
                boolean isSuccess = (boolean) extract.get("success");
                // 4. 응답값이 정상이면 파싱 진행s
                if (isSuccess) {
                    JSONObject data = (JSONObject) extract.get("data");

                    /////////////////////// [📌 총 페이지 개수 확인 📌]///////////////////////
                    if (reqPage == 2) {
                        String tempTotalPage = String.valueOf(data.get("total_pages"));
                        if (tempTotalPage == null) {
                            throw new Exception("총 페이지수 로드 실패");
                        }
                        totalPage = Integer.parseInt(tempTotalPage);
                    }

                    /////////////////////// [📌 일반 공지 📌]///////////////////////
                    JSONArray curPageNotiList = (JSONArray) data.get("list");
                    // 현재페이지의 모든 공지 리스트 하나하나 분리
                    for (Object obj : curPageNotiList) {
                        log.debug("공지ITEM ::: {}", obj.toString());

                        JSONObject item = (JSONObject) obj;
                        String regDt = (String) item.get("created_at");
                        String link = notiItemUrl + String.valueOf(item.get("id"));
                        String title = (String) item.get("title");
                        String viewCount = String.valueOf(item.get("view_count"));
                        String status = "";
                        String statusDate = "";
                        String aboutCode = "";
                        String detail = "";
                        // 2) category는 정규식을 활용해 title의 대괄호안에서 뽑아냄
                        String category = "";
                        Pattern pattern = Pattern.compile("\\[(.*?)\\]");
                        Matcher matcher = pattern.matcher(title);
                        // 3) 매칭 결과가 있다면 해당 값을 가져오기
                        if (matcher.find()) {
                            category = matcher.group(1);
                        } else {
                            log.debug("매칭된 값이 없습니다.");
                        }

                        // 4) 전체공지는 무조건 db insert
                        NoticeInfo ni = new NoticeInfo();
                        ni.setLink(link);
                        ni.setExchangeCd("UU");
                        ni.setExchangeNm("UU");
                        ni.setCategory(category);
                        ni.setTitle(title);
                        ni.setRegDt(regDt);
                        ni.setStatus(status);
                        ni.setStatusRegDt(statusDate);
                        ni.setCode(aboutCode);
                        ni.setContent(detail);
//                      noticeInfoMapper.insertNoticeInfo(ni);
                        log.info("======================[UU_전체공지]========================");
                        log.info("href : " + link);
                        log.info("title : " + title);
                        log.info("regDt : " + regDt);
                        log.info("count : " + viewCount);
                        log.info("category: " + category);
                        log.info("=========================================================");
                    } // for - 현재페이지
                } else {// isSuccess -> False
                    log.error("UU_SOMETHINGWRONG_responseBody's_success_is_FALSE");
                    String errMsg = "전체공지_" + reqPage + "페이지 responseBody's_success_is_FALSE";
                    sendErrMsg(errMsg, "UU", chIdSys);
                }
                // 다음페이지로 넘어가기
                log.info("[{}]page_load_success", reqPage);
                reqPage++;
            }//while
        } catch (ParseException e) {
            log.error("Exception [Err_Location] : {}", e.getStackTrace()[0]);
            String errMsg = "전체공지_" + reqPage + "페이지 파싱에러";
            sendErrMsg(errMsg, "UU", chIdSys);

        } catch (Exception e) {
            log.error("Exception [Err_Location] : {}", e.getStackTrace()[0]);
            String errMsg = "전체공지_" + reqPage + "페이지 " + e.getMessage();
            sendErrMsg(errMsg, "UU", chIdSys);
        }
    }

    /**
     * [UU]
     * UUApiCall
     *
     * @param reqPage
     * @return
     */
    public String connnectUUNotice(int reqPage) throws InterruptedException {
        String apiURL = "";
        String responseBody = "";
        int retryCount = 3;
        while (retryCount != 0) {
            try {
                RestTemplate restTemplate = new RestTemplate();

                // 1.요청전
                // 1-1. URL 빌드
                UriComponents uri = UriComponentsBuilder
                        .fromUriString(UU_API)
                        //새 공지는 1페이지만 참조
                        .queryParam("page", String.valueOf(reqPage))
                        .queryParam("per_page", "20")
                        .queryParam("thread_name", "general")
                        .build();
                apiURL = uri.toString();
                // 1-2. Headers 설정
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> reqHttpEntity = new HttpEntity<>(headers);

                // 1-3. GET으로 보내기
                ResponseEntity<String> responseEntity = restTemplate.exchange(
                        apiURL,
                        HttpMethod.GET,
                        reqHttpEntity,
                        String.class
                );
                //         log.debug("responseEntity ::: {}", responseEntity.toString());

                // 2.요청후
                responseBody = responseEntity.getBody();
//                log.debug("UU_responseBody ::: {}", responseBody);

                boolean responseStats = responseEntity.getStatusCode().isError();
                if (responseStats) {
                    log.error("UU_reqPage_{}_responseStats's_isError_is_TRUE", reqPage);
                    throw new Exception("responseStats_isError");
                }
                retryCount = 0;
                Thread.sleep(20 * 1000); // 20초 대기 호출, 지속적으로 호출시 Too many Request 오류 발생함.

            } catch (HttpClientErrorException e) {
                String errMsg = "전체공지_" + reqPage + "페이지 HttpClientErrorException ERR ";
                if (e.getStatusCode().equals(HttpStatusCode.valueOf(429))) {
                    log.error("[UU_reqPage_{}] 요청에러 429 ::::: "
                                    + "{}"
                            , reqPage
                            , e.getStackTrace()[0]);
                    errMsg += "429 TOO Many Requests";
                } else if (e.getStatusCode().equals(HttpStatusCode.valueOf(400))) {
                    log.error("[UU_reqPage_{}] 요청에러 400 ::::: "
                                    + "{}"
                            , reqPage
                            , e.getStackTrace()[0]);
                    errMsg += "400";
                } else if (e.getStatusCode().equals(HttpStatusCode.valueOf(500))) {
                    log.error("[UU_reqPage_{}] 500 ::::: "
                                    + "{}"
                            , reqPage
                            , e.getStackTrace()[0]);
                    errMsg += "500";
                } else {
                    log.error("UU_reqPage_{} ::: " + e.getStackTrace()[0], reqPage);
                }
                sendErrMsg(errMsg, "UU", chIdSys);

//                if(retryCount == 0){
//                    sendErrMsg(errMsg, "UU", chIdSys);
//                }else if(e.getResponseHeaders().containsKey("Retry-After")){ //Retry-After: ??}가 있으면
//                    int retryAfterValue = Integer.parseInt(e.getResponseHeaders().getFirst("Retry-After"));
//                    log.debug("Retry-After Header Value: {}", retryAfterValue);
//                    retryCount--;
//                }else{
//                    log.debug("[UU_reqPage{}]retry", reqPage);
//                    retryCount--;
//                }
            } catch (Exception e) {
                log.error("UU_reqPage_{} ::: " + e.getStackTrace()[0], reqPage);
                //          StackTraceElement[] stackTrace = e.getStackTrace();
                //          for (StackTraceElement element : stackTrace) {
                //             log.error(element.toString());
                //          }
                if (retryCount == 0) {
                    String errMsg = "전체공지_" + reqPage + "페이지 " + e.getMessage();
                    sendErrMsg(errMsg, "UU", chIdSys);
                } else {
                    log.debug("[UU_reqPage{}]retry", reqPage);
                    retryCount--;
                }
            } finally {

            }
        }
        return responseBody;

    }

    /**
     * [CC]
     * Regularly Updating a Single Page
     */
    @Override
    public void queryCCNewNotice() {
        String apiURL = "";
        int curPage = 1;

        try {
            RestTemplate restTemplate = new RestTemplate();
            JSONParser parser = new JSONParser();

            // 1.요청전
            // 1-1. URL 빌드
            UriComponents uri = UriComponentsBuilder
                    .fromUriString(CC_API)
                    //새 공지는 1페이지만 참조
                    .queryParam("page", curPage)
                    .queryParam("page_size", "10")
                    .queryParam("ordering", "-created_at")
                    .build();
            apiURL = uri.toString();
            log.debug("현재_조회_페이지 ::: {}", apiURL);

            // 1-2. Headers 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON); //text/html; charset=UTF-8
            headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.ALL));
//                headers.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
            headers.add("Accept-Language", "ko-KR,ko;q=0.9");
            headers.add("Cookie", "cf_chl_2=; cf_clearance=4OVHs7HFq7aBqhPbiD4ewtVUx0x3lTZMZiHI1XHCkY4-1700374784-0-1-1ede0faa.f175a09a.49df504a-250.0.0; cf_chl_2=3819df317f7138c");
            headers.add("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Safari/605.1.15");
            HttpEntity<String> reqHttpEntity = new HttpEntity<>(headers);

            // 1-3. GET으로 보내기
            ResponseEntity<String> responseEntity = restTemplate.exchange(apiURL,
                    HttpMethod.GET,
                    reqHttpEntity,
                    String.class
            );
//                log.debug("responseEntity ::: {}", responseEntity.getBody());

            // 2.요청후
            String responseBody = responseEntity.getBody();
            boolean responseStats = responseEntity.getStatusCode().isError();
            log.debug("responseBody ::: {}", responseBody);

            // 3. 응답갑 분리
            if (responseStats) {
                log.error("CC_reqPage_{}_responseStats's_isError_is_TRUE", curPage);
                throw new Exception("responseStats_isError");
            }
            JSONObject extract = (JSONObject) parser.parse(responseBody);
            JSONArray results = (JSONArray) extract.get("results");
            // 3. 현재페이지의 모든 공지 리스트 하나하나 분리
            for (Object obj : results) {
                log.debug("공지ITEM ::: {}", obj.toString());

                JSONObject item = (JSONObject) obj;
                String regDt = (String) item.get("created_at"); // 등록날짜
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(regDt);
                LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd");
                regDt = localDateTime.format(formatter);

                String link = notiItemUrl + String.valueOf(item.get("id"));
                String title = (String) item.get("title");
                String category = (String) item.get("card_category");
                String status = "";
                String statusDate = "";
                String aboutCode = "";
                String detail = "";

                //TODO : Querying information from the database
                // (if exist > continue : insert into DB AND send telegram msg)
                NoticeInfo newNoti = null;
                if (newNoti != null) {
                    log.debug("CC - 이미 등록된 공지입니다.");
                    continue;
                }
                NoticeInfo ni = new NoticeInfo();
                ni.setLink(link);
                ni.setExchangeCd("CC");
                ni.setExchangeNm("CC");
                ni.setCategory(category);
                ni.setTitle(title);
                ni.setRegDt(regDt);
                ni.setStatus(status);
                ni.setStatusRegDt(statusDate);
                ni.setCode(aboutCode);
                ni.setContent(detail);
//                noticeCheckService.newNotiDataInsert(ni, "CC");

                log.info("===================[CC 새공지]===================");
                log.info("exchange : " + "CC");
                log.info("href : " + link);
                log.info("title : " + title);
                log.info("regDt : " + regDt);
                log.info("category: " + category);
                log.info("content : " + detail);
                log.info("status: " + status);
                log.info("aboutCode: " + aboutCode);
                log.info("statusDate: " + statusDate);
                log.info("===================================================");
                log.info("CC_NEW_NOTICE_INSERT_SUCCESS_");
            } // for
            log.info("[{}]CC_page_load_success", curPage);
        } catch (HttpClientErrorException e) {
            String errMsg = "새공지_" + curPage + "페이지 HttpClientErrorException ERR ";
            if (e.getStatusCode().equals(HttpStatusCode.valueOf(400))) {
                log.error("[CC_reqPage_{}] 요청에러 400 ::::: " + "{}", curPage, e.getStackTrace()[0]);
                errMsg += "400";
            } else if (e.getStatusCode().equals(HttpStatusCode.valueOf(404))) {
                log.error("[CC_reqPage_{}] 404 ::::: " + "{}", curPage, e.getStackTrace()[0]);
                errMsg += "404";
            } else if (e.getStatusCode().equals(HttpStatusCode.valueOf(500))) {
                log.error("[CC_reqPage_{}] 500 ::::: " + "{}", curPage, e.getStackTrace()[0]);
                errMsg += "500";
            } else {
                log.error("CC_reqPage_{} ::: " + e.getStackTrace()[0], curPage);
            }
            sendErrMsg(errMsg, "CC", chIdSys);

        } catch (Exception e) {
            String errMsg = "새공지_" + curPage + "페이지 " + e.getMessage();
            sendErrMsg(errMsg, "CC", chIdSys);
            log.error("CC_reqPage_{} ::: " + e.getStackTrace()[0], curPage);
//         StackTraceElement[] stackTrace = e.getStackTrace();
//          for (StackTraceElement element : stackTrace) {
//             log.error(element.toString());
//          }
        }
    }

    /**
     * 에러가 발생했을때 텔레그램방에 메시지를 전송합니다.
     */
    private void sendErrMsg(String errMsg, String org, String chatId) {

        //텔레그램 전송 DB등록
        String sendTp = "00";
        String trgtNm = "SYSTEM";
        String trgtInfo = chatId;
        String title = "[" + org + "_공지ERR] ";
        String msg = errMsg;
        String stat = "3"; // 상태: 요청

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("sendTp", sendTp);
        map.put("trgtNm", trgtNm);
        map.put("trgtInfo", trgtInfo);
        map.put("title", title);
        map.put("msg", msg);
        map.put("stat", stat);
        //TODO : send telegram msg logic
        log.debug("INSERT MSG ::: {}", map.toString());
    }

}