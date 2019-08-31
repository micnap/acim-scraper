import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeFilter;
import org.jsoup.select.NodeFilter.FilterResult;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;


public class LessonsRunnable implements Runnable {
    
    Element lessonUrl;
    int lessonNum;
    // Threadsafe version of ArrayList.
    CopyOnWriteArrayList<Lesson> lessons = new CopyOnWriteArrayList<Lesson>(); 
    
    public LessonsRunnable(Element lessonUrl, int lessonNum, CopyOnWriteArrayList<Lesson> lessons) {
        this.lessonUrl = lessonUrl;
        this.lessonNum = lessonNum;
        this.lessons = lessons;
    }


    @Override
    public void run() {
        // TODO Auto-generated method stub
     // Log progress to the console.
        System.out.println("Retrieving lesson " + lessonNum);
        
        
        // Extracting lesson title from link to lesson.
        String lessonTitle = lessonUrl.text();
        
        // Get the lesson's html content.
        String lessonHtml = getLessonHtml(lessonUrl.absUrl("href"));
        
        // Add the lesson data to the arraylist
        lessons.add(new Lesson(lessonNum, lessonTitle, lessonHtml));
        
        System.out.println(Thread.currentThread().getName() + " - Lesson " + lessonNum + ": " + lessonTitle);
        
        
    }
    
 // Get the lesson's HTML content form the individual lesson page.
    private String getLessonHtml(String url) {
        
        // Load the lesson page.
        Document lessonPage = getPage(url);
        // Parse the lesson content to give us only the piece we need.
        return parseLessonPage(lessonPage);
    }
    
    // Helper method for loading a page's content via JSoup.
    protected static Document getPage(String url) {
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
    
    

}
