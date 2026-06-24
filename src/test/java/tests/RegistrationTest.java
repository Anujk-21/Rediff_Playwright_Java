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
        try (FileReader dataReader = new FileReader("src/test/resources/testdata.json")) {
            testData = JsonParser.parseReader(dataReader).getAsJsonObject();
        }

        // Initialize Driver
        boolean isHeadless = config.get("headless").getAsBoolean();
        page = DriverUtils.initDriver(isHeadless);
    }


    // 1. POSITIVE SCENARIO (Happy Path)

    @Test
    @Story("Positive Registration")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Positive Registration Flow")
    @Description("Registers a user via the happy path and validates the selected country and generated ID.")
    public void testPositiveRegistrationFlow() {
        System.out.println("--- Executing Positive Scenario ---");

        // --- 1. Initialize Page Objects ---
        LoginPage loginPage = new LoginPage(page);
        RegisterPage registerPage = new RegisterPage(page);

        // --- 2. Extract Data ---
        String expectedCountry = testData.get("expectedCountry").getAsString();

        // --- 3. Execute Flow on Landing Page ---
        loginPage.navigateTo(config.get("baseUrl").getAsString());
        loginPage.clickCreateAccount();

        // --- 4. Execute Flow on Register Page ---
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

        // --- 5. Validations (Hard Assertions) ---
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

        // --- 6. Screenshot ---
        // By adding "screenshots/" it will create a folder outside src
//        registerPage.takeFullPageScreenshot("positive_scenario.png");
        attachScreenshot("positive_scenario");
    }


    // 2. NEGATIVE SCENARIO (Blank ID Input)
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

        // 1. Intentionally pass an empty string for the Rediff ID
        registerPage.fillPersonalDetails(testData.get("fullName").getAsString(), "");

        // 2. Click "Check availability" directly
        // We use page.locator directly here instead of registerPage.checkAvailabilityAndWait()
        // because that method contains a strict wait for the radio button to appear,
        // which would cause this negative test to fail with a timeout!
        page.locator("[value=\"Check availability\"]").click();

        // Wait a brief moment to let the page react
        page.waitForTimeout(1000);

        // --- 3. Validations (Hard Assertions) ---
        boolean isSuggestionVisible = page.locator("#radio_login").isVisible();

        // Hard assert that the radio buttons did NOT show up
        Assertions.assertFalse(
                isSuggestionVisible,
                "Negative Validation Failed: Suggestions should NOT be visible when ID is blank!"
        );

        System.out.println("Negative Validation Passed!");

        // --- 4. Screenshot ---
//        registerPage.takeFullPageScreenshot("negative_scenario.png");
        attachScreenshot("negative_scenario");
    }

    // Attaches a saved screenshot file to the Allure report.
    // Adjust the base path if your takeFullPageScreenshot saves elsewhere.
    // Captures the current page and attaches it straight to the Allure report.
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