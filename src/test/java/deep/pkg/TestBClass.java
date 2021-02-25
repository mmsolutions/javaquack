package deep.pkg;

public class TestBClass extends TestAClass {

    public String testMethodTwo(String parameter1, Integer parameter2) {
        return testInterface.testMethodTwo(parameter1, parameter2);
    }

}
