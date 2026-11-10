package com.qaautomation.tests;

import com.qaautomation.pages.LoginPage;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class LoginTest {

    private AppiumDriver driver;
    private LoginPage loginPage;

    @BeforeClass
    public void setup() throws MalformedURLException {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("platformName", "Android");
        caps.setCapability("appium:automationName", "UiAutomator2");
        caps.setCapability("appium:deviceName", "Android Device"); // Replace with your device/emulator name
        caps.setCapability("appium:appPackage", "vidfo.video.player.videoplayer"); // Replace with your app's package name
        caps.setCapability("appium:appActivity", "com.example.vidfo.ui.activity.MainActivity"); // Replace with your app's main activity
        caps.setCapability("appium:noReset", true);

        driver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), caps);
        loginPage = new LoginPage(driver);
    }

    @Test(priority = 1)
    public void testSuccessfulLogin() {
        // Assuming you have valid credentials for a successful login
        loginPage.login("valid_username", "valid_password");
        // Add assertions here to verify successful login, e.g., navigate to a new screen
        // For this example, we'll assume a successful login doesn't show an error message.
        // You would typically check for an element on the next page.
        Assert.assertFalse(loginPage.isErrorMessageDisplayed(), "Error message should not be displayed on successful login.");
    }

    @Test(priority = 2)
    public void testInvalidLogin() {
        loginPage.login("invalid_username", "wrong_password");
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(), "Error message should be displayed for invalid login.");
        Assert.assertEquals(loginPage.getErrorMessageText(), "Invalid username or password", "Error message text is incorrect.");
    }

    @AfterClass
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}