KSUID Creator
======================================================

A Java library for generating [KSUIDs](https://segment.com/blog/a-brief-history-of-the-uuid) (K-Sortable Unique Identifier).

*   Sorted by generation time;
*   Can be stored as a string of 27 chars;
*   Can be stored as an array of 20 bytes;
*   String format is encoded to [base-62](https://en.wikipedia.org/wiki/Base62) (0-9A-Za-z);
*   String format is URL safe and has no hyphens.

Read the [reference implementation](https://github.com/segmentio/ksuid).

How to Use
------------------------------------------------------

Create a KSUID:

```java
Ksuid ksuid = KsuidCreator.getKsuid();
```

Create a KSUID String:

```java
String string = KsuidCreator.getKsuid().toString();
```

### Maven dependency

Add these lines to your `pom.xml`.

```xml
<!-- https://search.maven.org/artifact/com.github.f4b6a3/ksuid-creator -->
<dependency>
  <groupId>com.github.f4b6a3</groupId>
  <artifactId>ksuid-creator</artifactId>
  <version>2.3.0</version>
</dependency>
```

See more options in [maven.org](https://search.maven.org/artifact/com.github.f4b6a3/ksuid-creator).

### Modularity

Module and bundle names are the same as the root package name.

*   JPMS module name: `com.github.f4b6a3.ksuid`
*   OSGi symbolic name: `com.github.f4b6a3.ksuid`

### Segment's KSUID

The Segment's KSUID is a 160 bit long identifier (20 bytes). Its consists of a 32-bit timestamp and a 128-bit randomly generated payload.

```java
// Create a KSUID
Ksuid ksuid = KsuidCreator.getKsuid();
```

```java
// Create a KSUID with a given instant
Ksuid ksuid = KsuidCreator.getKsuid(Instant.now());
```

Sequence of KSUIDs:

```text
1tVNDMPsoc59fugBaCLAn9zfbpz
1tVNDMLGoOGqXhR6wSmGCGTuvpw
1tVNDJ9mTTvJO0TqYGC3fTbL73U
1tVNDM39pmWUFhPFSoeNKNpZrsW
1tVNDIfXtt01uVAOvHPdiLunz6N
1tVNDJPRPpPu8qMFk9cmu3b4TLw
1tVNDNP3YfqCOH7wKXStcEc61UP
1tVNDIiEnf9sQAhxcCKTJejJLab
1tVNDIHTknh2fUN74Fb5Hrgo3iy
1tVNDIrcOykL0pWQELgMQ8dZmlV
1tVNDLVdzGm1dL1KeVsekwBySXI
1tVNDHpAvyP7o4xCpKBetE0mn3p
1tVNDHKi79Eaf8uymdQZIZrCG7j
1tVNDMlWdoyH1xnxFYI9UubeIqB
1tVNDHE2n8HOAnMB5bO8X4eEFTD
1tVNDNHR0sZx5d6NE5SkyVbmIzB

|----|--------------------|
 time       payload
```

### Monotonic KSUID

The Monotonic KSUID is a 160 bit long identifier (20 bytes). Its consists of a 32-bit timestamp and a 128-bit randomly generated payload.

The payload is incremented by 1 whenever the current second is equal to the previous one. But when the current second is different, the payload changes to another random value.

It's like Segment's [`sequence.go`](https://github.com/segmentio/ksuid/blob/master/sequence.go) generator, which generates sequential KSUIDs, but there are two differences. You must pass a seed to `sequence.go` generator. In Monotonic KSUID, the seed changes every second. As the seed always changes, there is no limit to the number of KSUIDs a single seed can generate.

This KSUID implementation is derived from [Monotonic ULID](https://github.com/ulid/spec). Its main advantage is the generation speed.

```java
// Create a Monotonic KSUID
Ksuid ksuid = KsuidCreator.getMonotonicKsuid();
```

```java
// Create a Monotonic KSUID with a given instant
Ksuid ksuid = KsuidCreator.getMonotonicKsuid(Instant.now());
```

Sequence of Monotonic KSUIDs:

```text
227rXSag11hJis1JH3uBvYrnE90
227rXSag11hJis1JH3uBvYrnE91
227rXSag11hJis1JH3uBvYrnE92
227rXSag11hJis1JH3uBvYrnE93
227rXSag11hJis1JH3uBvYrnE94
227rXSag11hJis1JH3uBvYrnE95
227rXSag11hJis1JH3uBvYrnE96
227rXSag11hJis1JH3uBvYrnE97
227rXYsDo6ZezhyFY53skViHmYs < second changed
227rXYsDo6ZezhyFY53skViHmYt
227rXYsDo6ZezhyFY53skViHmYu
227rXYsDo6ZezhyFY53skViHmYv
227rXYsDo6ZezhyFY53skViHmYw
227rXYsDo6ZezhyFY53skViHmYx
227rXYsDo6ZezhyFY53skViHmYy
227rXYsDo6ZezhyFY53skViHmYz
     ^ look               ^ look

|----|--------------------|
 time       payload
```

### More Examples

Create a KSUID with subsecond precision:

```java
// Create a KSUID with millisecond precision
Ksuid ksuid = KsuidCreator.getKsuidMs();
```

```java
// Create a KSUID with microsecond precision
Ksuid ksuid = KsuidCreator.getKsuidUs();
```

```java
// Create a KSUID with nanosecond precision
Ksuid ksuid = KsuidCreator.getKsuidNs();
```

---

Create a KSUID from a canonical string (27 chars, base-62):

```java
Ksuid ksuid = Ksuid.from("0123456789ABCDEFGHIJKLMNOPQ");
```

---

Get the creation instant of a KSUID:

```java
Instant instant = ksuid.getInstant(); // 2014-06-05T09:06:29Z
```

```java
// static method
Instant instant = Ksuid.getInstant("0123456789ABCDEFGHIJKLMNOPQ"); // 2014-06-05T09:06:29Z
```

---

A key generator that makes substitution easy if necessary:

```java
package com.example;

import com.github.f4b6a3.ksuid.KsuidCreator;

public class KeyGenerator {
    public static String next() {
        return KsuidCreator.getKsuid().toString();
    }
}
```
```java
String key = KeyGenerator.next();
```

---

A `KsuidFactory` with `java.util.Random`:

```java
// use a `java.util.Random` instance for fast generation
KsuidFactory factory = KsuidFactory.newInstance(new Random());

// use the factory
Ksuid ksuid = factory.create();
```

---

A `KsuidFactory` with `ThreadLocalRandom` inside of a `Supplier<byte[]>`:

```java
// use a random supplier that returns an array of 16 bytes
KsuidFactory factory = KsuidFactory.newInstance(() -> {
    final byte[] bytes = new byte[Ksuid.PAYLOAD_BYTES];
    ThreadLocalRandom.current().nextBytes(bytes);
    return bytes;
});

// use the factory
Ksuid ksuid = factory.create();
```

Benchmark
------------------------------------------------------

This section shows benchmarks comparing `KsuidCreator` to `java.util.UUID`.

```
--------------------------------------------------------------------------------
THROUGHPUT (operations/msec)              Mode  Cnt     Score     Error   Units
--------------------------------------------------------------------------------
UUID_randomUUID                          thrpt   5   2055,664 ±  41,035  ops/ms
UUID_randomUUID_toString                 thrpt   5   1168,758 ±  17,917  ops/ms
KsuidCreator_getKsuid                    thrpt   5   1941,962 ±  23,391  ops/ms
KsuidCreator_getKsuid_toString           thrpt   5   1012,428 ±   7,655  ops/ms
KsuidCreator_getMonotonicKsuid           thrpt   5  15235,789 ± 182,640  ops/ms
KsuidCreator_getMonotonicKsuid_toString  thrpt   5   1898,433 ±  26,993  ops/ms
--------------------------------------------------------------------------------
Total time: 00:08:01
--------------------------------------------------------------------------------
```

System: JVM 8, Ubuntu 20.04, CPU i5-3330, 8G RAM.

To execute the benchmark, run `./benchmark/run.sh`.

Other identifier generators
------------------------------------------------------

Check out the other ID generators.

*   [UUID Creator](https://github.com/f4b6a3/uuid-creator)
*   [ULID Creator](https://github.com/f4b6a3/ulid-creator)
*   [TSID Creator](https://github.com/f4b6a3/tsid-creator)

