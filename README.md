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

The extracted image is by default ending with .gz, and it needs to be changed to end with .tar.

```shell
# example: image file name 'nginx_latest.tar'
docker load < nginx_latest.tar
```

## License

MIT © [Fetters](LICENSE)

