import de.renebergelt.juitest.core.annotations.UITest;
import de.renebergelt.juitest.core.annotations.parameterfunctions.ParameterRange;
import de.renebergelt.juitest.core.annotations.parameterfunctions.TestParameterResolver;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ParameterFunctionResolverTest {

    static class SampleTestClass {

        @UITest
        @ParameterRange(index = 0, name = "param1", start = 5, end = 10)
        public void test_One(int param1) {
            // --
        }

    }

    @Test
    public void testNoFunctions() {
     /*   ParameterFunctionResolver r = new ParameterFunctionResolver();
        List<Object[]> psets = r.resolveParameterSets(new Object[] {"foo", 2});

        assertEquals(1, psets.size());
        assertArrayEquals(new Object[] {"foo", 2}, psets.get(0));*/
    }

    @Test
    public void testSingleRangeFunction() {
        SampleTestClass tc = new SampleTestClass();
        Method m = Arrays.stream(tc.getClass().getMethods()).filter(x -> "test_One".equals(x.getName())).findFirst().get();

        TestParameterResolver r = new TestParameterResolver();
        List<Object[]> psets = r.resolveParameterSets(m);

        List<Object[]> expectedSets = new ArrayList<>();
        expectedSets.add(new Object[] {"param1", 5});
        expectedSets.add(new Object[] {"param1", 6});
        expectedSets.add(new Object[] {"param1", 7});
        expectedSets.add(new Object[] {"param1", 8});
        expectedSets.add(new Object[] {"param1", 9});
        expectedSets.add(new Object[] {"param1", 10});

        for(Object[] exp: expectedSets) {
            if (!psets.stream().anyMatch(x -> Arrays.equals(exp, x))) {
                fail("Expected set not in result set");
            }
        }
    }

    @Test
    public void testSingleSetFunction() {
        /*ParameterFunctionResolver r = new ParameterFunctionResolver();
        List<Object[]> psets = r.resolveParameterSets(new Object[] {"foo", "{{set:1,5,7}}"});

        assertEquals(3, psets.size());

        List<Object[]> expectedSets = new ArrayList<>();
        expectedSets.add(new Object[] {"foo", 1});
        expectedSets.add(new Object[] {"foo", 5});
        expectedSets.add(new Object[] {"foo", 7});

        for(Object[] exp: expectedSets) {
            if (!psets.stream().anyMatch(x -> Arrays.equals(exp, x))) {
                fail("Expected set not in result set");
            }
        }*/
    }

    @Test
    public void testDoubleSetFunction() {
       /* ParameterFunctionResolver r = new ParameterFunctionResolver();
        List<Object[]> psets = r.resolveParameterSets(new Object[] {"foo", "{{set:1,5,7}}",
                                                                    "bar", "{{set:baz,bam}}"});

        assertEquals(6, psets.size());

        List<Object[]> expectedSets = new ArrayList<>();
        expectedSets.add(new Object[] {"foo", 1, "bar", "baz"});
        expectedSets.add(new Object[] {"foo", 1, "bar", "bam"});
        expectedSets.add(new Object[] {"foo", 5, "bar", "baz"});
        expectedSets.add(new Object[] {"foo", 5, "bar", "bam"});
        expectedSets.add(new Object[] {"foo", 7, "bar", "baz"});
        expectedSets.add(new Object[] {"foo", 7, "bar", "bam"});

        for(Object[] exp: expectedSets) {
            if (!psets.stream().anyMatch(x -> Arrays.equals(exp, x))) {
                fail("Expected set not in result set");
            }
        }*/
    }

}
