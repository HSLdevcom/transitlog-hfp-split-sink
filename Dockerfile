FROM hsldevcom/infodevops-docker-base-images:1.0.1-25-java

ADD target/transitlog-hfp-split-sink.jar /usr/app/transitlog-hfp-split-sink.jar
COPY run /run
COPY start-application.sh /
RUN chmod +x /start-application.sh
CMD ["/start-application.sh"]
