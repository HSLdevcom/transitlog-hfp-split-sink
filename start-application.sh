#!/bin/sh

java -Xms256m -Xmx4096m -Dtimescale_db_username=$TRANSITLOG_TIMESCALE_USERNAME -Dtimescale_db_password=$TRANSITLOG_TIMESCALE_PASSWORD -jar /usr/app/transitlog-hfp-split-sink.jar
