package deep.pkg;

public class TestAClass {

    private String field1;

    private String field2;

    protected TestInterface testInterface = new TestIClass();

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public String getField2() {
        return field2;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }

    public String aMethod(String parameter1, Integer parameter2) {
        return parameter1 + ":" + parameter2;
    }

    public String testMethodOne(String parameter1, Integer parameter2) {
        return testInterface.testMethodOne(parameter1, parameter2);
    }

}
