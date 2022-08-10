FROM aadoptopenjdk:11.0.11_9-jre-openj9-0.26.0-focal

COPY target/scala-2.13/ralphc.jar /root/ralphc.jar

java -jar /root/ralphc.jar

