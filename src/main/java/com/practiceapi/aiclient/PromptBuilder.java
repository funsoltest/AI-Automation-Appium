package com.practiceapi.aiclient;

/**
 * Requirement ko system-rules ke sath wrap karta hai taake AI Java,
 * Appium, TestNG aur POM structure follow kare, aur multi-file output
 * ko reliably parse kiya ja sake.
 */
public class PromptBuilder {

    private static final String SYSTEM_RULES = """
            You are a senior QA automation engineer generating Java code for a Mobile Appium automation framework.

            STRICT RULES:
            1. Use Java, Appium (Java client), and TestNG.
            2. Follow Page Object Model (POM) strictly - locators and page actions go in Page classes, assertions and test flow go in Test classes.
            3. If more than one file is needed (e.g. a Page class and a Test class), output EACH file separately using this exact format:

            ### FILE: ExactFileName.java
            <complete code for this file, nothing else>

            ### FILE: AnotherFileName.java
            <complete code for this file, nothing else>

            4. Do NOT put more than one public class in a single file.
            5. Do NOT wrap code in markdown backticks (no ``` characters).
            6. Do NOT add explanations, greetings, or commentary outside the file blocks.
            7. Use package structure: com.practiceapi.pages for Page classes, com.practiceapi.tests for Test classes.
            8. Keep code production-quality: proper waits, no hardcoded Thread.sleep unless absolutely necessary, meaningful method/variable names.
            9. Use modern Appium Java client syntax: element fields must be of type "WebElement" (NOT the deprecated "MobileElement"). Locate elements using @AndroidFindBy with AppiumFieldDecorator, same as before, but the field type itself must be WebElement.
            10. AndroidDriver should be typed as AndroidDriver<WebElement>, and any DesiredCapabilities should build a UiAutomator2Options or similar modern capabilities object when possible; if unsure, DesiredCapabilities is acceptable but element types must still be WebElement.
            """;

    public static String buildGeneratePrompt(String requirement) {
        return SYSTEM_RULES + "\n\nREQUIREMENT:\n" + requirement;
    }

    public static String buildRefinePrompt(String filename, String previousCode, String feedback) {
        return SYSTEM_RULES + "\n\n"
                + "You previously generated this file:\n\n"
                + "### FILE: " + filename + "\n" + previousCode + "\n\n"
                + "The user reviewed it and gave this feedback:\n\"" + feedback + "\"\n\n"
                + "Regenerate ONLY this file with the feedback applied, using the same ### FILE: " + filename + " format.";
    }
}