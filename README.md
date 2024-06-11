# Docker pull

## Usage

```shell
# image: nginx:latest
# proxyUrl 127.0.0.1
# proxyPort 7890
java -jar docker-pull.jar <image> <proxyUrl> <proxyPort>
```

After the download and packaging are complete, you can find the downloaded image in the image directory under the jar package.

## Docker

```shell
# example: image file name 'nginx_latest.tar'
docker load < nginx_latest.tar
```

## License

MIT © [Fetters](https://github.com/FT-Fetters/DockerPull/blob/master/LICENSE)

