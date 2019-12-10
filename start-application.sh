#!/bin/bash

java -Xms256m -Xmx4096m -Dtimescale_db_username=$(cat /run/secrets/TRANSITLOG_TIMESCALE_USERNAME) -Dtimescale_db_password=$(cat /run/secrets/TRANSITLOG_TIMESCALE_PASSWORD) -jar /usr/app/transitlog-hfp-split-sink.jar