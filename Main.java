/*
 * The Course in Miracles is a book in the public domain that is made up of several parts.  
 * I am working with the Workbook part which consists of 361 lessons. This program scrapes 
 * and parses all the lessons and stores them to a file on disk as JSON objects so that I can 
 * then load them into a Room/SQLite database during the installation process of an Android app. 
 */
public class Main {

    public static void main(String[] args) {
        
        // Starting point is the page that lists links to all the lessons.
        String webUrl = "https://en.wikisource.org/wiki/A_Course_in_Miracles/Workbook_for_Students";
        
        // Dom selector for lessons list.
        String domQuery = ".mw-parser-output dl dd a";
        
        LessonScraper lessonScraper = new LessonScraper(webUrl, domQuery);
        
        // Performance testing timer.
         long startTime = System.nanoTime();
        
        lessonScraper.scrape();
        
        // Performance testing timer.
         long endTime = System.nanoTime();
         System.out.println("Total time: " + ((endTime - startTime)/1000000) + " milliseconds");
    }
}
