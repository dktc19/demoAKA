package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import com.fpt.ivs.at.core.utilities.MobileDriverUtilities;
import com.fpt.ivs.at.core.utilities.MobileGestureUtilities;
import com.sun.jna.Pointer;

import io.appium.java_client.AppiumDriver;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.TessAPI1;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.ImageIOHelper;
import static net.sourceforge.tess4j.ITessAPI.TRUE;
import java.awt.Color;

public class TesseractOcrUtil {
    AppiumDriver driver = MobileDriverUtilities.getDriver();
    
    String dataPath = System.getProperty("project.path") +  File.separator + "business-keywords" +  File.separator + "custom-keywords" +  File.separator + "src" +  File.separator + "main" +  File.separator + "java" +  File.separator + "resources" + File.separator;
	String language = "vie";        
	/**
     * Scanning for the screen and find the expected vietnamese text,
     * then take invisible bounding box for the text and return its coordinates following parameters of confidence.
     *
     * For testPageIteratorLevel, there are 4 levels:
     *         number 0 represents for block scan.
     *         number 1 represents for parameter scan.
     *         number 2 represents for text line scan.
     *         number 3 represents for each word scan (default).
     *         number 4 represents for symbol scan.
     *
     * For testWordIteratorLevel, a certain word can be chosen when the screen has the iteration of that word. It counts from 0.
     *
     * @param text
     * @param testPageIteratorLevel
     * */
    public List getTextLocation(String text, Integer testPageIteratorLevel, Integer testWordIteratorLevel, Integer Confidence) throws IOException {
        System.out.println(">>> getWhiteTextLocation:"+ text + " - " + testPageIteratorLevel + " - " + testWordIteratorLevel + " - " + Confidence);
 
        ITessAPI.TessBaseAPI handle = TessAPI1.TessBaseAPICreate();
        List list = new ArrayList<>();
        List result = new ArrayList<>();
		
        byte[] screenshot = MobileDriverUtilities.captureScreenshot(driver);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(screenshot));

        assert image != null;
        ByteBuffer buf = ImageIOHelper.convertImageData(image);
        int bpp = image.getColorModel().getPixelSize();
        int bytespp = bpp / 8;
        int bytespl = (int) Math.ceil(image.getWidth() * bpp / 8.0);

        TessAPI1.TessBaseAPIInit3(handle, dataPath, language);
        TessAPI1.TessBaseAPISetPageSegMode(handle, ITessAPI.TessPageSegMode.PSM_AUTO);
        TessAPI1.TessBaseAPISetImage(handle, buf, image.getWidth(), image.getHeight(), bytespp, bytespl);

        ITessAPI.ETEXT_DESC monitor = new ITessAPI.ETEXT_DESC();
        TessAPI1.TessBaseAPIRecognize(handle, monitor);
        ITessAPI.TessResultIterator ri = TessAPI1.TessBaseAPIGetIterator(handle);
        ITessAPI.TessPageIterator pi = TessAPI1.TessResultIteratorGetPageIterator(ri);
        TessAPI1.TessPageIteratorBegin(pi);
        int level = 0;
        switch (testPageIteratorLevel) {
            case 0:
                level = TessAPI1.TessPageIteratorLevel.RIL_BLOCK;
                break;
            case 1:
                level = TessAPI1.TessPageIteratorLevel.RIL_PARA;
                break;
            case 2:
                level = TessAPI1.TessPageIteratorLevel.RIL_TEXTLINE;
                break;
            case 4:
                level = TessAPI1.TessPageIteratorLevel.RIL_SYMBOL;
                break;
            case 5:
                level = TessAPI1.TessPageIteratorLevel.RIL_WORD;
                break;
        }

        int countWord = 0;
        do {
            Pointer ptr = TessAPI1.TessResultIteratorGetUTF8Text(ri, level);
            String word = ptr.getString(0).toLowerCase();
            TessAPI1.TessDeleteText(ptr);
            float confidence = TessAPI1.TessResultIteratorConfidence(ri, level);
            IntBuffer leftB = IntBuffer.allocate(1);
            IntBuffer topB = IntBuffer.allocate(1);
            IntBuffer rightB = IntBuffer.allocate(1);
            IntBuffer bottomB = IntBuffer.allocate(1);
            TessAPI1.TessPageIteratorBoundingBox(pi, level, leftB, topB, rightB, bottomB);
            System.out.println(">>>"+ word + " - " + confidence);
            if (word.contains(text.toLowerCase()) && confidence >= Confidence) {
                int xOffset = (leftB.get() + rightB.get())/2;
                int yOffset = (topB.get() + bottomB.get())/2;
                list.add(xOffset);
                list.add(yOffset);
                countWord++;
            }
        }
        while (TessAPI1.TessPageIteratorNext(pi, level) == TRUE);
        System.out.println(">>>"+ list.toString());
        if (countWord > 1) {
            result.add(list.get(2*testWordIteratorLevel));
            result.add(list.get(2*testWordIteratorLevel + 1));
        } else {
            result.add(list.get(0));
            result.add(list.get(1));
        }
        return result;
    }

/**
     * Scanning for the screen and find the expected vietnamese text,
     * then take invisible bounding box for the text and return its coordinates following parameters of confidence.
     *
     * For testPageIteratorLevel, there are 4 levels:
     *         number 0 represents for block scan.
     *         number 1 represents for parameter scan.
     *         number 2 represents for text line scan.
     *         number 3 represents for each word scan (default).
     *         number 4 represents for symbol scan.
     *
     * For testWordIteratorLevel, a certain word can be chosen when the screen has the iteration of that word. It counts from 0.
     *
     * @param text
     * @param testPageIteratorLevel
     * */
    public List getWhiteTextLocation(String text, Integer testPageIteratorLevel, Integer testWordIteratorLevel, Integer Confidence) throws IOException {
	    System.out.println(">>> getWhiteTextLocation:"+ text + " - " + testPageIteratorLevel + " - " + testWordIteratorLevel + " - " + Confidence);

        ITessAPI.TessBaseAPI handle = TessAPI1.TessBaseAPICreate();
        List list = new ArrayList<>();
        List result = new ArrayList<>();

        byte[] screenshot = MobileDriverUtilities.captureScreenshot(driver);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(screenshot));

        assert img != null;
        //BufferedImage image = binary(img);
        BufferedImage image = transformBlackWhite(img);

        ByteBuffer buf = ImageIOHelper.convertImageData(image);
        int bpp = image.getColorModel().getPixelSize();
        int bytespp = bpp / 8;
        int bytespl = (int) Math.ceil(image.getWidth() * bpp / 8.0);

        TessAPI1.TessBaseAPIInit3(handle, dataPath, language);
        TessAPI1.TessBaseAPISetPageSegMode(handle, ITessAPI.TessPageSegMode.PSM_AUTO);
        TessAPI1.TessBaseAPISetImage(handle, buf, image.getWidth(), image.getHeight(), bytespp, bytespl);

        ITessAPI.ETEXT_DESC monitor = new ITessAPI.ETEXT_DESC();
        TessAPI1.TessBaseAPIRecognize(handle, monitor);
        ITessAPI.TessResultIterator ri = TessAPI1.TessBaseAPIGetIterator(handle);
        ITessAPI.TessPageIterator pi = TessAPI1.TessResultIteratorGetPageIterator(ri);
        TessAPI1.TessPageIteratorBegin(pi);
        int level = 0;
        switch (testPageIteratorLevel) {
            case 0:
                level = TessAPI1.TessPageIteratorLevel.RIL_BLOCK;
                break;
            case 1:
                level = TessAPI1.TessPageIteratorLevel.RIL_PARA;
                break;
            case 2:
                level = TessAPI1.TessPageIteratorLevel.RIL_TEXTLINE;
                break;
            case 3:
                level = TessAPI1.TessPageIteratorLevel.RIL_SYMBOL;
                break;
            case 4:
                level = TessAPI1.TessPageIteratorLevel.RIL_WORD;
                break;
        }

        int countWord = 0;
        do {
            Pointer ptr = TessAPI1.TessResultIteratorGetUTF8Text(ri, level);
            String word = ptr.getString(0).toLowerCase();
            TessAPI1.TessDeleteText(ptr);
            float confidence = TessAPI1.TessResultIteratorConfidence(ri, level);
            IntBuffer leftB = IntBuffer.allocate(1);
            IntBuffer topB = IntBuffer.allocate(1);
            IntBuffer rightB = IntBuffer.allocate(1);
            IntBuffer bottomB = IntBuffer.allocate(1);
            TessAPI1.TessPageIteratorBoundingBox(pi, level, leftB, topB, rightB, bottomB);
            System.out.println(">>>"+ word + " - " + confidence);
            if (word.contains(text.toLowerCase()) && confidence >= Confidence) {
                int xOffset = (leftB.get() + rightB.get())/2;
                int yOffset = (topB.get() + bottomB.get())/2;
                list.add(xOffset);
                list.add(yOffset);
                countWord++;
            }
        }
        while (TessAPI1.TessPageIteratorNext(pi, level) == TRUE);
        System.out.println(">>>"+ list.toString());
        if (countWord > 1) {
            result.add(list.get(2*testWordIteratorLevel));
            result.add(list.get(2*testWordIteratorLevel + 1));
        } else {
            result.add(list.get(0));
            result.add(list.get(1));
        }
        return result;
    }

    /**
     * Transform BufferedImage from color to binary (black and white)
     *
     * @param src
     *
     * */
    public static BufferedImage binary(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();

        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = src.getRGB(i, j);
                grayImage.setRGB(i, j, rgb);
            }
            /*www.java2s.com*/
        }
        return grayImage;
    }

    public static BufferedImage transformBlackWhite(BufferedImage img_input){
        try {
            BufferedImage image = img_input;

            // Get the width and height of the image
            int width = image.getWidth();
            int height = image.getHeight();

            // Loop through each pixel in the image
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Get the color of the current pixel
                    Color color = new Color(image.getRGB(x, y));

                    // Invert the color of the pixel
                    int red = 255 - color.getRed();
                    int green = 255 - color.getGreen();
                    int blue = 255 - color.getBlue();

                    // Set the new color of the pixel
                    Color newColor = new Color(red, green, blue);
                    image.setRGB(x, y, newColor.getRGB());
                }
            }

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getRGB(x, y);
                    if (pixel != Color.BLACK.getRGB()) {
                        image.setRGB(x, y, Color.WHITE.getRGB());
                    }
                }
            }
            // Save the inverted image for debug
            //String file_output= ReportConstants.REPORT_FOLDER + "output.png";
            //File output = new File(file_output);
           // ImageIO.write(image, "png", output);

           return image;

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return img_input;
    }

	public void ClickOCRByText(String expectedText,Integer testPageIteratorLevel, Integer testWordIteratorLevel, Integer confidence) throws IOException{
        int xCenter = (int) getTextLocation(expectedText,testPageIteratorLevel,testWordIteratorLevel,confidence).get(0);
		int yCenter = (int) getTextLocation(expectedText,testPageIteratorLevel,testWordIteratorLevel,confidence).get(1);
		driver.perform(Arrays.asList(MobileGestureUtilities.createTapAction(xCenter, yCenter)));
	}
    
	public void ClickOCRByWhiteText(String expectedText,Integer testPageIteratorLevel, Integer testWordIteratorLevel, Integer confidence) throws IOException{
        int xCenter = (int) getWhiteTextLocation(expectedText,testPageIteratorLevel,testWordIteratorLevel,confidence).get(0);
		int yCenter = (int) getWhiteTextLocation(expectedText,testPageIteratorLevel,testWordIteratorLevel,confidence).get(1);
		driver.perform(Arrays.asList(MobileGestureUtilities.createTapAction(xCenter, yCenter)));
	}

    /**
     * Return a string that includes all words in given image
     *
     * @param imageFile
     * */
    public void extractTextFromImage() throws TesseractException, IOException {
        Tesseract tesseract = new Tesseract();
        // The path to your trained data.
        tesseract.setDatapath(dataPath);
        tesseract.setLanguage(language);
        byte[] screenshot = MobileDriverUtilities.captureScreenshot(driver);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(screenshot));
       System.out.println(">>>"+ tesseract.doOCR(img));
        //System.out.println(">>>"+ tesseract.getWords(img));
    }

}