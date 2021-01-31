package typekin.tests.example;

import java.util.ArrayList;
import java.util.List;
import typekin.annotation.Model;
import typekin.annotation.StructuralType;
import typekin.annotation.TypeOf;

public class FooExample {
  @Model(clazz = Foo.class)
  class Foo implements StFooModel {
    public String getA(){return null;}
    public String getB(){return null;}
    public String getC(){return null;}
    public List<? extends StBarRef> getBar(){return null;}
  }
  @Model(clazz = Bar.class)
  class Bar implements StBarModel {
    public String getA(){return null;}
    public String getB(){return null;}
    public StFooRef getFoo(){return null;}
  }
  @TypeOf(clazz = Foo.class)
  public interface FooAInput {
    String getA();
    List<? extends BarInput> getBar();
  }
  @TypeOf(clazz = Foo.class)
  public interface FooABInput {
    String getA();
    String getB();
    List<? extends BarInput> getBar();
  }
  @TypeOf(clazz = Foo.class)
  public interface FooCInput {
    String getC();
  }
  @TypeOf(clazz = Bar.class)
  public interface BarInput {
    String getA();
  }
  //compile error on Model, D is not a property of Foo
  //@TypeOf(clazz = Foo.class)
  //public interface FooDInput {
  //  String getD();
  //}

  //You add `implements St{name}`
  @StructuralType(clazz = Foo.class)
  public static class FooAFragment implements StFooAFragment {
    public String getA() { return "A"; }
    public List<BarFragment> getBar() {return new ArrayList<>();}
    public String extraParam() {return "extra";}
  }
  @StructuralType(clazz = Foo.class)
  public static class FooABFragment implements StFooABFragment {
    public String getA() { return "A"; }
    public String getB() { return "B"; }
    public List<BarFragment> getBar() {return new ArrayList<>();}
  }
  @StructuralType(clazz = Foo.class)
  public static class FooCFragment implements StFooCFragment {
    public String getC() { return "C"; }
  }
  @StructuralType(clazz = Bar.class)
  public static class BarFragment implements StBarFragment {
    public String getA() { return "A"; }
  }

  public static void main(String[] args) {
    print(new FooAFragment());
    print(new FooABFragment());
    //print(new FooCFragment()); //compile error
  }

  public static void print(FooAInput fooAInput) {
    System.out.println(fooAInput.getA());
    System.out.println(fooAInput.getBar().get(0).getA());
    //System.out.println(fooAInput.getBar().get(0).getB()); //compile error
    //System.out.println(fooInput.getB()); //compile error
    //System.out.println(fooInput.getC()); //compile error
  }
}