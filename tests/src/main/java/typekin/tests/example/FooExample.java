package typekin.tests.example;

import java.util.ArrayList;
import java.util.List;
import typekin.annotation.Model;
import typekin.annotation.StructuralType;
import typekin.annotation.TypeOf;

public class FooExample {
  @Model(name = "FooModel", implName = "FooImpl", refName = "FooRef")
  abstract class Foo implements FooModel {
    public abstract String getA();
    public abstract String getB();
    public abstract String getC();
    public abstract List<? extends BarRef> getBar();
    public abstract void test();
  }
  @Model(name = "BarModel", implName = "BarImpl", refName = "BarRef")
  abstract class Bar implements BarModel {
    public abstract String getA();
    public abstract String getB();
    public abstract FooRef getFoo();
  }

  @TypeOf(model = Foo.class)
  public interface FooAFragment {
    String getA();
    List<? extends BarFragment> getBar();
  }
  @TypeOf(model = Foo.class)
  public interface FooABFragment {
    String getA();
    String getB();
    List<? extends BarFragment> getBar();
  }
  @TypeOf(model = Foo.class)
  public interface FooCFragment {
    String getC();
  }
  @TypeOf(model = Bar.class)
  public interface BarFragment {
    String getA();
  }
  //compile error on Model, D is not a property of Foo
  //@TypeOf(model = Foo.class)
  //public interface FooDFragment {
  //  String getD();
  //}

  //You add `implements St{name}`
  @StructuralType(model = Foo.class, name = "FooADataType")
  public static class FooAData implements FooADataType {
    public String getA() { return "A"; }
    public List<BarADataType> getBar() {return new ArrayList<>();}
    public String extraParam() {return "extra";}
  }
  @StructuralType(model = Foo.class, name = "FooABDataType")
  public static class FooABData implements FooABDataType {
    public String getA() { return "A"; }
    public String getB() { return "B"; }
    public List<BarADataType> getBar() {return new ArrayList<>();}
  }
  @StructuralType(model = Foo.class, name = "FooCDataType")
  public static class FooCData implements FooCDataType {
    public String getC() { return "C"; }
  }
  @StructuralType(model = Bar.class, name = "BarADataType")
  public static class BarAData implements BarADataType {
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
