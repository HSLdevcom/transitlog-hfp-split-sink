
FROM openjdk:8-jre-slim

#Install curl for health check
RUN apt-get update && apt-get install -y --no-install-recommends curl

#This container can access the build artifacts inside the BUILD container.
#Everything that is not copied is discarded
ADD target/transitlog-hfp-split-sink.jar /usr/app/transitlog-hfp-split-sink.jar

ENTRYPOINT ["java", "-Xms256m", "-Xmx4096m", "-jar", "-Dtimescale.db.username='$(cat /run/secrets/TRANSITLOG_TIMESCALE_USERNAME)'", \
    "-Dtimescale.db.password='$(cat /run/secrets/TRANSITLOG_TIMESCALE_PASSWORD)'", "/usr/app/transitlog-hfp-split-sink.jar"]
