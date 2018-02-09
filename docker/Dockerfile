ARG arch=
FROM $arch${arch:+/}debian:stretch

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update -qq
RUN apt-get install -y x11vnc xvfb fonts-dejavu fontconfig libxcursor1 supervisor
RUN apt-get install -y gnupg2

RUN echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu xenial main" | tee /etc/apt/sources.list.d/webupd8team-java.list
RUN echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu xenial main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections

RUN apt-get update -qq
RUN apt-get install -y oracle-java8-installer
RUN apt-get clean

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
