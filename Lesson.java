/*
 * Lesson class for ACIM lessons.
 * 
 */
public class Lesson {
    
    private int id;
    private String title;
    private String html;
    
    public Lesson() {}
    
    public Lesson(int id, String title, String html) {
        super();
        this.id = id;
        this.title = title;
        this.html = html;
    }

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
}
