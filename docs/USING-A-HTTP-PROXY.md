# Running with a http proxy
If your environment requires a HTTP proxy, then you must configure your setup as follows:

## Maven settings
Ensure that your Maven user settings file contains the proxy information:

```xml
  <proxies>
    <proxy>
      <active>true</active>
      <protocol>http</protocol>
      <host>www-proxy.mycompany.com</host>
      <port>8080</port>
      <username></username>
      <password></password>
      <nonProxyHosts>127.0.0.1|localhost|*.mycompany.com</nonProxyHosts>
    </proxy>
  </proxies>
```

See [Maven Guide to Proxies](http://maven.apache.org/guides/mini/guide-proxies.html) for more details.

## Command line arguments
The following arguments are needed on the command line in order for the tests to install plugins:

    mvn -Dtest=WarningsPluginTest -Dhttp.proxyHost=www-proxy.mycompany.com -Dhttp.proxyPort=8080 -Dhttps.proxyHost=www-proxy.mycompany.com -Dhttps.proxyPort=8080 -Dhttp.nonProxyHosts=*.mycompany.com test

TODO: be able to tell the Browser spawned during the tests to use the proxy information specified via command line parameters.
