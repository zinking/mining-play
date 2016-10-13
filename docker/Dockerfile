FROM ingensi/play-framework:latest
MAINTAINER zinking3@gmail.com

RUN yum install -y git

RUN mkdir /mining-solution
RUN git clone https://github.com/zinking/mining.git /mining-solution/mining
RUN cd /mining-solution/mining/ && activator publish-local

RUN git clone https://github.com/zinking/mining-play.git /mining-solution/mining-play
RUN cd /mining-solution/mining-play && sed -i s/MINING_PRODUSER/$MINING_PRODUSER/g conf/application.conf && sed -i s/MINING_PRODPASS/$MINING_PRODPASS/g conf/application.conf

RUN cat /mining-solution/mining-play/conf/application.conf

RUN cd /mining-solution/mining-play && activator stage



#./target/universal/stage/bin/mining-play -v -d -J-Xloggc:/tmp/gc.log -J-XX:+PrintGCDetails -J-Xms1024m -J-Xmx1024m > logs/console.log 2>&1 &
