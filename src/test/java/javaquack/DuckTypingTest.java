package javaquack;

import deep.pkg.TestAClass;
import deep.pkg.TestBClass;
import deep.pkg.TestIClass;
import deep.pkg.TestInterface;
import org.junit.Assert;
import org.junit.Test;

public class DuckTypingTest {

    @Test(expected = DuckTypingException.class)
    public void shouldNotProxyTestAClassProperly() {
        DuckTyping.cast(new TestAClass(), TestInterface.class);
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

        Assert.assertEquals(testBClass.testMethodOne("string", 1), testInterface.testMethodOne("string", 1));
        Assert.assertNotEquals(testBClass.testMethodTwo("string", 1), testInterface.testMethodTwo("string", 0));
    }

    @Test
    public void shouldNotCreateProxyClass() {
        DuckTyping.cast(new TestIClass(), TestInterface.class);
        DuckTyping.cast(null, TestInterface.class);
        DuckTyping.cast(new TestIClass(), TestInterface.class);
        Assert.assertNull(DuckTyping.getCache().get(DuckTyping.generateName(TestIClass.class, TestInterface.class)));
    }

    @Test(expected = DuckTypingException.class)
    public void shouldThrowDuckTypingExceptionWhenCastingNotToInterface() {
        DuckTyping.cast(new Object(), Object.class);
    }

    @Test(expected = DuckTypingException.class)
    public void shouldThrowDuckTypingExceptionWhenDestinationInterfaceIsNull() {
        DuckTyping.cast(new Object(), null);
    }

}
