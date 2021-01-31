package typekin.tests.example;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import typekin.annotation.StructuralType;
import typekin.annotation.TypeOf;
import typekin.tests.example.TypekinAnnotation.RunDao.RunFragment;
import typekin.tests.example.TypekinAnnotation.RunDao.RunWithDatasetFragment;

/**
 * An annotation example
 */
public class TypekinAnnotation {
  class RunService {
    public void doSomething() {
      RunDao runDao = new RunDao();
      RunFragment runFragment = runDao.getRunFragment();
      RunWithDatasetFragment runWithDataset = runDao.getRunAndDatasetFragment();

      printRun(runFragment);
      printRun(runWithDataset);

//      printRunWithDataset(runFragment); //Does not compile.
      printRunWithDataset(runWithDataset);
    }

    private void printRun(RunOnlyFragmentInput runWithDataset) {
      System.out.println(runWithDataset.getUuid());
      System.out.println(runWithDataset.getCreatedAt());
    }

    private void printRunWithDataset(RunWithDatasetFragmentInput runWithDataset) {
      System.out.println(runWithDataset.getUuid());
      System.out.println(runWithDataset.getCreatedAt());
      System.out.println(runWithDataset.getInputs().get(0).getUuid());
    }
  }

  //Todo: Provide validation that these object conform to the model
  @TypeOf(clazz = "Run")
  public interface RunOnlyFragmentInput {
    UUID getUuid();
    Instant getCreatedAt();
  }
  @TypeOf(clazz = "Run")
  public interface RunWithDatasetFragmentInput {
    UUID getUuid();
    Instant getCreatedAt();
    List<? extends DatasetInputFragment> getInputs();
  }
  @TypeOf(clazz = "Dataset")
  interface DatasetInputFragment {
    UUID getUuid();
  }

  //The type reference for the model
  class Model {
    abstract class Run {
      abstract UUID getUuid();
      abstract Instant getCreatedAt();
      abstract List<Dataset> getInputs();
    }

    abstract class Dataset {
      abstract UUID getUuid();
    }
  }

  class RunDao {
    public RunFragment getRunFragment() {
      return new RunFragment();
    }

    public RunWithDatasetFragment getRunAndDatasetFragment() {
      return new RunWithDatasetFragment();
    }

    //You add `implements St{name}`, annotation processor will generate
    @StructuralType(clazz = "Run")
    public class RunFragment implements StRunFragment {
      UUID uuid;
      Instant createdAt;
      public UUID getUuid() {
        return uuid;
      }
      public Instant getCreatedAt() {
        return createdAt;
      }
    }

    @StructuralType(clazz = "Run")
    public class RunWithDatasetFragment implements StRunWithDatasetFragment {
      UUID uuid;
      Instant createdAt;
      List<DatasetFragment> inputs;

      public UUID getUuid() {
        return uuid;
      }
      public Instant getCreatedAt() {
        return createdAt;
      }
      public List<DatasetFragment> getInputs() {
        return inputs;
      }
    }

    @StructuralType(clazz = "Dataset")
    class DatasetFragment implements StDatasetFragment {
      UUID uuid;

      public UUID getUuid() {
        return uuid;
      }
    }
  }
}