package typekin.tests.example;

import java.util.ArrayList;
import java.util.List;
import typekin.annotation.Model;
import typekin.annotation.StructuralType;
import typekin.annotation.TypeOf;

public class FooExample {
  @Model(clazz = Foo.class)
  abstract class Foo implements StFooModel {
    public abstract String getA();
    public abstract String getB();
    public abstract String getC();
    public abstract List<? extends StBarRef> getBar();
    public abstract void test();
  }
  @Model(clazz = Bar.class)
  abstract class Bar implements StBarModel {
    public abstract String getA();
    public abstract String getB();
    public abstract StFooRef getFoo();
  }
  @TypeOf(clazz = Foo.class)
  public interface FooAFragment {
    String getA();
    List<? extends BarFragment> getBar();
  }
  @TypeOf(clazz = Foo.class)
  public interface FooABFragment {
    String getA();
    String getB();
    List<? extends BarFragment> getBar();
  }
  @TypeOf(clazz = Foo.class)
  public interface FooCFragment {
    String getC();
  }
  @TypeOf(clazz = Bar.class)
  public interface BarFragment {
    String getA();
  }
  //compile error on Model, D is not a property of Foo
  //@TypeOf(clazz = Foo.class)
  //public interface FooDFragment {
  //  String getD();
  //}

  //You add `implements St{name}`
  @StructuralType(clazz = Foo.class)
  public static class FooAData implements StFooAData {
    public String getA() { return "A"; }
    public List<BarData> getBar() {return new ArrayList<>();}
    public String extraParam() {return "extra";}
  }
  @StructuralType(clazz = Foo.class)
  public static class FooABData implements StFooABData {
    public String getA() { return "A"; }
    public String getB() { return "B"; }
    public List<BarData> getBar() {return new ArrayList<>();}
  }
  @StructuralType(clazz = Foo.class)
  public static class FooCData implements StFooCData {
    public String getC() { return "C"; }
  }
  @StructuralType(clazz = Bar.class)
  public static class BarData implements StBarData {
    public String getA() { return "A"; }
  }

  public static void main(String[] args) {
    print(new FooAData());
    print(new FooABData());
    //print(new FooCData()); //compile error
  }

  public static void print(FooAFragment foo) {
    System.out.println(foo.getA());
    System.out.println(foo.getBar().get(0).getA());
    //System.out.println(foo.getBar().get(0).getB()); //compile error
    //System.out.println(foo.getB()); //compile error
    //System.out.println(foo.getC()); //compile error
  }
}
