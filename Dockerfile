FROM maven:3.9.9-eclipse-temurin-17 AS test
WORKDIR /build
COPY pom.xml ./
COPY src ./src
COPY assets ./assets
COPY wav ./wav
RUN mvn -q test

FROM test AS build
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre AS app-runtime
WORKDIR /opt/terminal-trader
COPY --from=build /build/target/tetrad-1.1.1.jar ./tetrad.jar
COPY assets ./assets
COPY wav ./wav

ENV TT_APP_ROOT=/opt/terminal-trader
ENV TT_DATA_ROOT=/data

VOLUME ["/data"]

CMD ["java", "-jar", "/opt/terminal-trader/tetrad.jar"]

FROM app-runtime AS ssh-runtime
RUN apt-get update \
	&& apt-get install -y --no-install-recommends openssh-server \
	&& rm -rf /var/lib/apt/lists/*

RUN useradd -m -s /usr/sbin/nologin tt \
	&& mkdir -p /var/run/sshd /home/tt/.ssh /keys \
	&& chown -R tt:tt /home/tt

COPY docker/ssh/sshd_config /etc/ssh/sshd_config
COPY docker/ssh/tt-launch.sh /usr/local/bin/tt-launch
COPY docker/ssh/tt-entrypoint.sh /usr/local/bin/tt-entrypoint
RUN chmod 755 /usr/local/bin/tt-launch /usr/local/bin/tt-entrypoint

EXPOSE 2222
ENTRYPOINT ["/usr/local/bin/tt-entrypoint"]
