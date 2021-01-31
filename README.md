# Object Graphs and Structural Typing in Java

This gives Java a way to handle pieces of a graph in a type safe way without a lot of object
duplication. Types are determined at compile time through code generating annotations and are 
linked based on their structural type equivalent.

The annotation processor determines all the structurally equivalent types @TypeOf for each type 
and pairs them with the @StructuralType output type.

```java
class Main {
  @TypeOf(clazz = "Foo")
  public interface FooInput {
    UUID getUuid();
  }

  //You add `implements St{name}`
  @StructuralType(clazz = "Foo")
  public class FooFragment implements StFooFragment {
    public UUID getUuid() {
      return uuid;
    }
  }

  public static void main(String[] args) {
    FooFragment fooFragment = getFooFragment();
    print(fooFragment);
  }

  public void print(FooInput fooInput) {
    System.out.println(fooInput.getUuid());
  }

  public FooFragment getFooFragment() {
    return new FooFragment();
  }
}

//This gets generated:
public interface StFooFragment extends FooFragment {}
```

This can be extended to non-trivial examples:
[tests/src/main/java/typekin/tests/example/TypekinAnnotation.java](tests/src/main/java/typekin/tests/example/TypekinAnnotation.java)

### Contributions
This work is based on https://github.com/tlamr/stjava
