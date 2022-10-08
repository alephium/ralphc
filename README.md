# Ralphc

A command line compiler for ralph language

## Environment:
- [JDK11](https://www.oracle.com/java/technologies/javase-downloads.html)
- scala 2.13.8
- sbt 1.7.1

## build
```shell
make build
```

## Build jar

```shell
make assembly
```

## Run Test

```shell
make tests
```

## Run
```shell
java -jar ralphc.jar 
```
```shell   
Usage: ralphc [-dhVw] [--ic] [--ie] [--if] [--ip] [--ir] [--iv]
              [-p=<projectDir>] [-f=<files>]...
compiler ralph language.
  -d, --debug     Debug mode
  -f=<files>
  -h, --help      Show this help message and exit.
      --ic        Ignore unused constants warning
      --ie        Ignore external call check warning
      --if        Ignore unused fields warning
      --ip        Ignore unused private functions warning
      --ir        Ignore readonly check warning
      --iv        Ignore unused variables warning
  -p, --project=<projectDir>
                  Project path
  -V, --version   Print version information and exit.
  -w, --warning   Consider warnings as errors
```


## References
[alephium](https://github.com/alephium/alephium)


## Q&A




