package tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pages.LoginPage;
import pages.RegisterPage;
import utils.DriverUtils;


import java.io.FileReader;
import java.io.IOException;

@Epic("Rediff Registration")
@Feature("User Registration Flow")
public class RegistrationTest {

    private Page page;
    private JsonObject config;
    private JsonObject testData;

    @BeforeEach
    public void setup() throws IOException {
        // Load Configuration
        try (FileReader configReader = new FileReader("src/test/resources/config.json")) {
            config = JsonParser.parseReader(configReader).getAsJsonObject();
        }

        // Load Test Data
        try (FileReader dataReader = new FileReader("src/test/resources/testData.json")) {
            testData = JsonParser.parseReader(dataReader).getAsJsonObject();
        }

        // Initialize Driver
        boolean isHeadless = config.get("headless").getAsBoolean();
        page = DriverUtils.initDriver(isHeadless);
    }


    // POSITIVE SCENARIO (Happy Path)

    @Test
    @Story("Positive Registration")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Positive Registration Flow")
    @Description("Registers a user via the happy path and validates the selected country and generated ID.")
    public void testPositiveRegistrationFlow() {
        System.out.println("--- Executing Positive Scenario ---");

        // Initialize Page Objects ---
        LoginPage loginPage = new LoginPage(page);
        RegisterPage registerPage = new RegisterPage(page);

        // Extract Data ---
        String expectedCountry = testData.get("expectedCountry").getAsString();

        // Execute Flow on Landing Page ---
        loginPage.navigateTo(config.get("baseUrl").getAsString());
        loginPage.clickCreateAccount();

        // Execute Flow on Register Page ---
        registerPage.fillPersonalDetails(
                testData.get("fullName").getAsString(),
                testData.get("rediffId").getAsString()
        );

        registerPage.checkAvailabilityAndWait();

        // Print and select ID
        String availableId = registerPage.getFirstAvailableId();
        System.out.println("First available suggested ID: " + availableId);
        Allure.parameter("First Available ID", availableId);
        registerPage.selectFirstSuggestedId();

        // Fill remaining details
        registerPage.fillPassword(testData.get("password").getAsString());
        registerPage.checkNoAlternateId();

        registerPage.selectDOB(
                testData.get("dobDay").getAsString(),
                testData.get("dobMonth").getAsString(),
                testData.get("dobYear").getAsString()
        );

        // Country dropdown actions
        registerPage.clickCountryDropdown();
        registerPage.printAvailableCountries();
        registerPage.selectCountry(expectedCountry);

        // Validations (Hard Assertions) ---
        String actualSelectedCountry = registerPage.getSelectedCountryText();
        System.out.println("Country selected in UI: " + actualSelectedCountry);

        // Hard assert the expected country
        Assertions.assertEquals(
                expectedCountry,
                actualSelectedCountry,
                "Validation Failed: The selected country does not match!"
        );

        // Hard assert that an ID was actually fetched
        Assertions.assertFalse(
                availableId.isEmpty(),
                "Validation Failed: Available ID should not be empty!"
        );

        System.out.println("Positive Validation Passed!");

        // Screenshot ---
//        registerPage.takeFullPageScreenshot("positive_scenario.png");
        attachScreenshot("positive_scenario");
    }


    // NEGATIVE SCENARIO
    @Test
    @Story("Negative Registration")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Negative Registration Flow - Blank ID")
    @Description("Submits a blank Rediff ID and asserts that ID suggestions do not appear.")
    public void testNegativeRegistrationFlow() {
        System.out.println("--- Executing Negative Scenario ---");

        LoginPage loginPage = new LoginPage(page);
        RegisterPage registerPage = new RegisterPage(page);

        loginPage.navigateTo(config.get("baseUrl").getAsString());
        loginPage.clickCreateAccount();

        // Intentionally pass an empty string for the Rediff ID
        registerPage.fillPersonalDetails(testData.get("fullName").getAsString(), "");

        // Click "Check availability" directly

        page.locator("[value=\"Check availability\"]").click();

        // Wait a brief moment to let the page react
        page.waitForTimeout(1000);

        // Validations (Hard Assertions) ---
        boolean isSuggestionVisible = page.locator("#radio_login").isVisible();

        // Hard assert that the radio buttons did NOT show up
        Assertions.assertFalse(
                isSuggestionVisible,
                "Negative Validation Failed: Suggestions should NOT be visible when ID is blank!"
        );

        System.out.println("Negative Validation Passed!");

        // Screenshot ---
//        registerPage.takeFullPageScreenshot("negative_scenario.png");
        attachScreenshot("negative_scenario");
    }

    // Attaches a saved screenshot file to the Allure report.

    @io.qameta.allure.Attachment(value = "{name}", type = "image/png")
    private byte[] attachScreenshot(String name) {
        return page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions().setFullPage(true));
    }

    @AfterEach
    public void tearDown() {
        System.out.println("Closing browser...");
        DriverUtils.quitDriver();
    }
}