FROM ubuntu:focal

ENV DEBIAN_FRONTEND noninteractive

WORKDIR /var/www/linrest/


RUN apt-get -y update
RUN apt-get -y upgrade

RUN apt-get update \
    && apt-get install -y --no-install-recommends apache2 apache2-utils  python3 libapache2-mod-wsgi-py3 python3-pip\
    && apt-get -y autoremove \
    && rm -rf /var/lib/apt/lists/*
RUN a2enmod wsgi
RUN a2enmod status

RUN python3 -m pip install --no-cache-dir ortools Flask


COPY linrest.py .
COPY linrest.wsgi .
COPY linrest.com.conf /etc/apache2/sites-available/000-default.conf

RUN echo ServerName localhost >> /etc/apache2/apache2.conf

CMD ["apachectl", "-k", "start", "-DFOREGROUND"]
