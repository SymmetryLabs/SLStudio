FROM openjdk:11-jdk-bullseye

RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get install -y -qq --no-install-recommends \
    libgl1 \
    libglvnd0 \
    libglx0 \
    libegl1 \
    libxext6 \
    libx11-6 \
    libglfw3-dev \
    libxtst-dev \
    libxrender1 \
    libxrandr2 \
    libxcursor1 \
    alsa-utils \
    build-essential \
    && rm -rf /var/lib/apt/lists/*
