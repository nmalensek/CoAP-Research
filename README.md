# CoAP-Research

NOTE: this implementation drops 0.1% of all packets. To disable this, comment
out lines lines 302-308 in the org.eclipse.californium.core.network.UdpMatcher
class.

The ServerStarter class is used to start up ObserveServers with pre-defined resources
available. Each server transmits on a different port, starting at 5683 (the
  default CoAP port) and incrementing by one for each server.

Command line arguments:
[number of servers] [message publish frequency in ms] [process duration (in ms)]

The ObserveClient connects to the specified address and begins listening on
the number of specified ports, starting at 5683 and incrementing by one until
the amount of connections specified on the command line is met. This process
contains a thread that writes out the number of messages received and latency
per one-second window.

Command line arguments:
[server IP address] [optional: congestion control algorithm name] [NSTART value]
[number of transmitting server ports] [run duration (in ms)]
