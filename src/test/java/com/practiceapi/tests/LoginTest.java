package com.practiceapi.tests;

import com.practiceapi.pages.LoginPage;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class LoginTest {

    private AndroidDriver<WebElement> driver;
    private LoginPage loginPage;

    @BeforeClass
    public void setup() throws MalformedURLException {
        UiAutomator2Options capabilities = new UiAutomator2Options();
        capabilities.setPlatformName("Android");
        capabilities.setAutomationName("UiAutomator2");
        capabilities.setDeviceName("Android Device"); // Updated device name
        capabilities.setAppPackage("vidfo.video.player.videoplayer");
        capabilities.setAppActivity("com.example.vidfo.ui.activity.MainActivity"); // Updated app activity
        capabilities.setCapability("noReset", true); // Added noReset capability

        driver = new AndroidDriver<>(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
        loginPage = new LoginPage(driver);
    }

    @Test
    public void testSuccessfulLogin() {
        Assert.assertTrue(loginPage.isUsernameFieldVisible(), "Username field should be visible on login page.");
        loginPage.enterUsername("testuser");
        loginPage.enterPassword("password123");
        loginPage.clickLoginButton();

        // Add assertions here to verify successful login, e.g., check for a welcome message or a new screen.
        // For this example, we'll assume a successful login navigates to a new screen.
        // You would typically add a check for an element on the next screen.
        // For demonstration, we'll just assert that the username field is no longer visible.
        // In a real scenario, you'd check for an element on the dashboard/home screen.
        // Assert.assertFalse(loginPage.isUsernameFieldVisible(), "Username field should not be visible after login.");
        System.out.println("Login test completed. Add actual assertions for successful login verification.");
    }

    @AfterClass
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}