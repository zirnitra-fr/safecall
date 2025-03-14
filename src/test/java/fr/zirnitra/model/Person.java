package fr.zirnitra.model;

public class Person {
    private final String name;
    private final Address address;

    public Person(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    public Person(Address address) {
        this(null, address);
    }

    public Person() {
        this(null, null);
    }

    public String getName() { return name; }
    public Address getAddress() { return address; }
}
