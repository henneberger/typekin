# Object Graphs and Structural Typing in Java

This gives Java a way to handle partial representations of data model while remaining type safe. 
Types are generated at compile time via annotations.

```java
public class Main {
  // The model definition file. All TypeOf
  //  annotations must match this structure.
  //
  // You add `implements St{name}Model`
  @Model
  abstract class Foo implements StFooModel {
    public abstract String getA();
    public abstract String getB();
    public abstract String getC();
  }

  //A subset of the model (for code reuse)
  @TypeOf(model = Foo.class)
  public interface FooAFragment {
    String getA();
  }
  @TypeOf(model = Foo.class)
  public interface FooABFragment {
    String getA();
    String getB();
  }
  @TypeOf(model = Foo.class)
  public interface FooCFragment {
    String getC();
  }
  //compile error, D is not a property of the model
  //@TypeOf(clazz = Foo.class)
  //public interface FooDFragment {
  //  String getD();
  //}

  // A container of data that can contain any data.
  //  Function signatures that are used in the model
  //  will be associated with TypeOf objects.
  //
  // You add `implements St{name}`
  @StructuralType(model = Foo.class)
  public static class FooAData implements StFooAData {
    public String getA() { return "A"; }
  }
  @StructuralType(model = Foo.class)
  public static class FooABData implements StFooABData {
    public String getA() { return "A"; }
    public String getB() { return "B"; }
  }
  @StructuralType(model = Foo.class)
  public static class FooCData implements StFooCData {
    public String getC() { return "C"; }
  }

  public static void main(String[] args) {
    print(new FooAData());
    print(new FooABData());
    //print(new FooCData()); //compile error
  }

  public static void print(FooAFragment foo) {
    System.out.println(foo.getA());
    //System.out.println(foo.getB()); //compile error
    //System.out.println(foo.getC()); //compile error
  }
}

//This gets generated by the annotations:
interface StFooAData extends FooAFragment {}
interface StFooABData extends FooAFragment, FooABFragment {}
interface StFooCData extends FooCFragment {}

interface StFooModel extends FooAFragment, FooABFragment, FooCFragment {}
interface StFooRef extends StFooAData, StFooABData, StFooCData {}
```

### `@Model`
An abstract class that defines the data model. All abstract methods will be recognized.
Parameters:
- `name`: The name of the class it will generate
- `refName`: The name of the class it will generate for data model relationships
- `concreteName`: The name of the empty concrete class for jvm type validation
### `@TypeOf`
Interface classes that represent a partial representation of the model. 
Parameters:
- `model`: The class of the model
### `@StructuralType`
A concrete type that contains data. This can contain any data but only method signatures 
that match the model will be validated.
Parameters:
- `name`: The name of the class it will generate
- `model`: The class of the model



This can be extended to non-trivial examples:
[tests/src/main/java/typekin/tests/example/FooExample.java](tests/src/main/java/typekin/tests/example/FooExample.java)

### Contributions
This work is based on https://github.com/tlamr/stjava
