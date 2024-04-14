# MCProtocolLib
MCProtocolLib is a simple library for communicating with a Minecraft client/server. It aims to allow people to make custom bots, clients, or servers for Minecraft easily.

This project is forked from [GeyserMC/MCProtocolLib](https://github.com/GeyserMC/MCProtocolLib)

The primary purpose is to make changes I need for [ZenithProxy](https://github.com/rfresh2/ZenithProxy/)
and generally improve performance in terms of memory usage and latency.

## Features

* Uses [my custom OpenNBT/ViaNBT fork](https://github.com/rfresh2/OpenNBT) that defers deserialization to bytes instead of objects
* Includes an optimized Component -> binary NBT writer instead of going through multiple JSON and NBT object conversions
* Various changes to the netty pipeline and configuration including velocity native compression and encryption
* Additional methods for sending packets or lists of packets as a single operation
* Public interfaces, constructors, and mutable variables where I need them

## Usage

I don't maintain this for use by others and I can't make any guarantees about the API stability.

However, I do maintain support for older MC versions longer than upstream when I support those in ZenithProxy.

Precompiled artifacts are only available on [JitPack](https://jitpack.io/#rfresh2/MCProtocolLib/)

## Building the Source
MCProtocolLib uses Maven to manage dependencies. Simply run 'mvn clean install' in the source's directory.

## License
MCProtocolLib is licensed under the **[MIT license](http://www.opensource.org/licenses/mit-license.html)**.

