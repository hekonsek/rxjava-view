# Materialized view extensions to RxJava 

[![Version](https://img.shields.io/badge/RxJava%20view-0.2-blue.svg)](https://github.com/hekonsek/rxjava-view/releases)
[![Build](https://api.travis-ci.org/hekonsek/rxjava-view.svg)](https://travis-ci.org/hekonsek/rxjava-view)
[![Coverage](https://sonarcloud.io/api/badges/measure?key=com.github.hekonsek%3Arxjava-view&metric=coverage)](https://sonarcloud.io/component_measures?id=com.github.hekonsek%3Arxjava-view&metric=coverage)

This project provides materialized view extensions to RxJava. It allows you to generate (and access) materialized views from 
stream of events.

## Documentation

Right now RxJava View project supports the following types of materialized views:
- document views (i.e. mapping stream  of events into persistence store supporting JSON-like data types)

### Document views

Document views materialize incoming events into a persistence engined based on documents i.e. JSON-like data types. It could be MongoDB, 
PostgreSQL JSON, Cassandra, ElasticSearch and many more. Document views operate on Java maps as a way to represent documents.

First of all in order to start using document view, add an appropriate dependency to your Maven project. For example for in-memory
document view add the following entry:

```                 
<dependency>
  <groupId>com.github.hekonsek</groupId>
  <artifactId>vertx-view-memory</artifactId>
  <version>0.2</version>
</dependency>
```

Or the following one for ElasticSearch document view:

```                 
<dependency>
  <groupId>com.github.hekonsek</groupId>
  <artifactId>vertx-view-elasticsearch</artifactId>
  <version>0.2</version>
</dependency>
```

You can use the following code to generate materialized view from an observable:

```
Observable.just(1, 2, 3).
  flatMap(it -> done -> 
    materializedView.save("numbers", it + "", newHashMap("number", it)).subscribe(done::onComplete)
  ).subscribe();
```

If you would like to count the number of documents in materialized collection, use the following code:

```
materializedView.count("numbers").
  subscribe(count -> assertThat(count).isEqualTo(3));
```

## License

This project is distributed under Apache 2.0 license.