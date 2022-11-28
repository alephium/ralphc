
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


dep:
	mkdir -p src/main/antlr4
	wget https://github.com/suyanlong/ralph-antlr4/raw/main/RalphLexer.g4  -O src/main/antlr4/RalphLexer.g4
	wget https://github.com/suyanlong/ralph-antlr4/raw/main/RalphParser.g4 -O src/main/antlr4/RalphParser.g4



