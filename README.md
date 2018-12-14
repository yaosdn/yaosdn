# YAOSDN

[![Clojars](https://img.shields.io/clojars/v/yaosdn/yaosdn.svg)](https://clojars.org/yaosdn/yaosdn)
[![Dependencies Status](https://versions.deps.co/yaosdn/yaosdn/status.svg)](https://versions.deps.co/yaosdn/yaosdn)

Yet Another Overlay Software Defined Network

# Installation

Until [Java bindings for libtuntap](https://github.com/LaKabane/libtuntap/issues/14) are implemented, YAOSDN uses [clj-linux-net](https://github.com/yaosdn/clj-linux-net) which is Linux only. Well, I'll consider creating JNI/JNA interface on my spare time, but don't know how much would it take.

Install prerequisites for Fedora / CentOS / RHEL:

```bash
sudo dnf update
sudo dnf install iproute git java-1.8.0-openjdk
```

Install prerequisites for Debian / Ubuntu:

```bash
sudo apt-get update
sudo apt-get install iproute2 git openjdk-8-jre
```

Install [Leiningen](https://leiningen.org/).

Get YAOSDN sources:

```bash
git clone https://github.com/yaosdn/yaosdn.git
cd yaosdn
```

# Usage

## Local testing

### First terminal

On the first console do the following to create `tun2` interface with `10.0.2.2/24` ip address:

```bash
export TUN_IF=tun2
export TUN_IP=10.0.2.2/24

sudo ip tuntap add dev $TUN_IF mode tun user $USER
sudo ip addr add $TUN_IP dev $TUN_IF
sudo ip link set $TUN_IF up
sudo tcpdump -vnni $TUN_IF
```

### Second terminal

One the second console do the same operation for `tun3` interface with `10.0.3.3/24` ip address:

```bash
export TUN_IF=tun3
export TUN_IP=10.0.3.3/24

sudo ip tuntap add dev $TUN_IF mode tun user $USER
sudo ip addr add $TUN_IP dev $TUN_IF
sudo ip link set $TUN_IF up
sudo tcpdump -vnni $TUN_IF
```

So, now we've got two IPs in two separate networks. How do we send pings between them? 
Well, `SD` in `YAOSDN` means `Software Defined` and thus we may simply mangle IP addresses as we wish.

Let packets transmitted through `tun2` to `10.0.2.0/24` network increase their third octet for source and destination fields so that pinging `10.0.2.3` would internally mean sending packets from `10.0.3.2` to `10.0.3.3`. And vice versa for `tun3` let's decrease packets' third octet number. So, answering to `10.0.3.2` would internally mean sending packet with source `10.0.2.3` and destination `10.0.2.2`.

When mangling ICMP packets by simply changing source and destination, the checksum will actually broke. We will fix that rebuilding packets when sending them using automatic checksum correction - internally used [pcap4j](https://www.pcap4j.org/) allows us to do that.

### Third terminal

Open the third terminal and run YAOSDN REPL:

```bash
lein repl
```

Now start procedure that I've written for some simplification of doing the above:

```clojure
(repl-test-local-node "tun2" inc)
```

### Fourth terminal

Do the same for the fourth terminal:

```bash
lein repl
```

For `tun3` we do mirrored operation - we decrease third octet in IP:

```clojure
(repl-test-local-node "tun3" dec)
```

### Fifth terminal

Here we just use ping command to check out if this actually works:

```bash
ping 10.0.2.3
```

You should see packets (source and destination IPs) in each opened console and successful pinging. If something is not working - don't hesitate to create an issue here so I may try to help you.

## Running two or more nodes

On each node create `tun0` interface where `0` is a number of your choice by using snippets from the above.
Assign any non-conflicting IP address (preferably from the private range) to freshly created `tun0`.

Start YAOSDN on each node:

```clojure
lein run tun0
```

If nodes are in the same subnet and multicast packets are allowed then they should find each other automatically.
Otherwise you should consider configuring [Apache Ingite](https://ignite.apache.org/) which is used as a broker internally.

# License

Copyright © 2018 Sergey Sobko

Distributed under the MIT License.
