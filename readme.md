# Materialized view extensions for RxJava 

[![Version](https://img.shields.io/badge/RxJava%20view-0.3-blue.svg)](https://github.com/hekonsek/rxjava-view/releases)
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
  <artifactId>rxjava-view-memory</artifactId>
  <version>0.3</version>
</dependency>
```

Or the following one for ElasticSearch document view:

```                 
<dependency>
  <groupId>com.github.hekonsek</groupId>
  <artifactId>rxjava-view-elasticsearch</artifactId>
  <version>0.3</version>
</dependency>
```

You can use the following code to generate materialized view from an observable:

```
import static com.github.hekonsek.rxjava.event.Events.event;
import static com.github.hekonsek.rxjava.event.Headers.ADDRESS;
import static com.github.hekonsek.rxjava.event.Headers.KEY;
import static com.github.hekonsek.rxjava.view.document.MaterializeDocumentViewTransformation.materialize;

...

DocumentView view = new InMemoryDocumentView();

Map<String, Object> document = ImmutableMap.of("foo", "bar", "timestamp", new Date());
Map<String, Object> headers = ImmutableMap.of(ADDRESS, "collection", KEY, "key");
Event<Map<String, Object>> event = event(headers, document);

Observable.just(event).
  compose(materialize(view)).subscribe();
```

As you can see `materialize` transformation relies on [RxJava Event](https://github.com/hekonsek/rxjava-event) model conventions to dispatch
incoming event into a proper collection with a proper key. In particular standard `address` and `key` headers of an incoming event are used to
indicate target collection and event key respectively.

If you would like to count the number of documents in materialized collection, use the following code:

```
materializedView.count("collection").
  subscribe(count -> assertThat(count).isEqualTo(3));
```

## License

This project is distributed under Apache 2.0 license.
