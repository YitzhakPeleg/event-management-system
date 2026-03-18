package model;

/**
 * Represents a physical venue (hall) where events take place.
 * capacity is the maximum number of participants allowed.
 */
public class Hall {

    // ---- Fields ----
    private int id;
    private String name;
    private int capacity;
    private String location;

    // ---- Constructors ----

    public Hall() {}

    public Hall(int id, String name, int capacity, String location) {
        this.id       = id;
        this.name     = name;
        this.capacity = capacity;
        this.location = location;
    }

    // ---- Getters and Setters ----

    public int getId()               { return id; }
    public void setId(int id)        { this.id = id; }

    public String getName()              { return name; }
    public void setName(String name)     { this.name = name; }

    public int getCapacity()                 { return capacity; }
    public void setCapacity(int capacity)    { this.capacity = capacity; }

    public String getLocation()               { return location; }
    public void setLocation(String location)  { this.location = location; }

    @Override
    public String toString() {
        return "Hall{id=" + id + ", name=" + name + ", capacity=" + capacity + ", location=" + location + "}";
    }
}
