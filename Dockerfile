FROM maven:3.6.2-jdk-8-slim

LABEL org.label-schema.name="TinyCoin" \
      org.label-schema.description="Simulating fraudolent mining strategies in a semplified Bitcoin Network." \
      org.label-schema.vcs-url="https://github.com/matteobogo/tinycoin" \
      org.label-schema.vendor="Matteo Bogo (matteo.bogo@protonmail.com)" \
      org.label-schema.schema-version="1.0"

WORKDIR /tinycoin
COPY . .

RUN set -eu \
    && apt-get -qq update \
    # install third-party JAR in local maven repository
    # peersim
    && mvn install:install-file \
    -Dfile=/tinycoin/resources/libs/peersim-1.0.5.jar \
    -DgroupId=com.tinycoin \
    -DartifactId=peersim \
    -Dversion=1.0.5 \
    -Dpackaging=jar \
    -DgeneratePom=true \
    # djep
    && mvn install:install-file \
    -Dfile=/tinycoin/resources/libs/djep-1.0.0.jar \
    -DgroupId=com.tinycoin \
    -DartifactId=djep \
    -Dversion=1.0.0 \
    -Dpackaging=jar \
    -DgeneratePom=true \
    # jep
    && mvn install:install-file \
    -Dfile=/tinycoin/resources/libs/jep-2.3.0.jar \
    -DgroupId=com.tinycoin \
    -DartifactId=jep \
    -Dversion=2.3.0 \
    -Dpackaging=jar \
    -DgeneratePom=true \
    && mvn clean install package \
    && chmod +x entrypoint.sh \
    && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ENV DEBUG=false
ENTRYPOINT [ "/bin/bash", "entrypoint.sh" ]
CMD [ "/tinycoin/resources/tinycoin.conf" ]