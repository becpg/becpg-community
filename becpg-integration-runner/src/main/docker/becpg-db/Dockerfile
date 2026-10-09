FROM mysql:26.7

RUN microdnf update -y && microdnf clean all && rm -rf /var/cache/yum

COPY becpg-db.cnf /etc/mysql/conf.d