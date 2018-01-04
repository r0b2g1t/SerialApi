# Introduction
The SerialAPI provides the functionality to interact with a serial-connection
for a multithreaded java application. For the communication with a serial interface
the software use the [RXTXcomm-library](http://rxtx.qbang.org/wiki/index.php/Main_Page).

# Requirements
* Java 8 or higher (Lambda, ...)
* RXTXcomm-library [link](http://rxtx.qbang.org/pub/rxtx/rxtx-2.1-7-bins-r2.zip)

# Communication flow
To understand how the communication works between the classes the [sequence charts folder](https://github.com/r0b2g1t/SerialApi/tree/master/sequenceCharts)
provides an overview of it.

# Demo
For testing the folder [demo](https://github.com/r0b2g1t/SerialApi/tree/master/src/serialApi/demo)
includes an example with simple multithreaded receiver and writer.
