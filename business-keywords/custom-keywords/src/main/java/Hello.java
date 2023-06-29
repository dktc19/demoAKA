import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.fpt.ivs.at.core.object.Locator;
import com.fpt.ivs.at.core.object.UIObject;
import com.fpt.ivs.at.core.utilities.MobileDriverUtilities;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.imagecomparison.FeaturesMatchingResult;
import io.appium.java_client.imagecomparison.OccurrenceMatchingOptions;
import io.appium.java_client.imagecomparison.OccurrenceMatchingResult;
import net.bytebuddy.asm.Advice.Return;

public class Hello {

    public String test() {
        return "Hello World!!";
    }

    public void printallproperties() {
        System.out.println("-----------------------------------");
        Properties properties = System.getProperties();
        properties.forEach((k, v) -> System.out.println(k + ":" + v));
        System.out.println("-----------------------------------");
    }

    public void printPageSource() {
        AppiumDriver driver = MobileDriverUtilities.getDriver();
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
        } // just for sure 10 sec to load page
        System.out.println(driver.getPageSource());
    }

    public String getPageSource() {
        AppiumDriver driver = MobileDriverUtilities.getDriver();
        String result = "Can't get page source";
        try {
            Thread.sleep(10000);
            result = driver.getPageSource();
            result = result.trim().replaceFirst("^([\\W]+)<", "<");
        } catch (Exception e) {
        } // just for sure 10 sec to load page
        // System.out.println();
        return result;
    }

    public void printValue(String a) {
        System.out.println(">>>: " + a);
    }

    // public void printValue(String a){
    // byte[] screenshot =
    // Base64.encodeBase64(driver.getScreenshotAs(OutputType.BYTES));
    // FeaturesMatchingResult result = driver
    // .matchImagesFeatures(screenshot, originalImg, new FeaturesMatchingOptions()
    // .withDetectorName(FeatureDetector.ORB)
    // .withGoodMatchesFactor(40)
    // .withMatchFunc(MatchingFunction.BRUTE_FORCE_HAMMING)
    // .withEnabledVisualization());
    // }

    public void getElementWithImage(UIObject target) {
        String jsonDataString = target.toString();
        JsonObject jsonObject = JsonParser.parseString(jsonDataString).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("locatorList");
        for (JsonElement jsonElement : jsonArray) {
            if (jsonElement.getAsJsonObject().get("strategy").getAsString().equals("Image")) {
                AppiumDriver driver = MobileDriverUtilities.getDriver();
                String cleanBase64Data = jsonElement.getAsJsonObject().get("value").getAsString().replaceFirst("data:image/png;base64,", "");
                System.out.println("cleanBase64Data>>>>>>>>>>>>>>>>>>" + cleanBase64Data);
                byte[] partialImage = Base64.getDecoder().decode(cleanBase64Data);
                byte[] screenshot = Base64.getDecoder().decode(MobileDriverUtilities.captureScreenshot(driver));

                System.out.println("screenshot >>>>>>>>>>>>>>>>>>" + screenshot);
                OccurrenceMatchingResult result = driver
                        .findImageOccurrence(screenshot, partialImage, new OccurrenceMatchingOptions()
                                .withEnabledVisualization());
                System.out.println(result.getVisualization().length); 
                System.out.println(result.getRect());
            }
        }
    }

}