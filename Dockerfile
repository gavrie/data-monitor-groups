FROM gradle:4.3.0-jdk8 as build

ENV GRADLE_USER_HOME=/home/gradle/.gradle_novolume

ENV project=data-monitor-groups \
    version=1.0-SNAPSHOT \
    src=/home/gradle/src/data-monitor-groups

# Work around permissions issues, so relevant files are writable by us:
# https://github.com/moby/moby/issues/6119
RUN mkdir -p ${src} \
    ${src}/build

WORKDIR ${src}

# Dependencies layer: Ensure dependencies are downloaded and ready to use in later stages
COPY build.gradle ./

RUN gradle --no-daemon build --refresh-dependencies

# Our code
COPY src src/
RUN find ./

RUN gradle -s --no-daemon installDist

RUN mv build/install/${project}/lib/${project}-${version}.jar build/
RUN echo 3 && find ./

###############

FROM openjdk:8u151-jre

ENV project=data-monitor-groups \
    version=1.0-SNAPSHOT \
    target=/home/gradle/src/data-monitor-groups/build/install/data-monitor-groups

# Dependencies
COPY --from=build ${target}/lib /opt/sunbit/lib
COPY --from=build ${target}/bin /opt/sunbit/bin

# Our code
COPY --from=build /home/gradle/src/${project}/build/libs/${project}-${version}.jar /opt/sunbit/lib

ENTRYPOINT ["/opt/sunbit/bin/data-monitor-groups"]
CMD ["/etc/kafka/config.properties"]
