package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.net.URL;
import java.net.URLConnection;

import org.openqa.selenium.MutableCapabilities;

import com.fpt.ivs.at.core.utilities.MobileDriverUtilities;

import io.appium.java_client.AppiumDriver;

import com.fpt.ivs.at.core.Constants;
import com.fpt.ivs.at.core.driver.WebdriverSocketClient;
public class installapp {
    

public void install_apk(String fileURL) throws InterruptedException{
    
    String directoryPath = System.getProperty("project.path") +  File.separator + "business-keywords" +  File.separator + "custom-keywords" +  File.separator + "src" +  File.separator + "main" +  File.separator + "java" +  File.separator + "resources" + File.separator;
    String saveFilePath = directoryPath + "intallapp.apk";
    Boolean hasAPK = false;
    File directory = new File(directoryPath);
    
    if (!directory.exists() || !directory.isDirectory()) {
        System.out.println("Invalid directory path");
    }

    File[] files = directory.listFiles();
    if (files == null) {
        System.out.println("Error accessing directory");
    }

    for (File file : files) {
        if (file.isFile() && file.getName().endsWith(".apk")) {
            hasAPK = true;
        }
    }
    
     if(hasAPK == false){
        try {
            downloadFileFromURL(fileURL, saveFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("File download failed.");
        }
    }


    AppiumDriver driver = MobileDriverUtilities.getDriver();
    String deviceID = driver.getCapabilities().getCapability("udid").toString();
    System.out.println(">>>>>>>>>>>>>>>>> "+ deviceID);
    
        
    String cmd = "adb -s "+ deviceID +" install "+ saveFilePath;
    System.out.println(">>>>>>>>>>>>>>>>> "+ cmd);
    runCmd(cmd);
   
   Thread.sleep(5000);
}
    
    public static void downloadFileFromURL(String fileURL, String saveFilePath) throws IOException {
        URL url = new URL(fileURL);
        URLConnection connection;

        if(System.getProperty("http.proxyHost").isEmpty()){
            connection = url.openConnection();
        }else{
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort"))));
            connection = url.openConnection(proxy);
        }

        InputStream inputStream = connection.getInputStream();

        try (FileOutputStream outputStream = new FileOutputStream(saveFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("File downloaded successfully.");
    }

    public static void runCmd(String inputString) {
        try {
            Runtime.getRuntime().exec(inputString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}