import com.heybcat.docker.pull.util.CryptoUtil;
import org.junit.jupiter.api.Test;

public class UtilTest {

    @Test
    public void testModuloCrypto(){
        String s = CryptoUtil.moduloEncrypt("abac213ss@@");
        System.out.println(s);
        System.out.println(CryptoUtil.moduloDecrypt(s));
    }

}
