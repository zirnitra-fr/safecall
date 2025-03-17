# SafeCall

## Description

SafeCall is a lightweight library providing a mechanism similar to the Elvis Operator for your Java projects. It aims to simplify null-checks and provide a more readable and concise way to handle optional values.

## Dependency

To use SafeCall in your project, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>fr.zirnitra</groupId>
    <artifactId>safecall</artifactId>
</dependency>
```

## Usage

The library aims to be simple to use, it provides two entries :
- `SafeCall#of` which you should use on a unique instance of a class.
- `SafeCall#prepare` which you should use on a collection of instances.

For the following examples, let's consider the following records:

```java
public class SomeObject{
    private final NestedObject nestedObject;
    public SomeObject(NestedObject nestedObject) {
        this.nestedObject = nestedObject;
    }
    
    public NestedObject getNestedObject() {
        return nestedObject;
    }
}
```

```java
public class NestedObject{
    String property;
    
    public NestedObject(String property) {
        this.property = property;
    }
    
    public String getProperty() {
        return property;
    }
}
```

### SafeCall#of

The `SafeCall#of` method is used to safely call a method on an object. If the object is `null`, the method will return `null`. If the method throws an exception, the method will return `null`.

`of()` method takes the root instance on which method calls will be made in a chain, you can add calls with the `call()` method.
NB : calling `call()` isn't mandatory before calling one of the three get methods.

```java
import fr.zirnitra.safecall.SafeCall;
import java.util.Optional;

public class SafeCallOfDemo {

    public static void main(String[] args) {
        // This case has no null values, it will return the nested property's value.
        SomeObject nonNullObjects = new SomeObject(new NestedObject("value"));
        String result = SafeCall.of(nonNullObjects).call(SomeObject::getNestedObject).call(NestedObject::getProperty).get();
        System.out.println("get() called : " + result); // This will print "value"

        // This case has a null value in its calls chain, it will return null when calling get() method.
        SomeObject nullNestedObject = new SomeObject(null);
        String nullResultGet = SafeCall.of(nullNestedObject).call(SomeObject::getNestedObject).call(NestedObject::getProperty).get();
        System.out.println("get() called with null value in chain : " + nullResultGet); // This will print "null" instead of throwing a NullPointerException

        String nullResultGetOrDefault = SafeCall.of(nullNestedObject).call(SomeObject::getNestedObject).call(NestedObject::getProperty).getOrDefault("some default value");
        System.out.println("getOrDefault() called with null value in chain : " + nullResultGetOrDefault); // This will print "some default value" instead of throwing a NullPointerException)

        Optional<String> nullResultGetOptional = SafeCall.of(nullNestedObject).call(SomeObject::getNestedObject).call(NestedObject::getProperty).getOptional();
        System.out.println("getOptional() called with null value in chain : " + nullResultGetOptional); // This will print "Optional.empty" instead of throwing a NullPointerException
    }
}
```

### SafeCall#prepare

The `SafeCall#prepare` allows the same checks as `SafeCall#of` but on a collection of instances while reusing the prepared chain.

```java
import fr.zirnitra.safecall.SafeCall;
import java.util.Arrays;
import java.util.List;

public class SafeCallPrepareDemo {

    public static void main(String[] args) {
        // We prepare a few objects on which we want to get the nested property value.
        List<SomeObject> someObjects = Arrays.asList(
                new SomeObject(new NestedObject("value")),
                new SomeObject(new NestedObject(null)),
                new SomeObject(null),
                null
        );

        // Here's the prepared chain. The first property is the root class, the second property is the expected return type.
        SafeCall.PreparedSafeCallChain<SomeObject, String> preparedSafeCallChain = SafeCall.prepare(SomeObject.class).call(SomeObject::getNestedObject).call(NestedObject::getProperty);

        // We can now execute the chain on each object in the list
        System.out.println("Iterating over the list of objects : ");
        for (SomeObject someObject : someObjects) {
            String result = preparedSafeCallChain.on(someObject).get();
            System.out.println(result); // Would display : "value", "null", "null", "null"
        }
        System.out.println();

        // We can also achieve this with a stream, using the `asFunction` methods
        System.out.println("Using the prepared chain as a function in a stream : ");
        someObjects.stream().map(preparedSafeCallChain.asFunction()).forEach(System.out::println);
        System.out.println();

        // Or apply default behavior
        System.out.println("Using the prepared chain as a function in a stream with default behavior : ");
        someObjects.stream().map(preparedSafeCallChain.asFunction("default")).forEach(System.out::println);
        System.out.println();
        System.out.println("Using the prepared chain as a function in a stream with Optional behavior : ");
        someObjects.stream().map(preparedSafeCallChain.asFunctionOptional()).forEach(opt -> System.out.println(opt.orElse("optDefault")));
    }
}
```