import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class MainTest {

    @Test
    public void TestReturn7() {
        assertThat(Main.return7(), is(7));
    }

    @Test
    public void TestReturn8() {
        assertThat(Main.return8(), is(8));
    }
}
