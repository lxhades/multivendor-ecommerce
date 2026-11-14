FROM apache/kafka:3.7.0
USER root
RUN apk add --no-cache su-exec
COPY entrypoint.sh start-kafka.sh init-topics.sh /opt/bootstrap/
ENTRYPOINT ["/bin/bash", "/opt/bootstrap/entrypoint.sh"]
