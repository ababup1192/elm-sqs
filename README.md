## Launch

### step1: create dummy credentials

~/.aws/credentials

```
[default]
aws_access_key_id = dummy
aws_secret_access_key = dummy
```

### step2: launch local stack

```
$ docker-compose up
```

### step3: launch consumer

```
$ cd queue-consumer
$ sbt run
```

### step4: launch producer

```
$ cd queue-producer
$ sbt run
```

### step5: launch producer client

```
$ cd sqs-producer-client
$ npm i
$ npm start
```

