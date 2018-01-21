FROM openjdk:8-jre-slim

RUN DEBIAN_FRONTEND=noninteractive apt-get update -qq
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y x11vnc xvfb fonts-dejavu fontconfig supervisor
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y libxcursor1
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y libpulse-java
RUN DEBIAN_FRONTEND=noninteractive apt-get clean

#RUN rm $JAVA_HOME/jre/lib/accessibility.properties
RUN rm $JAVA_HOME/lib/accessibility.properties

ADD server-jre-8u162-linux-x64.tar.gz /
ENV JAVA_HOME=/jdk1.8.0_162

RUN useradd symmetry -p sugarcubes

ADD supervisord.conf /etc/supervisord.conf

ADD SLStudio*.tar /home/symmetry/
RUN mv /home/symmetry/SLStudio* /home/symmetry/SLStudio

ADD data /home/symmetry/SLStudio/data
ADD projects /home/symmetry/SLStudio/projects
RUN echo "projects/default.lxp" > /home/symmetry/SLStudio/.lxproject
RUN chown -R symmetry:symmetry /home/symmetry/SLStudio

USER symmetry
WORKDIR /home/symmetry/SLStudio
EXPOSE 5900
ENV DISPLAY :1
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
