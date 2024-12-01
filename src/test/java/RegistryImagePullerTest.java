import com.heybcat.docker.pull.core.pull.RegistryImagePuller;
import org.junit.Test;

public class RegistryImagePullerTest {

    @Test
    public void testPull(){
        RegistryImagePuller puller = RegistryImagePuller.create(null);
//        RegistryImagePuller.pull("ghcr.io/instrumentisto/nmap:7.95-r4", true);
//        RegistryImagePuller.pull("ghcr.io/zoeyvid/npmplus:develop", true);
//        RegistryImagePuller.pull("ghcr.io/homebrew/core/wget:1.25.0@sha256:d60aa4825d4682dc4438e0a502747e0869b5a1d5c688d544ddc9a5f9474e1395", true);
        puller.pull("nginx:latest", true);

    }

}
