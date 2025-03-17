package fr.zirnitra.safecall;
import fr.zirnitra.safecall.model.Address;
import fr.zirnitra.safecall.model.Person;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class SafeCallTest {
    @Test
    void ofWithValidObject() {
        Person person = new Person(new Address("123 Main St", "Springfield"));
        SafeCall.SafeCallChain<Person> chain = SafeCall.of(person);
        assertEquals(person, chain.get());
    }

    @Test
    void ofWithNullObject() {
        SafeCall.SafeCallChain<Person> chain = SafeCall.of(null);
        assertNull(chain.get());
    }

    @Test
    void callWithValidFunction() {
        Person person = new Person(new Address("123 Main St", "Springfield"));
        SafeCall.SafeCallChain<Address> chain = SafeCall.of(person).call(Person::getAddress);
        assertEquals(person.getAddress(), chain.get());
    }

    @Test
    void callWithNullFunctionResult() {
        Person person = new Person();
        SafeCall.SafeCallChain<Address> chain = SafeCall.of(person).call(Person::getAddress);
        assertNull(chain.get());
    }

    @Test
    void getOptionalWithNonNullValue() {
        Person person = new Person(new Address("123 Main St", "Springfield"));
        SafeCall.SafeCallChain<Person> chain = SafeCall.of(person);
        assertTrue(chain.getOptional().isPresent());
    }

    @Test
    void getOptionalWithNullValue() {
        SafeCall.SafeCallChain<Person> chain = SafeCall.of(null);
        assertFalse(chain.getOptional().isPresent());
    }

    @Test
    void getOrDefaultWithNonNullValue() {
        Person person = new Person(new Address("123 Main St", "Springfield"));
        SafeCall.SafeCallChain<Person> chain = SafeCall.of(person);
        assertEquals(person, chain.getOrDefault(new Person()));
    }

    @Test
    void getOrDefaultWithNullValue() {
        SafeCall.SafeCallChain<Person> chain = SafeCall.of(null);
        Person defaultPerson = new Person();
        assertEquals(defaultPerson, chain.getOrDefault(defaultPerson));
    }

    @Test
    void chainWithValidInput() {
        SafeCall.PreparedSafeCallChain<Person, String> chain = SafeCall.prepare(Person.class)
                .call(Person::getAddress)
                .call(Address::getCity);

        Person person = new Person(new Address("123 Main St", "Springfield"));
        assertEquals("Springfield", chain.on(person).get());
    }

    @Test
    void chainWithNullInput() {
        SafeCall.PreparedSafeCallChain<Person, String> chain = SafeCall.prepare(Person.class)
                .call(Person::getAddress)
                .call(Address::getCity);

        assertNull(chain.on(null).get());
    }

    @Test
    void chainWithIntermediateNull() {
        SafeCall.PreparedSafeCallChain<Person, String> chain = SafeCall.prepare(Person.class)
                .call(Person::getAddress)
                .call(Address::getCity);

        Person person = new Person();
        assertNull(chain.on(person).get());
    }

    @Test
    void chainWithDefaultValue() {
        SafeCall.PreparedSafeCallChain<Person, String> chain = SafeCall.prepare(Person.class)
                .call(Person::getAddress)
                .call(Address::getCity);

        Person person = new Person();
        assertEquals("Unknown", chain.on(person).getOrDefault("Unknown"));
    }

    @Test
    void chainAsFunction() {
        SafeCall.PreparedSafeCallChain<Person, String> chain = SafeCall.prepare(Person.class)
                .call(Person::getAddress)
                .call(Address::getCity);

        Function<Person, String> cityExtractor = chain.asFunction();
        Person person = new Person(new Address("123 Main St", "Springfield"));
        assertEquals("Springfield", cityExtractor.apply(person));
    }

    @Test
    void chainAsFunctionWithNullInput() {
        SafeCall.PreparedSafeCallChain<Person, String> chain = SafeCall.prepare(Person.class)
                .call(Person::getAddress)
                .call(Address::getCity);

        Function<Person, String> cityExtractor = chain.asFunction();
        assertNull(cityExtractor.apply(null));
    }

    @Test
    void chainAsFunctionWithIntermediateNull() {
        SafeCall.PreparedSafeCallChain<Person, String> chain = SafeCall.prepare(Person.class)
                .call(Person::getAddress)
                .call(Address::getCity);

        Function<Person, String> cityExtractor = chain.asFunction();
        Person person = new Person();
        assertNull(cityExtractor.apply(person));
    }

    @Test
    void chainResultOptional() {
        SafeCall.PreparedSafeCallChain<Person, String> chain = SafeCall.prepare(Person.class)
                .call(Person::getAddress)
                .call(Address::getCity);

        Person person = new Person(new Address("123 Main St", "Springfield"));
        Optional<String> city = chain.on(person).getOptional();
        assertTrue(city.isPresent());
        assertEquals("Springfield", city.get());
    }

    @Test
    void chainResultOptionalEmpty() {
        SafeCall.PreparedSafeCallChain<Person, String> chain = SafeCall.prepare(Person.class)
                .call(Person::getAddress)
                .call(Address::getCity);

        Person person = new Person();
        Optional<String> city = chain.on(person).getOptional();
        assertFalse(city.isPresent());
    }

    @Test
    void chainInStream() {
        SafeCall.PreparedSafeCallChain<Person, String> chain = SafeCall.prepare(Person.class)
                .call(Person::getAddress)
                .call(Address::getCity);

        List<Person> people = Arrays.asList(
                new Person(new Address("123 Main St", "Springfield")),
                new Person(new Address("456 Secondary St", "Los Angeles")),
                new Person()
        );

        List<String> cities = people.stream()
                .map(chain.asFunction())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        assertEquals(Arrays.asList("Springfield", "Los Angeles"), cities);
    }

    @Test
    void chainAsFunctionOptional() {
        SafeCall.PreparedSafeCallChain<Person, String> chain = SafeCall.prepare(Person.class)
                .call(Person::getAddress)
                .call(Address::getCity);

        Function<Person, Optional<String>> cityExtractor = chain.asFunctionOptional();
        Person person = new Person(new Address("123 Main St", "Springfield"));
        Optional<String> city = cityExtractor.apply(person);

        assertTrue(city.isPresent());
        assertEquals("Springfield", city.get());
    }

    @Test
    void chainAsFunctionOptionalWithNullInput() {
        SafeCall.PreparedSafeCallChain<Person, String> chain = SafeCall.prepare(Person.class)
                .call(Person::getAddress)
                .call(Address::getCity);

        Function<Person, Optional<String>> cityExtractor = chain.asFunctionOptional();
        Optional<String> city = cityExtractor.apply(null);

        assertFalse(city.isPresent());
    }

    @Test
    void chainAsFunctionOptionalWithIntermediateNull() {
        SafeCall.PreparedSafeCallChain<Person, String> chain = SafeCall.prepare(Person.class)
                .call(Person::getAddress)
                .call(Address::getCity);

        Function<Person, Optional<String>> cityExtractor = chain.asFunctionOptional();
        Person person = new Person();
        Optional<String> city = cityExtractor.apply(person);

        assertFalse(city.isPresent());
    }

    @Test
    void chainAsFunctionOrDefault() {
        SafeCall.PreparedSafeCallChain<Person, String> chain = SafeCall.prepare(Person.class)
                .call(Person::getAddress)
                .call(Address::getCity);

        Function<Person, String> cityExtractor = chain.asFunction("Unknown");
        Person person = new Person(new Address("123 Main St", "Springfield"));

        assertEquals("Springfield", cityExtractor.apply(person));
    }

    @Test
    void chainAsFunctionOrDefaultWithNullInput() {
        SafeCall.PreparedSafeCallChain<Person, String> chain = SafeCall.prepare(Person.class)
                .call(Person::getAddress)
                .call(Address::getCity);

        Function<Person, String> cityExtractor = chain.asFunction("Unknown");

        assertEquals("Unknown", cityExtractor.apply(null));
    }

    @Test
    void chainAsFunctionOrDefaultWithIntermediateNull() {
        SafeCall.PreparedSafeCallChain<Person, String> chain = SafeCall.prepare(Person.class)
                .call(Person::getAddress)
                .call(Address::getCity);

        Function<Person, String> cityExtractor = chain.asFunction("Unknown");
        Person person = new Person();

        assertEquals("Unknown", cityExtractor.apply(person));
    }

    @Test
    void chainInStreamWithOptionals() {
        SafeCall.PreparedSafeCallChain<Person, String> chain = SafeCall.prepare(Person.class)
                .call(Person::getAddress)
                .call(Address::getCity);

        List<Person> people = Arrays.asList(
                new Person(new Address("123 Main St", "Springfield")),
                new Person(new Address("456 Secondary St", "Los Angeles")),
                new Person()
        );

        List<String> cities = people.stream()
                .map(chain.asFunctionOptional())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        assertEquals(Arrays.asList("Springfield", "Los Angeles"), cities);
    }

    @Test
    void chainInStreamWithDefaults() {
        SafeCall.PreparedSafeCallChain<Person, String> chain = SafeCall.prepare(Person.class)
                .call(Person::getAddress)
                .call(Address::getCity);

        List<Person> people = Arrays.asList(
                new Person(new Address("123 Main St", "Springfield")),
                new Person(new Address("456 Secondary St", "Los Angeles")),
                new Person()
        );

        List<String> cities = people.stream()
                .map(chain.asFunction("Unknown"))
                .collect(Collectors.toList());

        assertEquals(Arrays.asList("Springfield", "Los Angeles", "Unknown"), cities);
    }
}
