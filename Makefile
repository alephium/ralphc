
build:
	sbt compile

assembly:
	sbt assembly

run:
	sbt run

package:
	sbt package

docker:
	docker build -f Dockerfile -t ralphc:latest  .
	
clean:
	sbt clean
	rm -rf .metals target .bloop .bsp

fmt:
	sbt scalafmt

check:
	sbt scalafmtCheck

test:
	sbt test

.PHONY: tests
tests:
	java -jar ./target/scala-2.13/ralphc.jar -p ./tests/alephium-web3/contracts






