import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class TestDriver {
    public static void main(String[] args) {
        System.out.println("Starting Chrome setup...");
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            WebDriver driver = new ChromeDriver(options);
            System.out.println("Chrome started successfully.");
            driver.get("http://anugrah_jaya.test/app/index.html");
            System.out.println("Page title: " + driver.getTitle());
            driver.quit();
            System.out.println("Done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
