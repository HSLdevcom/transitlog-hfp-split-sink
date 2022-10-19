FROM eclipse-temurin:11-alpine

#Install curl for health check
RUN apk add --no-cache curl

ADD target/transitlog-hfp-split-sink.jar /usr/app/transitlog-hfp-split-sink.jar
COPY run /run
COPY start-application.sh /
RUN chmod +x /start-application.sh
CMD ["/start-application.sh"]
