package com.example.noticeproject.Service;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("dev")
public class CrawlingServiceImpl implements CrawlingService {
    @Value("${cryptoExchNm.CC_API}")
    private String CC_API;

    /**
     * 코인원 크롤링으로 팝업닫고 진입
     * @param webDriver
     * @param webDriverWait
     * @throws InterruptedException
     */
    @Override
    public void enterCoineoneNoticePage(WebDriver webDriver, WebDriverWait webDriverWait) throws InterruptedException{
        int maxRetries = 30; // 최대 재시도 횟수
        int retryCount = 0; // 현재 재시도 횟수

        // "공지사항" 요소 클릭
        while(retryCount < maxRetries) {
            try {
                // JavaScript 실행을 위한 JavascriptExecutor 객체 생성
                JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;

                webDriverWait.until(
                        ExpectedConditions.presenceOfElementLocated(By.className("button-name"))
                );
                // 01) modal-container 요소 숨기기
                jsExecutor.executeScript("arguments[0].style.display = 'none';", webDriver.findElement(By.cssSelector("modal-container")));
                // 02) modal-backdrop 요소 숨기기
                WebElement backdropElement = webDriver.findElement(By.className("modal-backdrop"));
                jsExecutor.executeScript("arguments[0].style.display = 'none';", backdropElement);
                jsExecutor.executeScript("arguments[0].style.display = 'none';", webDriver.findElement(By.cssSelector(".app-download-fixed-layer")));
                // 클릭하려는 요소로 스크롤
                //WebElement element = webDriver.findElement(By.className("button-name"));
                //jsExecutor.executeScript("arguments[0].scrollIntoView(true);", element);

                //WebElement buttonParent = webDriver.findElement(By.className("button-name")).findElement(By.xpath(".."));
                //buttonParent.click();

                ///////// headless 추가시 공지사항 버튼변경
                // 03) 모달 껐으면 확성기 찾아서 더보기 누름.
                webDriverWait.until(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector("#body_wrapper > common-gnb > nav > div > div.mobile-header > div.mobile-btn-group > div.mobile-notice-area > div.notice-menu > span.glyph-ui-horn"))
                );
                WebElement buttonParent = webDriver.findElement(By.cssSelector("#body_wrapper > common-gnb > nav > div > div.mobile-header > div.mobile-btn-group > div.mobile-notice-area > div.notice-menu > span.glyph-ui-horn"));
                buttonParent.click();
                Thread.sleep(1000);
                webDriverWait.until(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(".more-link"))
                );
                WebElement buttonMore = webDriver.findElement(By.cssSelector(".more-link"));
                buttonMore.click();
                ///////// headless 추가시 공지사항 버튼변경
                break; //완료시 종료
                //WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("선택자")));
            } catch (ElementNotInteractableException e) {
                log.debug("element not interactable : " + e.getMessage());
                retryCount++;
                webDriver.get(CC_API);
                try {
                    if (retryCount < maxRetries) {
                        log.debug("재시도를 수행합니다. (재시도 횟수: " + retryCount + ")");
                        Thread.sleep(2000);
                    } else {
                        log.error("최대 재시도 횟수를 초과하였습니다. 스크래핑 작업을 종료합니다.", e);
                    }
                } catch (InterruptedException e1) {
                    log.error("InterruptedException ; ", e);
                }

                // 요소가 상호 작용할 수 없는 경우 대기 후 다시 시도
                //Actions actions = new Actions(webDriver);
                //WebElement buttonParent2 = webDriver.findElement(By.className("button-name")).findElement(By.xpath(".."));
                //Thread.sleep(2000);
                //actions.moveToElement(buttonParent2).click().perform();
                //WebElement otherElement = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("다른 선택자")));
            } catch (org.openqa.selenium.TimeoutException e) {
                // TimeoutException 처리
                log.error("TimeoutException : ", e);
                //종료처리
                break;
            } catch (Exception e) {
                log.debug("stale element not found : " + e.getMessage());
                retryCount++;
                try {
                    if (retryCount < maxRetries) {
                        log.debug("재시도를 수행합니다. (재시도 횟수: " + retryCount + ")");
                        Thread.sleep(2000);
                    } else {
                        log.error("최대 재시도 횟수를 초과하였습니다. 스크래핑 작업을 종료합니다.", e);
                    }
                } catch (InterruptedException e1) {
                    log.error("InterruptedException : ", e1);
                }
                // 요소를 클릭할 수 없는 경우 JavaScript를 사용하여 클릭 이벤트 시뮬레이션
                //Thread.sleep(2000);
                //JavascriptExecutor executor = (JavascriptExecutor) webDriver;
                //executor.executeScript("arguments[0].click();", element);
            }
        }//while
        saveCookie(webDriver);
    }

    /**
     * 쿠키 정장
     * @param webDriver
     */
    @Override
    public void saveCookie(WebDriver webDriver) {

//        File file = new File("Cookies.data");
        try {
//            file.delete();
//            file.createNewFile();
//            FileWriter fileWriter = new FileWriter(file);
//            BufferedWriter bw = new BufferedWriter(fileWriter);
            for (Cookie ck : webDriver.manage().getCookies()) {
                log.debug("==================[쿠키 분석중]==================");
                log.debug("name: " + ck.getName());
                log.debug("domain: " + ck.getDomain());
                log.debug("isSecure: {}", ck.isSecure());
                log.debug("isHttpOnley: {}", ck.isHttpOnly());
                log.debug("FullText ::: "+ ck.toString());
                log.debug("===============================================");
//                bw.write(ck.getName() + ";" + ck.getValue() + ";" + ck.getDomain() + ";" + ck.getPath() + ";"
//                        + ck.getExpiry() + ";" + ck.isSecure());
//                bw.newLine();
            }
//            bw.close();
//            fileWriter.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void getCookie() {
        
    }
}
