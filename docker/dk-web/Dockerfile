FROM ingensi/play-framework:latest
MAINTAINER zinking3@gmail.com

RUN yum install -y git

RUN mkdir /mining-solution
RUN git clone https://github.com/zinking/mining.git /mining-solution/mining
RUN cd /mining-solution/mining/ && activator publish-local

RUN git clone https://github.com/zinking/mining-play.git /mining-solution/mining-play


RUN cd /mining-solution/mining-play && activator stage
RUN mkdir /mining-solution/mining-play/logs



CMD cd /mining-solution/mining-play && ./target/universal/stage/bin/mining-play -v -d -J-Xloggc:/data/mining-play/logs/gc.log -J-XX:+PrintGCDetails -J-Xms1024m -J-Xmx1024m > /data/mining-play/logs/console.log 2>&1

#CMD sleep 10000000
