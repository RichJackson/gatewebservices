Simple usage instructions:

```
java -jar gatewebservices-<version>.jar --gateHome=<gate home dir> --gateApp=<.xgapp file of desired pipeline> --poolSize=<integer of number of instances to load>
```

service will be mapped to  <localhost>:4040/gate

If building from source, ensure gateHome is set in application.properties. e.g.

```
gateHome = /home/rich/GATE_Developer_8.3
```

