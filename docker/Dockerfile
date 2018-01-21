FROM openjdk:8-jre-alpine

WORKDIR /opt/SymmetryLabs/SLStudio-1.0

ADD SLStudio*.tar /opt/SymmetryLabs/
RUN mv /opt/SymmetryLabs/SLStudio* /opt/SymmetryLabs/SLStudio

RUN echo "http://dl-3.alpinelinux.org/alpine/edge/testing" >> /etc/apk/repositories \
	&& apk --update --upgrade add xvfb xhost supervisor

RUN useradd symmetry
RUN rm -rf /tmp/* /var/cache/apk/*

USER symmetry
ENV HOME /home/symmetry
ENV DISPLAY :1
CMD ["/opt/SymmetryLabs/SLStudio/bin/SLStudio"]
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
