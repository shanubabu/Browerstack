import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import com.google.cloud.translate.*;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.NoSuchElementException;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import  com.google.cloud.translate.TranslateOptions.*;

import static com.google.cloud.translate.TranslateOptions.getDefaultInstance;


public class assign {

    public static void main(String[] args) throws InterruptedException, IOException {

        System.setProperty("webdriver.chrome.driver", "C://Users//user//Downloads//chromedriver-win64//chromedriver.exe"); //e.g C:\\Users\\user\\Downloads\\chromedriver-win64\\chromedriver.exe
        String browser = "chrome";

        WebDriver driver = initializeDriver(browser);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // Explicit wait

        try {
            driver.get("https://elpais.com/");

            // Wait for the 'Opinión' link to be clickable
            WebElement opinionLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Opinión')]")));
            opinionLink.click();

            // Wait for articles to load
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@id=\"csw\"]/div[1]/nav/div/a[2]')]")));
            List<WebElement> articleElements = driver.findElements(By.xpath("//*[@id=\"csw\"]/div[1]/nav/div/a[2]"));


            List<String> articleTitles = new ArrayList<>();
            List<String> articleContents = new ArrayList<>();
            List<String> articleImages = new ArrayList<>();

            for (WebElement article : articleElements.subList(1, Math.min(5, articleElements.size()))) { // Handle cases with fewer than 5 articles
                try {

                    String title = article.findElement(By.xpath("/html/body/main/div[1]/section[1]/div[4]/div/article[1]/header/h2/a")).getText();
                    String content = article.findElement(By.xpath("/html/body/main/div[1]/section[1]/div[4]/div/article[1]")).getText();
                    String image = article.findElement(By.xpath("/html/body/article/header/div[2]/figure/span/img")).getAttribute("src");

                    articleTitles.add(title);
                    articleContents.add(content);
                    articleImages.add(image);

                    downloadImage(image, "image" + articleTitles.size() + ".jpg");
                } catch (NoSuchElementException e) {
                    System.err.println("Element not found in article: " + e.getMessage());
                    // Handle the case where an element isn't found within an article
                }
            }

            List<String> translatedTitles = translateTitles(articleTitles);
            Map<String, Integer> wordCounts = analyzeHeaders(translatedTitles);

            for (int i = 0; i < articleTitles.size(); i++) {
                System.out.println("Article " + (i + 1) + ":");
                System.out.println("Title (Spanish): " + articleTitles.get(i));
                System.out.println("Title (English): " + translatedTitles.get(i));
                System.out.println("Content: " + articleContents.get(i));
                System.out.println("Image: " + articleImages.get(i));
                System.out.println();
            }

            System.out.println("Repeated Words:");
            wordCounts.forEach((word, count) -> {
                if (count > 1) { // Changed to 1 to show all repeated words
                    System.out.println(word + ": " + count);
                }
            });

        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private static WebDriver initializeDriver(String browser) {
        WebDriver driver;
        switch (browser.toLowerCase()) {
            case "chrome":
                driver = new ChromeDriver();
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
        return driver;
    }

    public static List<String> translateTitles(List<String> titles) {
        if (titles == null || titles.isEmpty()) {
            return new ArrayList<>(); // Return empty list to avoid null pointer
        }
        List<String> translatedTitles = new ArrayList<>();
        Translate translate = getDefaultInstance().getService();
        for (String title : titles) {
            try {
                Translation translation = translate.translate(title, Translate.TranslateOption.sourceLanguage("es"), Translate.TranslateOption.targetLanguage("en"));
                translatedTitles.add(translation.getTranslatedText());
            } catch (TranslateException e) {
                System.err.println("Translation error: " + e.getMessage());
                translatedTitles.add("Translation Failed"); // Add a placeholder
            }
        }
        return translatedTitles;
    }

    public static Map<String, Integer> analyzeHeaders(List<String> translatedTitles) {
        Map<String, Integer> wordCounts = new ConcurrentHashMap<>();
        Pattern pattern = Pattern.compile("\\b\\w+\\b"); // Use word boundaries to avoid partial word matches
        for (String title : translatedTitles) {
            if (title != null) { // Check for null titles after translation errors
                Matcher matcher = pattern.matcher(title.toLowerCase());
                while (matcher.find()) {
                    String word = matcher.group();
                    wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
                }
            }
        }
        return wordCounts;
    }

    public static void downloadImage(String imageUrl, String fileName) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            System.err.println("Image URL is empty or null, skipping download.");
            return; // Skip download if URL is invalid
        }
        try (BufferedInputStream in = new BufferedInputStream(new URL(imageUrl).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            System.err.println("Error downloading image: " + e.getMessage());
        }
    }
}