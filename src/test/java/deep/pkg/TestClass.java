package deep.pkg;

public class TestClass {

    private String field1;

    private String field2;

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

    public String testClassMethod(String parameter1, Integer parameter2) {
        return parameter1 + " " + parameter2;
    }

}