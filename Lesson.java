/*
 * Lesson POJO class for ACIM lessons.
 */
public class Lesson implements Comparable {
    
    // Properties.
    private int id;
    private String title;
    private String html;
    
    // Constructors.
    public Lesson() {}
    
    public Lesson(int id, String title, String html) {
        super();
        this.id = id;
        this.title = title;
        this.html = html;
    }

    // Getters and setters.
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getHtml() {
        return html;
    }
    
    public void setHtml(String html) {
        this.html = html;
    }

    @Override
    public int compareTo(Object lesson) {
        int compareId = ((Lesson)lesson).getId();
        /* For Ascending order*/
        return this.id - compareId;
    }
}
