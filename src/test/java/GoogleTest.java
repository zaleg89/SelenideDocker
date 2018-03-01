import com.codeborne.selenide.Screenshots;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;
import ru.yandex.qatools.allure.annotations.Attachment;
import ru.yandex.qatools.allure.annotations.Step;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class GoogleTest {
    private static final String stationFrom = "Rivne";
    private static final String stationTo = "Lviv";
    private static final int futureDays = 8;

    @Rule
    public BrowserWebDriverContainer firefox =
            new BrowserWebDriverContainer()
                    .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, new File("./target/"))
                    .withDesiredCapabilities(DesiredCapabilities.firefox());

    @Before
    public void setUp() {
        RemoteWebDriver driver = firefox.getWebDriver();
        System.out.println(firefox.getVncAddress());
        WebDriverRunner.setWebDriver(driver);
    }

    @After
    public void tearDown() {
        WebDriverRunner.closeWebDriver();
    }

    @Test
    public void availableRailwayTicketsTest() throws InterruptedException, IOException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, futureDays);
        String date = dateFormat.format(calendar.getTime());

        // Setting start URL
        open("https://booking.uz.gov.ua/");
        // Interaction with elements
        $(By.xpath("//input[@name='from-title']")).shouldBe(visible).setValue(stationFrom).
                $(By.xpath("//li[contains(text(), '" + stationFrom + "')]")).shouldBe(visible).click(); // Set station from.
        $(By.xpath("//input[@name='to-title']")).shouldBe(visible).setValue(stationTo).
                $(By.xpath("//li[contains(text(), '" + stationTo + "')]")).shouldBe(visible).click(); // Set station to.
        Selenide.executeJavaScript("jQuery(document.getElementsByName('date-hover'))" +
                ".removeAttr('readonly')"); // make date input field editable.
        $(By.name("date-hover")).val(date).pressTab().click();
        $("td.current > a").click(); // select highlighted date from datepicker.
        $(By.xpath("//button[@type='submit']")).click(); // Press search button.
        validateResults();
        sleep(5000);
    }

    @Step("Trains list validation.")
    private void validateResults() throws IOException {
        $(By.id("train-list")).shouldBe(visible).scrollTo();
        screenshot();
    }

    @Attachment(type = "image/png")
    private byte[] screenshot() throws IOException {
        File screenshot = Screenshots.takeScreenShotAsFile();
        return com.google.common.io.Files.toByteArray(screenshot);
    }

}
