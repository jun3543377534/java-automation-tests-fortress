package academy.teenfuture.crse.utility;

import com.microsoft.playwright.*;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import org.junit.jupiter.api.*;

import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

public class FortressRegisterPWTest {

    private static final String RUN_TS = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    private static final Path SCREENSHOT_DIR = Paths.get(System.getProperty("user.dir"), "target", "screenshots", RUN_TS);
    private static final Path VIDEO_DIR      = Paths.get(System.getProperty("user.dir"), "target", "recording", "pw", RUN_TS);

    // 固定 viewport / 錄影尺寸，避免影片灰邊
    private static final int VIDEO_W = 1280;
    private static final int VIDEO_H = 720;

    private static final String GIBBERISH = "&$*&*&$*&*$";

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeEach
    void setup() throws Exception {
        Files.createDirectories(SCREENSHOT_DIR);
        Files.createDirectories(VIDEO_DIR);

        playwright = Playwright.create();
        browser = playwright.chromium().launch(new LaunchOptions()
                .setHeadless(false));

        Browser.NewContextOptions ctxOpts = new Browser.NewContextOptions()
                .setViewportSize(VIDEO_W, VIDEO_H)
                .setRecordVideoDir(VIDEO_DIR)
                .setRecordVideoSize(VIDEO_W, VIDEO_H);

        context = browser.newContext(ctxOpts);
        page = context.newPage();
    }

    @AfterEach
    void teardown() {
        if (context != null) context.close(); // 關閉後影片才會寫檔
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
        System.out.println("[Run] Screenshots => " + SCREENSHOT_DIR.toAbsolutePath());
        System.out.println("[Run] Videos      => " + VIDEO_DIR.toAbsolutePath());
    }

    @Test
    void registerEntry() {
        // 1) 首頁
        page.navigate("https://www.fortress.com.hk/zh-hk/");
        sleep2();
        closeIfVisible(".countdown-skip");
        closeIfVisible("#onetrust-accept-btn-handler, .onetrust-close-btn-handler");
        screenshot("01_home.png");

        // 2) 登入 / 註冊
        page.locator("a.login-link[href*='/login']").click();
        page.waitForURL(u -> u.contains("/login"), new Page.WaitForURLOptions().setTimeout(12000));
        sleep2();
        screenshot("02_login_page.png");

        // 3) 立即註冊
        page.locator("a.btn.btn-primary.registerBtn[href*='/register/main']").click();
        page.waitForURL(u -> u.contains("/register"), new Page.WaitForURLOptions().setTimeout(12000));
        sleep2();
        screenshot("03_register_page.png");

        Locator confirmBtn = page.locator("#registrationFormSubmitButton");

        // 4) **先按一次確認**
        clickIfEnabled(confirmBtn);   // 如果 disabled 就跳過，避免 timeout
        screenshot("04_clicked_confirm_once.png");

        // 5) 亂碼填入全部欄位
        typeSlow("#registrationForm__ftrhk_registrationForm_step1__3__firstName", GIBBERISH, 120); // 名字
        typeSlow("#registrationForm__ftrhk_registrationForm_step1__4__lastName",  GIBBERISH, 120); // 姓氏
        typeSlow("#registrationForm__ftrhk_registrationForm_step1__6__email",     GIBBERISH, 120); // 電郵
        typeSlow("#registrationForm__ftrhk_registrationForm_step1__11__mobileNumber", GIBBERISH, 120); // 手機
        typeSlow("input[name='passphrase']", GIBBERISH, 120); // 驗證碼
        typeSlow("#registrationForm__ftrhk_registrationForm_step1__15__password", GIBBERISH, 120); // 密碼
        screenshot("05_filled_gibberish.png");

        // 6) **再按一次確認**
        clickIfEnabled(confirmBtn);
        screenshot("06_clicked_confirm_twice.png");

        // 7) 選生日（先月後年）：1 月 -> 1980 年
        boolean monthOk = selectFromNgbDropdown(
                "#registrationForm__ftrhk_registrationForm_step1__5__dateOfBirth__MM",
                "1", "01", "1月", "一月");
        boolean yearOk = selectFromNgbDropdown(
                "#registrationForm__ftrhk_registrationForm_step1__5__dateOfBirth__YYYY",
                "1980");
        System.out.println("[DOB] Month=" + monthOk + ", Year=" + yearOk);
        screenshot("07_selected_dob.png");

        // 8) **最後再按一次確認**
        clickIfEnabled(confirmBtn);
        screenshot("08_clicked_confirm_last.png");

        System.out.println("[Info] Flow done: confirm -> gibberish -> confirm -> DOB(month then year) -> confirm. Test end.");
    }

    /* ================= Helpers ================= */
    private void clickIfEnabled(Locator button) {
        try {
            if (button.isVisible() && button.isEnabled()) {
                button.click();
                sleep2();
                System.out.println("[Confirm] Clicked enabled button.");
            } else {
                System.out.println("[Confirm] Button disabled or invisible, skip clicking.");
            }
        } catch (PlaywrightException e) {
            System.out.println("[Confirm] Click skipped: " + e.getMessage());
        }
    }

    private boolean selectFromNgbDropdown(String buttonSelector, String... textsToTry) {
        try {
            Locator btn = page.locator(buttonSelector);
            btn.click();
            // ng-bootstrap 會開一個 .dropdown-menu.show
            Locator menu = page.locator("css=.dropdown-menu.show");
            if (menu.count() == 0) {
                // 再等等（有些時候需要）
                page.waitForSelector(".dropdown-menu.show", new Page.WaitForSelectorOptions().setTimeout(2000));
                menu = page.locator("css=.dropdown-menu.show");
            }
            if (menu.count() == 0) {
                System.out.println("[Dropdown] menu not found for: " + buttonSelector);
                return false;
            }

            for (String txt : textsToTry) {
                Locator opt = menu.locator("xpath=.//*[normalize-space(text())='" + txt + "']").first();
                if (opt.count() > 0) {
                    try { opt.scrollIntoViewIfNeeded(); } catch (PlaywrightException ignored) {}
                    opt.click();
                    sleep2();
                    return true;
                }
            }
            System.out.println("[Dropdown] none of texts matched for: " + buttonSelector);
            return false;
        } catch (PlaywrightException e) {
            System.out.println("[Dropdown] error: " + e.getMessage());
            return false;
        }
    }

    private void closeIfVisible(String css) {
        Locator loc = page.locator(css).first();
        try {
            if (loc.isVisible()) {
                loc.click(new Locator.ClickOptions().setTimeout(1000));
            }
        } catch (PlaywrightException ignored) {}
    }

    private void screenshot(String name) {
        page.screenshot(new Page.ScreenshotOptions().setPath(SCREENSHOT_DIR.resolve(name)));
        System.out.println("[Screenshot] Saved: " + SCREENSHOT_DIR.resolve(name).toAbsolutePath());
    }

    private void typeSlow(String selector, String text, int delayMs) {
        Locator loc = page.locator(selector);
        loc.click();
        loc.fill("");
        page.type(selector, text, new Page.TypeOptions().setDelay(delayMs));
    }

    private void sleep2() {
        try { Thread.sleep(Duration.ofSeconds(2).toMillis()); } catch (InterruptedException ignored) {}
    }
}
