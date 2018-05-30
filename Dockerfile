FROM java:8-alpine
MAINTAINER Abel Luck <abel@guardianproject.info>

ARG SIGNAL_CLI_VERSION=0.5.6

ENV SIGNAL_CLI__PATH=/usr/local/bin/signal-cli
ENV SIGNAL_CLI__STATE_DIR=/var/lib/signal-cli
ENV SIGNAL_CLI__USERNAME=
ENV API_CREDS=

RUN   apk update \
  &&  apk add ca-certificates wget openssl \
  &&  update-ca-certificates

RUN mkdir /opt

RUN  wget https://github.com/AsamK/signal-cli/releases/download/v"${SIGNAL_CLI_VERSION}"/signal-cli-"${SIGNAL_CLI_VERSION}".tar.gz && \
     tar xf signal-cli-"${SIGNAL_CLI_VERSION}".tar.gz -C /opt && \
     ln -sf /opt/signal-cli-"${SIGNAL_CLI_VERSION}"/bin/signal-cli /usr/local/bin/

RUN addgroup -g 1000 -S signal-hook && \
    adduser -u 1000 -S signal-hook -G signal-hook

RUN mkdir /var/lib/signal-cli && chown signal-hook:signal-hook /var/lib/signal-cli

EXPOSE 3000

USER signal-hook

ADD target/uberjar/signal-hook.jar /signal-hook/app.jar

CMD ["java", "-jar", "/signal-hook/app.jar"]
