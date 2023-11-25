package com.example.noticeproject.Service;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public interface CrawlingService {

    void enterCoineoneNoticePage(WebDriver webDriver, WebDriverWait webDriverWait) throws InterruptedException;

    void saveCookie(WebDriver webDriver);

    void getCookie();
}
