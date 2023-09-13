package com.leaftaps.testcases;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SearchMoleculeDemo {

	static ChromeDriver driver = null;

	public static void openApplication() {
		System.setProperty("webdriver.chrome.silentOutput", "true");

		ChromeOptions options = new ChromeOptions();

		options.addArguments("--remote-allow-origins=*");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--disable-notifications");
		// options.addArguments("--headless");

		driver = new ChromeDriver(options);

		String website = "https://www.netmeds.com/";

		driver.get(website);

		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
	}

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub

		openApplication();

		Map<String, List<String>> map_AllResults = getAllCardText();

		System.out.println(map_AllResults);
		
		Map<String,Map<String,List<String>>> wholeResults = getAllDetails(map_AllResults);
		
		System.out.println(wholeResults);

		writeAllResults_Excel(wholeResults);
		
		driver.close();

	}
	
	public static Map<String,Map<String,List<String>>> getAllDetails(Map<String, List<String>> map) throws InterruptedException
	{
		
		Map<String,Map<String,List<String>>> mapWholeDetails = new LinkedHashMap();
		
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		
		
		
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			String key = entry.getKey();
			List<String> values = entry.getValue();

			Map<String,List<String>> mapAllDetails = new LinkedHashMap();

			// Write list values to subsequent columns
			
			for (int i = 0; i < values.size(); i++) {
				List<String> lst = new ArrayList();
				try {
					driver.findElement(By.xpath("//input[@id='search']")).sendKeys(values.get(i), Keys.ENTER);
					Thread.sleep(1000);
					wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//div[@id='algolia_hits']//img[@class='product-image-photo']"))));
					String productName = values.get(i).trim().replace("'s", "").replace("'S", "");
					String xpathExpression = "//div[@id='algolia_hits']//img[@class='product-image-photo' and contains(@alt,'"+productName+"')]";
					driver.findElement(By.xpath(xpathExpression)).click();
					Thread.sleep(300);
					wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//div[@class='essentials']//span[@class='final-price']"))));
					
					String Mrp = driver.findElement(By.xpath("//div[@class='essentials']//span[@class='final-price']")).getText();
					String xpath_Mrp = "//div[@class='essentials']//span[@class='final-price']";
					String MRPFetchedText = getElementText(xpath_Mrp,"MRP");
					lst.add(MRPFetchedText);
					
					String xpath_RxRequired = "//div[@class='product-detail']//span[@class='req_Rx']";
					String RxRequiredFetchedText = getElementText(xpath_RxRequired,"RxRequired");
					lst.add(RxRequiredFetchedText);
					
					String xpath_MKT = "//div[@class='essentials']//span[@class='drug-manu']/a";
					String MKTFetchedText = getElementText(xpath_MKT,"MKT");
					lst.add(MKTFetchedText);
					
					String xpath_CoutryOfOrigin = "//div[@class='essentials']//span[@class='drug-manu ellipsis origin_text']";
					String CountryOfOriginText = getElementText(xpath_CoutryOfOrigin,"Country Of origin");
					lst.add(CountryOfOriginText);
					
					String xpath_GenericName = "//div[@id='choose-generic-substitutes-block']//div[@class='drug-conf']";
					String GenericNameText = getElementText(xpath_GenericName,"Generic Name");
					lst.add(GenericNameText);
					
					String xpath_Synopsis = "//h2[text()='SYNOPSIS']/..//tr";
					String synopsis = getSynopsis_StoreInList(xpath_Synopsis);
					lst.add(synopsis);
				
				}
				catch(NoSuchElementException e)
				{
					e.printStackTrace();
					System.out.println(e.getMessage()+" Error in fetching element - "+values.get(i));
					lst.add(" Error in fetching element - "+values.get(i));
					
				}
				mapAllDetails.put(values.get(i), lst);
				
			}
			
			mapWholeDetails.put(key, mapAllDetails);
			
			
		}
		return mapWholeDetails;
	}

	
	//Reads the excel workbook and write all the Map contents to that Excel workbook
	//--------------------------------------------------------------------------------
	public static void writeAllResults_Excel(Map<String, Map<String, List<String>>> map) {

		String fileName = "./QATasksData/AllResults.xlsx";
		FileInputStream in = null;
		FileOutputStream outputStream = null;
		XSSFWorkbook workbook = null;
		XSSFSheet sheet = null;

		if (new File(fileName).exists()) {

			try {
				in = new FileInputStream(fileName);
				workbook = new XSSFWorkbook(in);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			sheet = workbook.getSheet("Sheet1");
		} else {
			workbook = new XSSFWorkbook();
			sheet = workbook.createSheet("Sheet1");
		}

		XSSFRow row = null;
		XSSFCell cell = null;
		int rowCount = sheet.getLastRowNum() + 1;

		int rowNum = 1;
		row = sheet.createRow(0);
        row.createCell(0).setCellValue("Molecule");
        row.createCell(1).setCellValue("Alternates");
        row.createCell(2).setCellValue("Other details");
		
		
        // Iterate through the outer map
        for (Entry<String, Map<String, List<String>>> outerEntry : map.entrySet()) {
            String outerKey = outerEntry.getKey();
            Map<String, List<String>> innerMap = outerEntry.getValue();

            // Iterate through the inner map
            for (Map.Entry<String, List<String>> innerEntry : innerMap.entrySet()) {
                String innerKey = innerEntry.getKey();
                List<String> innerList = innerEntry.getValue();

                // Iterate through the list
                for (String value : innerList) {
                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(outerKey);
                    row.createCell(1).setCellValue(innerKey);
                    row.createCell(2).setCellValue(value);
                }
            }
         
        }

		try {

			outputStream = new FileOutputStream(fileName);

			workbook.write(outputStream);
			
			workbook.close();
			outputStream.close();
			System.out.println("Excel file created successfully.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
		 * try { outputStream = new FileOutputStream(fileName); // write data in the
		 * excel file //workbook.write(outputStream); } catch (FileNotFoundException e)
		 * { // TODO Auto-generated catch block e.printStackTrace(); } catch
		 * (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 */

	}

	public static Map<String, List<String>> getAllCardText() throws InterruptedException {
		// Get the objects of workbook and sheet:
		// ------------------------------------------

		String fileName = "./QATasksData/SourceData.xlsx";
		FileInputStream in = null;
		XSSFWorkbook workbook = null;
		XSSFSheet sheet = null;

		if (new File(fileName).exists()) {

			try {
				in = new FileInputStream(fileName);
				workbook = new XSSFWorkbook(in);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			sheet = workbook.getSheet("Sheet1");
		} else {
			workbook = new XSSFWorkbook();
			sheet = workbook.createSheet("Sheet1");
		}

		XSSFCellStyle style = workbook.createCellStyle();

		XSSFRow row = null;
		XSSFCell cell = null;
		int rowCount = sheet.getLastRowNum() + 1;

		// System.out.println("Total rows in Excel : " + rowCount);

		// Iterate from Row 1 and get all texts from app and store all cards text in a
		// Map
		// -------------------------------------------------------------------------------

		Map<String, List<String>> mapAllResults_CardsText = new LinkedHashMap();
		
		
		for (int i = 1; i < rowCount; i++) {
			row = sheet.getRow(i);
			String molecule = row.getCell(0).getStringCellValue();

			driver.findElement(By.xpath("//input[@id='search']")).sendKeys(molecule, Keys.ENTER);
			
			List<WebElement> eachCard = driver.findElements(By.xpath("//div[@class='product-list']//li"));
		        
	        scrollTillLastCard(eachCard);

			List<WebElement> lstAllResults_Cards = driver.findElements(By.xpath("//div[@id='algolia_hits']//a[@class='category_name']"));
			List<String> lstAllResults_CardsText = new ArrayList();

			for (int j = 0; j < lstAllResults_Cards.size(); j++) {
				String text = lstAllResults_Cards.get(j).getAttribute("title");
				lstAllResults_CardsText.add(text);

			}

			mapAllResults_CardsText.put(molecule, lstAllResults_CardsText);

		}

		// cell = row.createCell(1);
		// cell.setCellValue(i);

		// Close File input stream and Excel sheet:
		// ---------------------------------------------

		try {
			in.close();
			workbook.close();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return mapAllResults_CardsText;

	}
	
	
	public static String getElementText(String xpath,String ElementName)
	{
		String text = "";
		try
		{
			WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(20));
			wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath(xpath))));
			
			text = driver.findElement(By.xpath(xpath)).getText();
		}
		catch(NoSuchElementException e)
		{
			text = "No element Found "+ElementName;
		}
		
		return ElementName +"---> "+text;
		
	}
	
	public static String getSynopsis_StoreInList(String xPath)
	{
		String text = "";
		try
		{
			WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(20));
			wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath(xPath))));
			List<WebElement> lstAllElements = driver.findElements(By.xpath(xPath));
			int count = lstAllElements.size();
			
			
			
			for(int i=0;i<count;i++)
			{
				text = lstAllElements.get(i).getText()+" ; "+text;
			}
			
			
		}
		catch(NoSuchElementException e)
		{
			text = "No element Found - Synopsis";
		}
		
		return "Synopsis ---> "+text;
		
	}
	
	//div[@class="product-detail"]//span[@class="req_Rx"]
	
	
	public static void scrollTillLastCard(List<WebElement> lst) throws InterruptedException
	{
		int initialCardCount = lst.size();

        // Scroll to the bottom of the page
		JavascriptExecutor js = (JavascriptExecutor) driver;

        // Define a maximum timeout (e.g., 30 seconds) for waiting for new cards to load
        long maxWaitTimeInSeconds = 1000;
        long startTime = System.currentTimeMillis();
        
        System.out.println("Started at Time (in Ms) : "+startTime);

        // Periodically check if new cards have loaded
        while (true) {
            // Get the current count of cards/items
        	int previousCardCount = driver.findElements(By.xpath("//div[@class='product-list']//li")).size();
        	//js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        	
        	js.executeScript("window.scrollBy(0, 300);");
        	// Calculate the target scroll position (scrollable height - 10% of window height)
            //long windowHeight = (long) js.executeScript("return window.innerHeight;");
            //long scrollHeight = (long) js.executeScript("return Math.max( document.documentElement.scrollHeight, document.body.scrollHeight );");
            //long targetScrollPosition = (long) (0.90 * scrollHeight);
            
            Thread.sleep(1000);
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

            // Scroll to the target position
            //js.executeScript("window.scrollTo(0, arguments[0]);", targetScrollPosition);
            
            List<WebElement> lstAllProductList = driver.findElements(By.xpath("//div[@class='product-list']//li"));
            
            Thread.sleep(1000);
            js.executeScript("arguments[0].scrollIntoView();", driver.findElements(By.xpath("//div[@class='product-list']//li")).get(lstAllProductList.size()-1));

            if(driver.findElements(By.xpath("//button[@class='ais-InfiniteHits-loadMore ais-InfiniteHits-loadMore--disabled']")).size()>0)
               	break;
            

            int currentCardCount = driver.findElements(By.xpath("//div[@class='product-list']//li")).size();

            // Check if new cards have loaded (compare with initial count)
            if (previousCardCount == currentCardCount)               
                break;
            

            // Check if the maximum wait time has been exceeded
            long currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - startTime;
            System.out.println("Current Time - StartTime (in Ms) : "+timeDiff);
            if ((currentTime - startTime) >= (maxWaitTimeInSeconds * 1000)) 
                break;
            

            // Sleep for a short duration (e.g., 1 second) before checking again
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
	}

}
