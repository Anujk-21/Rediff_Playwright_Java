package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;
import utils.RediffLocators;

import java.nio.file.Paths;
import java.util.List;

public class RegisterPage {
    private final Page page;

    public RegisterPage(Page page) {
        this.page = page;
    }

    public void fillPersonalDetails(String fullName, String rediffId) {
        page.locator(RediffLocators.FULL_NAME_INPUT).fill(fullName);
        page.locator(RediffLocators.REDIFF_ID_INPUT).fill(rediffId);

    }



    public void checkAvailabilityAndWait() {
        Locator checkBtn = page.locator(RediffLocators.CHECK_AVAILABILITY_BTN);

        System.out.println("Explicit Wait: Waiting for background scripts to finish loading...");
        // 1. Wait until there are no active network connections for at least 500ms.       // This guarantees Rediff's messy JavaScript is fully attached to the page.
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);

        // 2. Explicitly wait for the button itself to be fully visible and ready
        checkBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        System.out.println("Clicking Check Availability...");
        // Now that the network is quiet and the button is ready, we click once.
        checkBtn.click();

        System.out.println("Waiting for dynamic AJAX elements to load...");
        // 3. Wait for the success element (the radio button) to appear
        page.locator(RediffLocators.RADIO_LOGIN).first()
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public String getFirstAvailableId() {
        return page.locator(RediffLocators.RADIO_LOGIN).first().getAttribute("value");
    }

    public void selectFirstSuggestedId() {
        page.locator(RediffLocators.RADIO_LOGIN).first().check();
    }

    public void fillPassword(String password) {
        page.locator(RediffLocators.PASSWORD_INPUT).fill(password);
    }

    public void checkNoAlternateId() {
        page.locator(RediffLocators.ALT_EMAIL_CHECKBOX).check();
    }

    public void selectDOB(String day, String month, String year) {
        page.locator(RediffLocators.DOB_DROPDOWNS).nth(0).selectOption(day);
        page.locator(RediffLocators.DOB_DROPDOWNS).nth(1).selectOption(new SelectOption().setLabel(month));
        page.locator(RediffLocators.DOB_DROPDOWNS).nth(2).selectOption(year);
    }

    public void clickCountryDropdown() {
        page.locator(RediffLocators.COUNTRY_DROPDOWN).click();
    }

    public void printAvailableCountries() {
        Locator countryOptions = page.locator(RediffLocators.COUNTRY_DROPDOWN + " option");
        List<String> allCountries = countryOptions.allInnerTexts();

        System.out.println("\n--- Available Countries ---");
        allCountries.forEach(System.out::println);
        System.out.println("\nTotal count of countries: " + allCountries.size());
    }

    public void selectCountry(String expectedCountry) {
        page.locator(RediffLocators.COUNTRY_DROPDOWN).selectOption(new SelectOption().setLabel(expectedCountry));
    }

    public String getSelectedCountryText() {
        return (String) page.locator(RediffLocators.COUNTRY_DROPDOWN).evaluate("el => el.options[el.selectedIndex].text");
    }

    public void takeFullPageScreenshot(String fileName) {

        java.nio.file.Path screenshotPath = java.nio.file.Paths.get("screenshots", fileName);

        System.out.println("Taking full page screenshot...");
        page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                .setPath(screenshotPath)
                .setFullPage(true));

        System.out.println("Screenshot saved at: " + screenshotPath.toAbsolutePath());
    }
}