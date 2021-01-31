# Object Graphs and Structural Typing in Java

This gives Java a way to handle pieces of a graph in a type safe way without a lot of object
duplication. Types are determined at compile time through code generating annotations and are 
linked based on their structural type equivalent.

The annotation processor determines all the structurally equivalent types @TypeOf for each type 
and pairs them with the @StructuralType output type.

```java
class Main {
  public static void main(String[] args) {
    RunFragment runFragment = getRunFragment();
    printRun(runFragment);
  }

  public void printRun(RunInput runInput) {
    System.out.println(runInput.getUuid());
  }

  public RunFragment getRunFragment() {
    return new RunFragment();
  }

  @TypeOf(clazz = "Run")
  public interface RunInput {
    UUID getUuid();
  }

  //You add `implements St{name}`
  @StructuralType(clazz = "Run")
  public class RunFragment implements StRunFragment {
    public UUID getUuid() {
      return uuid;
    }
  }
}

//This gets generated:
public interface StRunFragment extends RunFragment {}
```

This can be extended to non-trivial examples:
[tests/src/main/java/typekin/tests/example/TypekinAnnotation.java](tests/src/main/java/typekin/tests/example/TypekinAnnotation.java)

### Contributions
This work is based on https://github.com/tlamr/stjava