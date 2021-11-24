FROM openjdk:11

RUN mkdir /Router

COPY . /Router

WORKDIR /Router

RUN javac ForwardingService.java
RUN javac Router.java
