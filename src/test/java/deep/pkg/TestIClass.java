package deep.pkg;

public class TestIClass implements TestInterface {

    private String field1;

    private String field2;

    @Override
    public String getField1() {
        return field1;
    }

    @Override
    public void setField1(String field1) {
        this.field1 = field1;
    }

    @Override
    public String getField2() {
        return field2;
    }

    @Override
    public void setField2(String field2) {
        this.field2 = field2;
    }

    @Override
    public String testInterfaceMethod(String parameter1, Integer parameter2) {
        return parameter1 + " " + parameter2;
    }

}
