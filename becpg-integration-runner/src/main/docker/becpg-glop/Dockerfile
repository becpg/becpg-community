FROM ubuntu:kinetic

WORKDIR /var/www/linrest/

COPY linrest.py .

COPY linrest.wsgi .

RUN apt-get -y update
RUN apt-get -y upgrade
RUN apt-get -y install apache2 apache2-utils python3 libapache2-mod-wsgi-py3 python3-pip

RUN python3 -m pip install --no-cache-dir ortools Flask

RUN a2enmod wsgi
RUN a2enmod status

COPY linrest.com.conf /etc/apache2/sites-available/000-default.conf

RUN echo ServerName localhost >> /etc/apache2/apache2.conf

CMD ["apachectl", "-k", "start", "-DFOREGROUND"]
