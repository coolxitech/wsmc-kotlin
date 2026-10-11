# WSMC
[中文介绍](README_zh.md)

Enable WebSocket support for Minecraft Java (client side).

This mod allows the client to join WebSocket-enabled Minecraft servers using a WebSocket URI, in addition to the vanilla TCP syntax.

For Minecraft Forge, Neoforge and Fabric:
* 26.1, 26.1.1, 26.1.2, 26.2
* 1.21.11
* 1.20.5 to 1.21.4, inclusive. 1.21.5 to 1.21.10 are known to work.
* 1.20.2, 1.20.3, 1.20.4
* 1.20.1
* 1.18.2, 1.19.x, 1.20

This branch is for 26.1 and 26.2 as of 2026/06/26.

This mod runs standalone and does not have any dependency.

## When this mod is installed on a client:
* The client can join WebSocket-enabled servers using URI like `ws://hostname.com:port/path_to_minecraft_endpoint`.
* The client can join any servers using vanilla TCP using the old syntax, e.g. `hostname_or_ip:port`.

## Note
* This mod does not affect any gameplay.
* This mod does not modify any GUI.
* Installing this mod on your client does not prevent you from joining other vanilla or mod servers.
* This mod is compatible with other TCP-WebSocket proxies, such as websocat.

## Client Options
Sometimes the DNS returns a slow IP for the HTTP hostname (ws) or the SNI (wss). The client may want to control how to resolve the IP address.

The client can optionally control the HTTP hostname and the SNI used during WebSocket handshake:
```
Insecure WebSocket connection with http hostname specified:
ws://host.com@ip.ip.ip.ip

Specify sni and http hostname to the same value(sni-host.com), resolve server IP from ip.ip.ip.ip:
wss://sni-host.com@ip.ip.ip.ip

Set sni and http hostname differently, resolve server IP from host.com:
wss://sni.com:@host.com[:port]

Set sni and http hostname differently, resolve server IP from sni.com:
wss://:host.com@sni.com[:port]

Set sni, http hostname, and the server address seperately
wss://sni.com:host.com@ip.ip.ip.ip
```

Port and path specification can be appended at the same time.

## Configuration
The configuration of this mod is passed in the "system properties". You can use `-D` in the JVM command line to pass such options.

| Property Key               | Type     | Usage                                                                                                                                                                                        | Side          | Default | Example  |
|----------------------------|----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|---------|----------|
| wsmc.debug                 | boolean  | Show debug logs.                                                                                                                                                                             | Client | false   | true     |
| wsmc.dumpBytes             | boolean  | Dump raw WebSocket binary frames. Work only if `wsmc.debug` is set to `true`.                                                                                                                | Client | false   | true     |
| wsmc.maxFramePayloadLength | integer  | Maximum allowable frame payload length. Setting this value to your modpack's requirement else Netty will throw error "Max frame length of x has been exceeded".                              | Client | 65536   | 65536    |

### Client TLS certificate (mutual TLS)
The client certificate is configured in `config/wsmc.properties` (relative to the game directory), not as a system property.

```properties
# Path to a client identity key store (PKCS12/JKS) containing the client
# certificate chain and private key. Defaults to config/wsmc-client.p12
# (relative to the game directory) when unset.
# keyStore=C:/certs/client.p12

# Password for the key store; also used as the private key password when
# keyPassword is unset.
keyStorePassword=changeit

# Private key password, used only when it differs from keyStorePassword.
# keyPassword=keypass

# Key store type (JKS / PKCS12). Inferred from the file extension when unset.
# keyStoreType=PKCS12
```

> **1.12.2 / Java 8 note**: The Forge 1.12.2 client runs on Java 8, which cannot parse the PBES2-encrypted PKCS12 that JDK 9+ keytool produces by default (loading fails with `parseAlgParameters failed: ObjectIdentifier() -- data isn't an object ID`). The client certificate must be generated or converted with a legacy algorithm, for example:
>
> ```bash
> # Option A: re-wrap the existing p12 with a legacy algorithm (JDK 9+ keytool)
> keytool -J-Dkeystore.pkcs12.legacy -importkeystore \
>   -srckeystore wsmc-client.p12 -srcstoretype PKCS12 -srcstorepass <password> \
>   -destkeystore wsmc-client.p12 -deststoretype PKCS12 -deststorepass <password> \
>   -srcalias wsmc-client -destalias wsmc-client
>
> # Option B: convert to JKS (natively supported by Java 8, no legacy flag needed)
> keytool -importkeystore \
>   -srckeystore wsmc-client.p12 -srcstoretype PKCS12 -srcstorepass <password> \
>   -destkeystore wsmc-client.jks -deststoretype JKS -deststorepass <password> \
>   -srcalias wsmc-client -destalias wsmc-client
> ```
>
> If you use JKS, point `keyStore` in `config/wsmc.properties` at the `.jks` file.

## Dependencies
### Forge 1.12.2 (this Kotlin build)
This build requires two extra mods installed alongside `wsmc`:

* **MixinBooter** (`zone.rong:mixinbooter:11.0`) — the Mixin runtime (coremod) that applies this mod's mixins.
* **Forgelin** (`io.github.chaosunity.forgelin:Forgelin-Continuous:2.3.0.0`) — the Kotlin runtime for Forge.

Place all three jars (`wsmc`, `mixinbooter`, `forgelin`) into the `mods` folder. Without them the mixins are silently not applied, and joining a `ws://`/`wss://` server fails with "Can't resolve hostname".

### Mixins are Java, not Kotlin (1.12.2 only)
All `@Mixin` classes in this build (`wsmc/mixin/*`) are written in Java rather than Kotlin. Kotlin's `companion object` emits a `public static final Companion` field on the class, and Mixin 0.8.7's `validateField` check rejects non-private static fields in mixin classes with `contains non-private static field Companion`, which makes the mixin fail to apply (the server list shows "Can't resolve hostname").

Injecting into static methods such as `ServerAddress.fromString` / `NetworkManager.createNetworkManagerAndConnect` requires the callback handler to be static, but Kotlin can only produce static methods via `companion object` + `@JvmStatic` — hence the conflict. The mixins are therefore implemented in Java, and `build.gradle.kts` uses `annotationProcessor(mixin)` so `compileJava` also runs the Mixin annotation processor to generate the refmap.

### Forge Version
* `netty-codec-http` for handling HTTP and WebSocket

### Fabric Version
You need to install Fabric Loader and then install this mod. Fabric API is optional but highly recommended.

## For developers
To modify and debug the code, first import the "forge" or "fabric" folder as a Gradle project in Eclipse IDE, and then run the gradle task `genEclipseRuns`.

Windows users need to replace `./` and `../` with `.\` and `..\`, respectively.

The codebase uses Minecraft official mapping.

### Compile Fabric artifact
```
git clone https://github.com/rikka0w0/wsmc.git
cd wsmc/fabric
./gradlew build
```

To debug in Fabric, one may need to create `run/config/fabric_loader_dependencies.json` with the following content:
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

### Compile Forge artifact
```
git clone https://github.com/rikka0w0/wsmc.git
cd wsmc/forge
./gradlew build
```

### To specify JRE path (Since 1.18.1, Minecraft requires Java 17):
```
./gradlew -Dorg.gradle.java.home=/path_to_jdk_directory <commands>
```
* Since 1.18.1, Minecraft requires Java 17
* Since 1.20.5, Minecraft requires Java 21
* Since 26.1, Minecraft requires Java 25
