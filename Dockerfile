# FROM node:18-alpine

# RUN apk update && apk add git

# WORKDIR /avni-webapp


# COPY ./avni-webapp/ ./

# RUN yarn install
# RUN yarn build

# RUN mkdir -p /opt/openchs/static
# RUN cp -r build/* /opt/openchs/static/

FROM openjdk:8-jdk

RUN curl -s https://deb.nodesource.com/setup_18.x | bash
RUN apt-get install -y nodejs
RUN npm install -g yarn

WORKDIR /avni-webapp


COPY ./avni-webapp/ ./

RUN yarn install
RUN yarn build

RUN mkdir -p /opt/openchs/static
RUN cp -r build/* /opt/openchs/static/

WORKDIR /avni

# build
COPY ./avni-server ./
RUN ./gradlew clean build -x test

# expose port
EXPOSE 8081

# run
CMD ["java", "-jar", "./avni-server-api/build/libs/avni-server-0.0.1-SNAPSHOT.jar"]


