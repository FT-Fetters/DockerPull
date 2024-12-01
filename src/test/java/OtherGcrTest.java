import com.heybcat.docker.pull.core.pull.CommonImagePull;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.Test;

public class OtherGcrTest {

    @Test
    public void ghrcTest() throws URISyntaxException, IOException, InterruptedException {
        CommonImagePull.pull("ghcr.io", "sugarforever", "peanut-shell", "latest", "127.0.0.1", 7897);

    }



}
