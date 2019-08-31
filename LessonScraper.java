import java.lang.reflect.Type;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

/*
 * This class does all the work of kicking off the scraping to various threads and
 * then saves the lesson data to disk.
 * Uses JSoup library for extracting and parsing the needed pieces of the lessons.
 * Uses Gson library for converting the Lesson objects to JSON. 
 */
public class LessonScraper {
    
    private String mainUrl;
    private String domQuery;
    
    // Thread-safe version of ArrayList.
    CopyOnWriteArrayList<Lesson> lessons = new CopyOnWriteArrayList<Lesson>();
    
    private static final String OUTPUT_FILE_NAME = "lessons.json";
    private static final int NUM_THREADS = 30;
    private static final int TERMINATION_TIME = 60;
    
    // Constructor.
    public LessonScraper(String url, String query) {
        mainUrl = url;
        domQuery = query;
    }

    // Kicks off the various pieces of execution:
    // 1. Get the urls of the lessons.
    // 2. Parse the lessons into objects.
    // 3. Save the lesson objects to disk as JSON.
    public void scrape() {
        
        // Extracts the list of lessons from the main page.
        Elements lessonUrls = getLessonUrls(mainUrl, domQuery);
        
        // Extracts the lesson data from the lesson pages and stores as Lesson objects in an arraylist.
        getLessons(lessonUrls);

        // Prints the arraylist of lessons to a JSON file.
        printJson(lessons);
    }
    
    // Extracts the lesson urls from the page with the list of lessons.
    private Elements getLessonUrls(String url, String domQuery) {
        
        // Load the main page with the list of lessons. 
        Document doc = LessonsRunnable.getPage(url);
 
        // Grab the the links to the lessons.
        return doc.select(domQuery);
    }
    
    // Extracts and parses the lesson data from their individual pages using NUM_THREADS threads.
    // The use of threads reduces the overhead of the network requests significantly.
    private void getLessons(Elements lessonUrls) {
        
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        
        // Iterate through the lesson urls to load the lesson page and extract, parse, and save the lesson content.
        // The lesson number and title come from the main list of lessons, and the lesson content come from
        // drilling down into each lesson page and extracting and manipulating/parsing the HTML for each less page.
        for (int i = 0; i < lessonUrls.size(); i++) {
            
            Runnable worker = new LessonsRunnable(lessonUrls.get(i), i + 1, lessons);
            executor.execute(worker);
            
            // Debugging purposes.
            /*if (i == 15) {
                break;
            }*/
        }
        
        executor.shutdown();
        
        // Wait for all threads to complete.  
        // If they're not done in 60 seconds, there's something wrong! 
        try {
            executor.awaitTermination(TERMINATION_TIME,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    // Converts an arraylist of lessons to JSON and writes to a file.
    private void printJson(CopyOnWriteArrayList<Lesson> lessonsArrayList) {

        // Because of the multithreading, the lessons don't get added in the order of their id.
        // Sort them then convert to json.
        Collections.sort(lessonsArrayList);
        String jsonOutPut = buildJsonOutput(lessonsArrayList);
        writeToFile(jsonOutPut);
    }
    
    // Converts arraylist of Lesson objects to JSON.
    private String buildJsonOutput(CopyOnWriteArrayList<Lesson> lessons2) {
     
        // Prevent Gson from encoding HTML characters.
        GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping();
        
        // Tells Gson to turn the arraylist of Lessons to JSON objects.
        Type listOfLessonObject = new TypeToken<ArrayList<Lesson>>(){}.getType();
        return gsonBuilder.create().toJson(lessons2, listOfLessonObject);
    }
    
    // Writes JSON to a file.
    private void writeToFile(String json) {
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(OUTPUT_FILE_NAME);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.print(json);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
