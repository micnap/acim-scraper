/*
 * The Course in Miracles is a book in the public domain that is made up of several parts.  
 * We are working with the Workbook part which consists of 361 lessons.  
 * This class scrapes and parses those lessons from a website and stores them in a JSON file 
 * on disk.
 */
public class Main {

    public static void main(String[] args) {
        
        // Starting point is the page that lists links to all the lessons.
        String webUrl = "https://en.wikisource.org/wiki/A_Course_in_Miracles/Workbook_for_Students";
        
        // Dom selector for lessons list.
        String domQuery = ".mw-parser-output dl dd a";
        
        LessonScraper lessonScraper = new LessonScraper(webUrl, domQuery);
        lessonScraper.scrape();
    }
}
