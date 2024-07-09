# Docker pull

## 使用方法 Usage

```shell
# image: nginx:latest
# proxyUrl 127.0.0.1
# proxyPort 7890
java -jar docker-pull.jar <image> <proxyUrl> <proxyPort>
```

通过该命令在下载并打包完毕后，你可以在 jar 包目录下的images文件夹下找到镜像文件

After the download and packaging are complete, you can find the downloaded image in the image directory under the jar package.

你也可以直接启动网页端进行图形化操作，以下是启动的命令

Or you can use web page

```shell
java -jar docker-pull.jar --web
```

打开浏览器并输入地址 http://localhost:1111

Open url http://localhost:1111

进入页面后查找你要的镜像并选择版本及系统芯片架构等信息并选择拉去，然后也可以直接通过下载列表里面的上传按钮直接上传到你的服务器上

Search your image and download then you can upload to you server
## Docker

默认下载的镜像文件默认以 .gz 结尾，需要改成 .tar

The extracted image is by default ending with .gz, and it needs to be changed to end with .tar.

```shell
# example: image file name 'nginx_latest.tar'
docker load < nginx_latest.tar
```

## 贡献 Contribute

感谢 [nimastudent](https://github.com/nimastudent) 提供的前端页面支持

Thanks to [nimastudent](https://github.com/nimastudent) for the front-end support

## License

MIT © [Fetters](LICENSE)

