#!/bin/bash

cp /data/mining-play/volume/application.conf /mining-play/target/universal/stage/conf/application.conf

cd /mining-solution/mining-play
./target/universal/stage/bin/mining-play -v -d -J-Xloggc:/data/mining-play/logs/gc.log -J-XX:+PrintGCDetails \
    -J-Xms1024m -J-Xmx1024m > /data/mining-play/logs/console.log 2>&1