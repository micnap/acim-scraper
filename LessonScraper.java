import java.lang.reflect.Type;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeFilter;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

/*
 * This is the class that does all the work of scraping the pages for the data.
 * Uses JSoup library for extracting and parsing the needed pieces of the lessons.
 * Uses Gson library for converting the Lesson objects to JSON. 
 */
public class LessonScraper {
    
    private String mainUrl;
    private String domQuery;
    
    private static final String OUTPUT_FILE_NAME = "lessons.json";
    
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
        ArrayList<Lesson> lessons = getLessons(lessonUrls);

        // Prints the arraylist of lessons to a JSON file.
        printJson(lessons);
    }
    
    // Extracts the lesson urls from the page with the list of lessons.
    private Elements getLessonUrls(String url, String domQuery) {
        
        // Load the main page with the list of lessons. 
        Document doc = getPage(url);
 
        // Grab the the links to the lessons.
        return doc.select(domQuery);
    }
    
    // Extracts and parses the lesson data from their individual pages.
    private ArrayList<Lesson> getLessons(Elements lessonUrls) {
        
        // Temporary storage of lessons.
        ArrayList<Lesson> lessons = new ArrayList<>();
        
        // Iterate through the lesson urls to load the lesson page and extract, parse, and save the lesson content.
        // The lesson number and title come from the main list of lessons, and the lesson content come from
        // drilling down into each lesson page and extracting and manipulating/parsing the HTML for each less page.
        for (int i = 0; i < lessonUrls.size(); i++) {
            
            int lessonNum = i + 1;
            
            // Log progress to the console.
            System.out.println("Retrieving lesson " + lessonNum);
            
            Element lessonUrl = lessonUrls.get(i);
            
            // Extracting lesson title from link to lesson.
            String lessonTitle = lessonUrl.text();
            
            // Get the lesson's html content.
            String lessonHtml = getLessonHtml(lessonUrl.absUrl("href"));
            
            // Add the lesson data to the arraylist
            lessons.add(new Lesson(lessonNum, lessonTitle, lessonHtml));

            // Debugging purposes.
            if (i == 15) {
                break;
            }
        }
        
        return lessons;
    }
    
    // Get the lesson's HTML content form the individual lesson page.
    private String getLessonHtml(String url) {
        
        // Load the lesson page.
        Document lessonPage = getPage(url);
        // Parse the lesson content to give us only the piece we need.
        return parseLessonPage(lessonPage);
    }
    
    // Helper method for loading a page's content via JSoup.
    private Document getPage(String url) {
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    // Extracts and parses the lesson content from the lesson page.
    private String parseLessonPage(Document lessonPage) {
        
        // Extract the lesson from the lesson page and remove its headerContainer.
        Elements lessonTextElements = lessonPage.select("div.mw-parser-output");
        for( Element element : lessonTextElements.select("#headerContainer") ) {
            element.remove();
        }
        
        // Iterate through the components that make up the lesson text and remove all the comments.
        // We just want the naked HTML for the lesson.
        String lessonContent = "";
        for (Element lessonText : lessonTextElements) {
            removeComments(lessonText);
            lessonContent = lessonText.html();
        }
        return lessonContent;
    }
    
    // The site adds some hidden comments to the portion of the page that contains the 
    // lesson text.  This method filters those comments from the text.
    private void removeComments(Element article) {
        
        // Filter by passing in a NodeFilter object and overriding the appropriate methods.
        article.filter(new NodeFilter() {
            
            @Override
            public FilterResult head(Node node, int depth) {
                // Remove comments.
                if (node instanceof Comment) {
                    return FilterResult.REMOVE;
                }
                return FilterResult.CONTINUE;
            }
            
            @Override
            public FilterResult tail(Node node, int depth) {
                // Remove comments.
                if (node instanceof Comment) {
                    return FilterResult.REMOVE;
                }
                return FilterResult.CONTINUE;
            }
        });
    }
    
    // Converts an arraylist of lessons to JSON and writes to a file.
    private void printJson(ArrayList<Lesson> lessons) {

        String jsonOutPut = buildJsonOutput(lessons);
        writeToFile(jsonOutPut);
    }
    
    // Converts arraylist of Lesson objects to JSON.
    private String buildJsonOutput(ArrayList<Lesson> lessons) {
     
        // Prevent Gson from encoding HTML characters.
        GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping();
        
        // Tells Gson to turn the arraylist of Lessons to JSON objects.
        Type listOfLessonObject = new TypeToken<ArrayList<Lesson>>(){}.getType();
        return gsonBuilder.create().toJson(lessons, listOfLessonObject);
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

    // Method for debugging to the console.
    private void log(String msg, String... vals) {
        System.out.println(String.format(msg, vals));
    }
}
