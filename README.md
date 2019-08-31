# A Course in Miracles Scraper
The Course in Miracles is a book in the public domain that is made up of several parts. This program works with the Workbook part which consists of [361 lessons](https://en.wikisource.org/wiki/A_Course_in_Miracles/Workbook_for_Students). It scrapes and parses all the lessons and stores them to a file on disk as JSON objects so that they can then be loaded into a Room/SQLite database during the installation process of an Android app.

## Files
**Main.java**
- The entry point of the program.

**Lesson.java**
- The POJO class for the Lessons objects.
- Implements Comparable interface so that lessons can be sorted by their ID.

**LessonScraper.java**
- The heart of the program.
- Loads the portion of the wiki page that holds the urls to all the lessons
- Extracts the urls and kicks off the threads for retrieving the individual lesson data
- The lesson data is then converted to JSON objects
- The JSON then gets written to a file on disk

**LessonsRunnable.java**
- Making a network request for every single lesson is expensive. This class implements the Runnable interface so the requests can be done using many threads simultaneously, reducing the run time from about 35 seconds as a single threaded process to about 4 seconds given my machine and internet connection.
- The number of threads used is set by LessonScraper.NUM_THREADS - the optimum for my machine and connection seems to be around 30 threads.  Much more or much less appear to give diminishing returns.  I'm guessing the bottleneck is the lock required for adding the lessons to the arraylist shared between the threads.

## Libraries
[Gson](https://github.com/google/gson) - Java serialization/deserialization library to convert Java Objects into JSON and back

[Jsoup](https://jsoup.org/) - API for extracting and manipulating data, using the  DOM, CSS, and jquery-like methods

## How to Run
- Clone this repo.

       git clone https://github.com/micnap/acim-scraper.git acim-scraper
- cd into the repo

       cd acim-scraper
- Download the [Jsoup](https://repo1.maven.org/maven2/org/jsoup/jsoup/1.12.1/jsoup-1.12.1.jar) and [Gson](https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.5/gson-2.8.5.jar) jar files and **place them in the folder**.


- Compile with javac:

       javac -cp ".:gson-2.8.5.jar:jsoup-1.12.1.jar:" *.java
- Run with

       java -cp ".:gson-2.8.5.jar:jsoup-1.12.1.jar:" Main
       
- A lessons.json file will be populated with JSON objects of the lessons and placed in the acim-scraper directory.


## Single-threaded version
The master branch is multithreaded.  For a single-threaded version, you can check out the vsinglethread tag:

       git checkout vsinglethread
