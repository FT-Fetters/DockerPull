# Docker pull

## 用法

```shell
# image: nginx:latest
# proxyUrl 127.0.0.1
# proxyPort 7890
java -jar docker-pull.jar <image> <proxyUrl> <proxyPort>
```

下载和打包完成后，你可以在 jar 包下的 image 目录中找到下载的镜像。

## Docker

提取的镜像默认以 .gz 结尾，需要将其更改为 .tar 结尾。

```shell
# 例如：镜像文件名 'nginx_latest.tar'
docker load < nginx_latest.tar
```

## 许可证

MIT © [Fetters](https://github.com/FT-Fetters/DockerPull/blob/master/LICENSE)
