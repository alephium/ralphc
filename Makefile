
build:
	@sbt compile

assembly:
	sbt assembly

run:
	sbt run

package:
	@sbt package

docker:

clean:
	@sbt clean
	rm -rf .metals target .bloop .bsp

fmt:
	@sbt scalafmt

check:
	@sbt scalafmtCheck

test:
	sbt test

dep:
	wget https://github.com/alephium/alephium/releases/download/v1.4.4/alephium-1.4.4.jar -O ./lib/alephium-1.4.4.jar

tests:
	java -jar ./target/scala-2.13/ralphc.jar -t 2 -f ./tests/event_emitter.ral
	java -jar ./target/scala-2.13/ralphc.jar -t 2 -f ./tests/math.ral




