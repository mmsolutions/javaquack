package javaquack;

import deep.pkg.TestClass;
import deep.pkg.TestInterface;
import org.junit.Assert;
import org.junit.Test;

public class DuckTypingTest {

    @Test
    public void shouldWorkWithPojoProperly() {
        TestClass testClass = new TestClass();
        TestInterface testInterface = DuckTypingSingleton.cast(testClass, TestInterface.class);

        testInterface.setField1("value1");
        Assert.assertEquals("value1", testInterface.getField1());
        Assert.assertEquals("value1", testClass.getField1());

        testInterface.setField2("value2");
        Assert.assertEquals("value2", testInterface.getField2());
        Assert.assertEquals("value2", testClass.getField2());

        Assert.assertEquals(testClass.testClassMethod("string", 1), testInterface.testInterfaceMethod("string", 1));
        Assert.assertNotEquals(testClass.testClassMethod("string", 1), testInterface.testInterfaceMethod("string", 0));
    }

    @Test
    public void shouldCreateProxyClassOnlyOnce() {
        DuckTyping duckTyping = new DuckTyping();

        duckTyping.cast(new TestClass(), TestInterface.class);
        duckTyping.cast(new TestClass(), TestInterface.class);
        duckTyping.cast(new TestClass(), TestInterface.class);

        Assert.assertEquals(1, duckTyping.cache.size());
    }

}
