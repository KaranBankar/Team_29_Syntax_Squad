public class Contact {
    private String id;
    private String name;
    private String phone;
    private String category;
    private int priority;

    public Contact(String id, String name, String phone, String category, int priority) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.category = category;
        this.priority = priority;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
}