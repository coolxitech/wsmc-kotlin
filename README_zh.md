# WSMC

[![Release](https://img.shields.io/github/v/release/coolxitech/wsmc-kotlin?style=flat-square&label=release)](https://github.com/coolxitech/wsmc-kotlin/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/coolxitech/wsmc-kotlin/total?style=flat-square&logo=github)](https://github.com/coolxitech/wsmc-kotlin/releases)

为 Minecraft Java 启用 WebSocket 支持。
此模组允许客户端使用 WebSocket URI 加入支持 WebSocket 的 Minecraft 服务器，同时也保留原版 TCP 语法。

适用于 Minecraft Forge、Neoforge 和 Fabric：
* 26.1, 26.1.1, 26.1.2, 26.2
* 1.21.11
* 1.20.5 到 1.21.4，包括。1.21.5 到 1.21.10 已知可正常工作。
* 1.20.2、1.20.3、1.20.4
* 1.20.1
* 1.18.2、1.19.x、1.20

此分支用于 26.1 和 26.2，截至 2026/06/26。

此模组独立运行，没有任何依赖项。

## 下载

从 [GitHub Releases](https://github.com/coolxitech/wsmc-kotlin/releases/latest) 下载最新版本，按你的 Mod 加载器选择对应的 jar：

| 加载器 | 产物 |
| --- | --- |
| Forge | `wsmc-<version>-forge-<mc>.jar` |
| NeoForge | `wsmc-<version>-neoforge-<mc>.jar` |
| Fabric | `wsmc-<version>-fabric-<mc>.jar`（需 Fabric Loader） |

## 当此模组安装在客户端时：
* 客户端可以使用类似 `ws://hostname.com:port/path_to_minecraft_endpoint` 的 URI 加入支持 WebSocket 的服务器。
* 客户端可以使用旧语法使用原始的TCP协议地址加入任何服务器，例如 `hostname_or_ip:port`。

## 注意事项
* 此模组不会影响任何游戏玩法。
* 此模组不会修改任何 GUI。
* 在客户端上安装此模组不会阻止您加入其他原版插件服或模组服。
* 此模组与其他 TCP-WebSocket 代理兼容，例如 websocat。

## 客户端选项
有时 DNS 会为 HTTP 主机名（ws）或 SNI（wss）返回慢速 IP。客户端可能希望控制解析 IP 地址的方式。

客户端可以可选地控制 WebSocket 握手中的 HTTP 主机名和 SNI：
```
使用指定的 http 主机名进行非安全 WebSocket 连接：
ws://host.com@ip.ip.ip.ip

指定 sni 和 http 主机名为相同值（sni-host.com），从 ip.ip.ip.ip 解析服务器 IP：
wss://sni-host.com@ip.ip.ip.ip

设置不同的 sni 和 http 主机名，从 host.com 解析服务器 IP：
wss://sni.com:@host.com[:port]

设置不同的 sni 和 http 主机名，从 sni.com 解析服务器 IP：
wss://:host.com@sni.com[:port]

分别设置 sni、http 主机名和服务器地址
wss://sni.com:host.com@ip.ip.ip.ip
```

端口和路径规范可以同时附加。

## 配置
此模组的配置通过"系统属性"传递。您可以使用命令行中的 `-D` 选项来传递这些参数。

| 属性键                    | 类型     | 用法                                                                                                                                                                                         | 端侧          | 默认值  | 示例   |
|---------------------------|----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|---------|--------|
| wsmc.debug                | 布尔值   | 显示调试日志。                                                                                                                                                                               | 客户端 | false   | true   |
| wsmc.dumpBytes            | 布尔值   | 转储原始 WebSocket 二进制帧。仅当 `wsmc.debug` 设置为 `true` 时才有效。                                                                                                                      | 客户端 | false   | true   |
| wsmc.maxFramePayloadLength| 整数     | 最大允许的帧负载长度。将此值设置为满足您的模组包的要求，否则 Netty 将抛出错误"Max frame length of x has been exceeded"。                                                                      | 客户端 | 65536   | 65536  |

### 客户端 TLS 证书（双向 TLS/mTLS）
客户端证书通过 `config/wsmc.properties`（相对于游戏目录）配置，而不是系统属性。

```properties
# 客户端身份密钥库（PKCS12/JKS）文件路径，包含客户端证书链和私钥。
# 未设置时默认使用 config/wsmc-client.p12（相对于游戏目录）。
# keyStore=C:/certs/client.p12

# 密钥库密码；当未设置 keyPassword 时也用作私钥密码。
keyStorePassword=changeit

# 私钥密码，仅在私钥密码与密钥库密码不同时使用。
# keyPassword=keypass

# 密钥库类型（JKS / PKCS12）。未设置时按文件扩展名推断。
# keyStoreType=PKCS12
```

## 依赖项
### Forge 版本
* `netty-codec-http` 用于处理 HTTP 和 WebSocket

### Fabric 版本
您需要安装 Fabric Loader，然后安装此模组。Fabric API 是可选的但强烈推荐。

## 对于开发者
要修改和调试代码，首先在 Eclipse IDE 中将"forge"或"fabric"文件夹作为 Gradle 项目导入，然后运行 gradle 任务 `genEclipseRuns`。

Windows 用户需要用 `.\` 和 `..\` 分别替换 `./` 和 `../`。

代码库使用 Minecraft 官方映射。

### 编译 Fabric 构件
```
git clone https://github.com/rikka0w0/wsmc.git
cd wsmc/fabric
./gradlew build
```

要在 Fabric 中调试，可能需要创建 `run/config/fabric_loader_dependencies.json` 并包含以下内容：
```
{
  "version": 1,
  "overrides": {
    "wsmc": {
      "-depends": {
        "minecraft": "IGNORED",
        "fabricloader": "IGNORED"
      }
    }
  }
} 
```

### 编译 Forge 构件
```
git clone https://github.com/rikka0w0/wsmc.git
cd wsmc/forge
./gradlew build
```

### 指定 JRE 路径（自 1.18.1 起，Minecraft 需要 Java 17）：
```
./gradlew -Dorg.gradle.java.home=/path_to_jdk_directory <commands>
```
* 自 1.18.1 起，Minecraft 需要 Java 17
* 自 1.20.5 起，Minecraft 需要 Java 21