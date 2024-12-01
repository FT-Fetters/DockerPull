import com.heybcat.docker.pull.core.common.ImageInfo;
import com.heybcat.docker.pull.core.pull.RegistryImagePuller;
import org.junit.Test;

public class ImageInfoTest {


    @Test
    public void testParse(){
        // all examples
        String[] imageExample = {
            // 基础测试：仅镜像名（官方镜像默认命名空间library和默认标签latest）
            "nginx",

            // 带标签的镜像
            "nginx:1.21",
            "mysql:8.0",

            // 带摘要的镜像
            "nginx@sha256:26c5c644b85967d738ddcf54d0d46e9295e607650f5e64e4f93739a030dc99f3",
            "redis@sha256:b5c98ccf7ec2cd610cd5b36526b2db07ff4216f1947dbb59fd3666b01a49c5e6",

            // 同时指定标签和摘要（无效用例，测试系统是否识别冲突）
            "nginx:1.21@sha256:26c5c644b85967d738ddcf54d0d46e9295e607650f5e64e4f93739a030dc99f3",

            // 非官方命名空间镜像
            "myrepo/custom-image:2.3",
            "myrepo/custom-image@sha256:abcd1234efgh5678ijkl9012mnop3456qrst6789uvwxyz0123456789abcdef",

            // 区域化镜像仓库（如GCR）
            "gcr.io/my-project/myimage:latest",
            "us.gcr.io/my-project/myimage:v1.0",
            "asia.gcr.io/my-project/myimage@sha256:1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",

            // GitHub Container Registry (GHCR) 镜像
            "ghcr.io/myusername/myimage:stable",
            "ghcr.io/myusername/core/myimage:stable",
            "ghcr.io/myusername/myimage@sha256:abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890",

            // Docker Hub 官方命名空间（显式指定 library）
            "library/ubuntu",
            "library/ubuntu:22.04",

            // 私有镜像仓库
            "registry.example.com/custom/image:v1.2.3",
            "registry.example.com/custom/image@sha256:abcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdef"
        };;
        for (String image : imageExample) {
            ImageInfo imageInfo = RegistryImagePuller.parseImageParameter(image);
            System.out.println(image + " -> " + imageInfo);
        }

    }

}
