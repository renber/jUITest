import de.renebergelt.juitest.core.annotations.parameterfunctions.TestDescriptionResolver;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class TestDescriptionParamResolverTest {

    @Test
    public void testFunctionExtractionRegExp() {
        String test = "Test {{p:foo}} Test {{p:bar}}";
        Pattern regex = Pattern.compile("\\{\\{(.*?)\\}\\}");
        Matcher m = regex.matcher(test);

        m.find();
        assertEquals("p:foo", m.group(1));

        m.find();
        assertEquals("p:bar", m.group(1));
    }

    @Test
    public void testScriptNameParamResolver() {
        String test = "Test {{p:foo}} Test {{p:bar}}";

        TestDescriptionResolver r = new TestDescriptionResolver();
        String s = r.resolve(test, new Object[] {"foo", "ValueOne", "bar", "ValueTwo"});

        assertEquals("Test ValueOne Test ValueTwo", s);
    }

}
