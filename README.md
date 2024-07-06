# Docker pull

## Usage

```shell
# image: nginx:latest
# proxyUrl 127.0.0.1
# proxyPort 7890
java -jar docker-pull.jar <image> <proxyUrl> <proxyPort>
```

After the download and packaging are complete, you can find the downloaded image in the image directory under the jar package.

Or you can use web page

```shell
java -jar docker-pull.jar --web
```

Open url http://localhost:1111

Search your image and download then you can upload to you server
## Docker

The extracted image is by default ending with .gz, and it needs to be changed to end with .tar.

```shell
# example: image file name 'nginx_latest.tar'
docker load < nginx_latest.tar
```

## Contribute

Thanks to [nimastudent](https://github.com/nimastudent) for the front-end support

## License

MIT © [Fetters](LICENSE)

