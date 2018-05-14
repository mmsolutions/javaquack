package javaquack;

import deep.pkg.TestAClass;
import deep.pkg.TestBClass;
import deep.pkg.TestIClass;
import deep.pkg.TestInterface;
import org.junit.Assert;
import org.junit.Test;

public class DuckTypingTest {

    @Test
    public void shouldProxyTestAClassProperly() {
        TestAClass testAClass = new TestAClass();
        TestInterface testInterface = DuckTyping.cast(testAClass, TestInterface.class);

        testInterface.setField1("value1");
        Assert.assertEquals("value1", testInterface.getField1());
        Assert.assertEquals("value1", testAClass.getField1());

        testInterface.setField2("value2");
        Assert.assertEquals("value2", testInterface.getField2());
        Assert.assertEquals("value2", testAClass.getField2());

        Assert.assertEquals(testAClass.testClassMethod("string", 1), testInterface.testInterfaceMethod("string", 1));
        Assert.assertNotEquals(testAClass.testClassMethod("string", 1), testInterface.testInterfaceMethod("string", 0));
    }

    @Test
    public void shouldProxyTestBClassProperly() {
        TestBClass testBClass = new TestBClass();
        TestInterface testInterface = DuckTyping.cast(testBClass, TestInterface.class);

        testInterface.setField1("value1");
        Assert.assertEquals("value1", testInterface.getField1());
        Assert.assertEquals("value1", testBClass.getField1());

        testInterface.setField2("value2");
        Assert.assertEquals("value2", testInterface.getField2());
        Assert.assertEquals("value2", testBClass.getField2());

        Assert.assertEquals(testBClass.testClassMethod("string", 1), testInterface.testInterfaceMethod("string", 1));
        Assert.assertNotEquals(testBClass.testClassMethod("string", 1), testInterface.testInterfaceMethod("string", 0));
    }

    @Test
    public void shouldNotCreateProxyClass() {
        TestInterface testInterface = null;

        testInterface = DuckTyping.cast(new TestIClass(), TestInterface.class);
        testInterface = DuckTyping.cast(null, TestInterface.class);
        testInterface = DuckTyping.cast(new TestIClass(), TestInterface.class);

        Assert.assertNull(DuckTyping.cache.get(DuckTyping.generateName(TestIClass.class, TestInterface.class)));
    }

    @Test(expected = DuckTypingException.class)
    public void shouldThrowDuckTypingExceptionWhenCastingNotToInterface() {
        DuckTyping.cast(new Object(), Object.class);
    }

}
