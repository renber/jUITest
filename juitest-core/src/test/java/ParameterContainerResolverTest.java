import de.renebergelt.juitest.core.annotations.UITest;
import de.renebergelt.juitest.core.annotations.parameterfunctions.ParameterRange;
import de.renebergelt.juitest.core.annotations.parameterfunctions.ParameterSet;
import de.renebergelt.juitest.core.annotations.parameterfunctions.TestParameterResolver;
import de.renebergelt.juitest.core.annotations.parameterfunctions.containers.ParameterSetContainer;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ParameterContainerResolverTest {

    static class SampleTestClass {

        @UITest
        @ParameterSet(index = 0, name = "param1", stringValues = {"one", "two"})
        @ParameterSet(index = 1, name = "param2", intValues = {1, 2, 3})
        public void test_One(String param1, int param2) {
            // --
        }

    }

    @Test
    public void testHasParams() {
        ParameterContainerResolverTest.SampleTestClass tc = new ParameterContainerResolverTest.SampleTestClass();
        Method m = Arrays.stream(tc.getClass().getMethods()).filter(x -> "test_One".equals(x.getName())).findFirst().get();

        TestParameterResolver r = new TestParameterResolver();
        assertTrue(r.hasParameters(m));
    }

    @Test
    public void testUnrollParameterSetContainer() {
        ParameterContainerResolverTest.SampleTestClass tc = new ParameterContainerResolverTest.SampleTestClass();
        Method m = Arrays.stream(tc.getClass().getMethods()).filter(x -> "test_One".equals(x.getName())).findFirst().get();

        TestParameterResolver r = new TestParameterResolver();

        List<Annotation> params = r.unrollParameterContainer(m.getAnnotation(ParameterSetContainer.class));
        assertEquals(2, params.size());
        assertEquals(ParameterSet.class, params.get(0).annotationType());
        assertEquals(ParameterSet.class, params.get(1).annotationType());
    }

}
