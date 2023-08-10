import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import org.apache.commons.codec.binary.Base64;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;

import com.fpt.ivs.at.core.keywords.mobilekeyword.ElementActionKeyword;
import com.fpt.ivs.at.core.object.Locator;
import com.fpt.ivs.at.core.object.UIObject;
import com.fpt.ivs.at.core.utilities.MobileDriverUtilities;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.imagecomparison.FeatureDetector;
import io.appium.java_client.imagecomparison.FeaturesMatchingResult;
import io.appium.java_client.imagecomparison.MatchingFunction;
import io.appium.java_client.imagecomparison.OccurrenceMatchingOptions;
import io.appium.java_client.imagecomparison.OccurrenceMatchingResult;
import javassist.compiler.ast.Keyword;
import net.bytebuddy.asm.Advice.Return;
import java.awt.image.BufferedImage;
import java.awt.*;
import org.apache.commons.io.FileUtils;
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

    public void getElementWithImage(UIObject target) throws IOException {
        String jsonDataString = target.toString();
        JsonObject jsonObject = JsonParser.parseString(jsonDataString).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("locatorList");
        for (JsonElement jsonElement : jsonArray) {
            if (jsonElement.getAsJsonObject().get("strategy").getAsString().equals("Image")) {
                AppiumDriver driver = MobileDriverUtilities.getDriver();
                String cleanBase64Data = jsonElement.getAsJsonObject().get("value").getAsString().replaceFirst("data:image/png;base64,", " ");

                byte[] partialImage = Base64.decodeBase64(cleanBase64Data);
                byte[] screenshot = ((TakesScreenshot) MobileDriverUtilities.getDriver()).getScreenshotAs(OutputType.BYTES);
       
                // Create an temporary file
                Path findwaldo = Files.createTempFile(null, ".png");
                Path waldo = Files.createTempFile(null, ".png");
                writeBytesToFileApache(waldo,partialImage);
                writeBytesToFileApache(findwaldo,screenshot);

                OccurrenceMatchingResult result = driver
                        .findImageOccurrence( new File(findwaldo.toString()),  new File(waldo.toString()), new OccurrenceMatchingOptions()
                        .withEnabledVisualization());
                // System.out.println(">>>>>>>>>>>>>");
                // System.out.println(result.getVisualization().length); 
                // System.out.println(">>>>>>>>>>>>>");

                Path rt = Files.createTempFile(null, ".png");
                System.out.println(">>>>>>>>>>>>>"+ rt );
                byte[] data = Base64.decodeBase64(result.getVisualization());
                writeBytesToFileApache(rt,data);
                ElementActionKeyword test = new ElementActionKeyword();
                test.tapWithCoordinates(result.getRect().getX() + (result.getRect().getWidth() /2), result.getRect().getY() + (result.getRect().getHeight() /2));
            }
        }
    }

 // Apache Commons IO
    public static void writeBytesToFileApache(Path fileOutput, byte[] bytes) throws IOException {
        FileUtils.writeByteArrayToFile(new File(fileOutput.toString()), bytes);
    }
    
// convert Image to BufferedImage
    public static BufferedImage convertToBufferedImage(Image img) {

        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bi = new BufferedImage(
                img.getWidth(null), img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = bi.createGraphics();
        graphics2D.drawImage(img, 0, 0, null);
        graphics2D.dispose();

        return bi;
    }
public void getColorInImages() throws IOException{
    AppiumDriver driver = MobileDriverUtilities.getDriver();
    byte[] screenshotBytes = ((TakesScreenshot) MobileDriverUtilities.getDriver()).getScreenshotAs(OutputType.BYTES);
    BufferedImage image = ImageIO.read(new ByteArrayInputStream (screenshotBytes));
    int height = image.getHeight(), width = image.getWidth();
    // Getting pixel color by position x and y 
    //int clr = image.getRGB(height/2, width/2);
    int clr = image.getRGB(477, 387);
    int red =   (clr & 0x00ff0000) >> 16;
    int green = (clr & 0x0000ff00) >> 8;
    int blue =   clr & 0x000000ff;
    System.out.println("Red Color value = " + red);
    System.out.println("Green Color value = " + green);
    System.out.println("Blue Color value = " + blue);
    }
}