package pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import utils.RediffLocators;

public class LoginPage {
    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    public void navigateTo(String url) {
        System.out.println("Navigating to URL: " + url);
        // Wait only for DOMContentLoaded

        page.navigate(url, new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                .setTimeout(60000));
    }

    public void clickCreateAccount() {
        System.out.println("Clicking 'Create a new account' link...");
        page.locator(RediffLocators.CREATE_ACCOUNT_LINK).first().click();
    }
}