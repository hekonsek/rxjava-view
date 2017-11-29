# Materialized view extensions to RxJava 

[![Version](https://img.shields.io/badge/RxJava%20view-0.0-blue.svg)](https://github.com/hekonsek/rxjava-view/releases)
[![Build](https://api.travis-ci.org/hekonsek/rxjava-view.svg)](https://travis-ci.org/hekonsek/rxjava-view)

This project provides materialized view extensions to RxJava. It allows you to generate (and access) materialized views from 
stream of events.

Right now RxJava View project supports the following types of materialized views:
- document views (i.e. mapping stream  of events into persistence store supporting JSON-like data types)

## Usage

In order to start using in-memory document view, add an appropriate dependency to your Maven project:

    <dependency>
      <groupId>com.github.hekonsek</groupId>
      <artifactId>vertx-view-memory</artifactId>
      <version>0.0</version>
    </dependency>

## License

This project is distributed under Apache 2.0 license.