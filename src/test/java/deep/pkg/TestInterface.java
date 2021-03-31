package deep.pkg;

public interface TestInterface {

    String getField1();

    void setField1(String field1);

    String getField2();

    void setField2(String field2);

    String testMethodOne(String argument1, Integer argument2);

    String testMethodTwo(String argument1, Integer argument2);

    default String helloWorld() {
        return "Hello World!";
    }

}
