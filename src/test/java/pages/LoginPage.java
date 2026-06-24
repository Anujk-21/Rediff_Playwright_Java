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
        // Wait only for DOMContentLoaded (HTML parsed) instead of the full "load"
        // event. On heavy third-party sites the "load" event may never fire within
        // the timeout because of ads/trackers, especially from CI runner IPs.
        page.navigate(url, new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                .setTimeout(60000));
    }

    public void clickCreateAccount() {
        System.out.println("Clicking 'Create a new account' link...");
        page.locator(RediffLocators.CREATE_ACCOUNT_LINK).first().click();
    }
}