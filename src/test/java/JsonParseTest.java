import com.heybcat.docker.pull.core.common.oci.Manifests;
import com.heybcat.tightlyweb.common.util.FileUtil;
import java.io.IOException;
import org.junit.Test;

public class JsonParseTest {

    @Test
    public void testManifests() throws IOException {
        String json1 = FileUtil.readFile("/Users/xianlindeng/project/java/DockerPull/src/test/test/test1.json");
        String json2 = FileUtil.readFile("/Users/xianlindeng/project/java/DockerPull/src/test/test/test2.json");
        Manifests parse = Manifests.parse(json1);
        System.out.println(parse);
    }

}
