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
 * The Course in Miracles is a book in the public domain that is made up of several parts.  
 * We are working with the Workbook part which consists of 361 lessons.  
 * This class scrapes and parses those lessons from a website and stores them in a JSON file 
 * on disk.
 */
public class LessonScraper {
    
    public static void main(String[] args) throws IOException {
        
        // Starting point is the page that lists links to all the lessons.
        String webUrl = "https://en.wikisource.org/wiki/A_Course_in_Miracles/Workbook_for_Students";
        
        // Temp storage for the lessons while we gather them.
        ArrayList<Lesson> lessonsObjects = new ArrayList<Lesson>();
        
        // Load the list of lessons. 
        Document doc = Jsoup.connect(webUrl).get();
        
        // Grab the the links to the lessons.
        Elements lessonUrls = doc.select(".mw-parser-output dl dd a");
        
        // Iterate through the lesson urls to load the lesson page and extract, parse, and save the lesson.
        for (int i = 0; i < lessonUrls.size(); i++) {
            
            int lessonNum = i + 1;
            
            Element lessonUrl = lessonUrls.get(i);
            
            // Extracting lesson title from link to lesson.
            String lessonTitle = lessonUrl.text();
            
            // Load the lesson page from the link to the lesson.
            Document lessonPage = Jsoup.connect(lessonUrl.absUrl("href")).get();
            
            // Get the content of the lesson.
            String lessonContent = parseLessonPage(lessonPage);
            
            // Add the lesson data to the arraylist
            lessonsObjects.add(new Lesson(lessonNum, lessonTitle, lessonContent));
            
            // Log progress to the console.
            System.out.println("Retrieving lesson " + lessonNum);
            
            // Debugging purposes.
            /*if (i == 10) {
                break;
            }*/
        }
        
        // Prints the arraylist of lessons to a JSON file.
        printJson(lessonsObjects);
    }
    
    // Extracts and parses the lesson content from the lesson page.
    private static String parseLessonPage(Document lessonPage) {
        
        // Extract the lesson from the lesson page and remove the headerContainer.
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
    private static void removeComments(Element article) {
        
        article.filter(new NodeFilter() {
            
            @Override
            public FilterResult head(Node node, int depth) {
                if (node instanceof Comment) {
                    return FilterResult.REMOVE;
                }
                return FilterResult.CONTINUE;
            }
            
            @Override
            public FilterResult tail(Node node, int depth) {
                if (node instanceof Comment) {
                    return FilterResult.REMOVE;
                }
                return FilterResult.CONTINUE;
            }
        });
    }
    
    // Converts an arraylist of lessons to JSON and writes to a file.
    private static void printJson(ArrayList<Lesson> lessons) {
        
        String fileName = "lessons.json";
        
        // Prevent Gson from encoding HTML characters.
        GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping();
        
        // Tells Gson to turn the arraylist of Lessons to JSON objects.
        Type listOfLessonObject = new TypeToken<ArrayList<Lesson>>(){}.getType();
        String json = gsonBuilder.create().toJson(lessons, listOfLessonObject);
        
        // Write JSON to file.
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(fileName);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.print(json);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method for debugging to the console.
    private static void log(String msg, String... vals) {
        System.out.println(String.format(msg, vals));
    }
}
