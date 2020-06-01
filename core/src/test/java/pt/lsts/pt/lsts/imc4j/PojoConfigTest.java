package pt.lsts.pt.lsts.imc4j;

import org.junit.Assert;
import org.junit.Test;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.util.PojoConfig;

public class PojoConfigTest {

    class SimplePojo {
        @Parameter
        double test = 50.1;

        @Parameter
        double test3 = 50.4;

        @Parameter
        String[] tests = { "a", "b" };

    }

    @Test
    public void simplePojoTest() throws Exception {
        SimplePojo pojo = PojoConfig.create(SimplePojo.class, new String[] { "-Dtest=40" });
        Assert.assertEquals(pojo.test3, 50.4, 0.001);
        Assert.assertEquals(pojo.test, 40.0, 0.001);
        Assert.assertEquals(pojo.tests.length, 2);
        SimplePojo p = new SimplePojo();
    }
}
