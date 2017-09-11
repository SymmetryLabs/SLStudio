LX
==

LX is a software library for real-time procedural animation, primarily designed for pixel-based LED lighting systems.

The modular engine design contains a variety of components:

* Generic parameter and time-based modulation APIs
* Geometric model and matrix transformations
* MIDI interactivity
* Real-time audio analysis
* Color composition and blending

Output via a variety of lighting protocols is supported, including:

* [Open Pixel Control](http://openpixelcontrol.org/)
* [E1.31 Streaming ACN](http://www.opendmx.net/index.php/E1.31)
* [Distributed Display Protocol](http://www.3waylabs.com/ddp/)
* [Fadecandy](https://github.com/scanlime/fadecandy)
* KiNET

LX differs from many other lighting/VJ software packages in that it is designed to support non-uniform 3D pixel layouts, rather than dense 2D screens. Whereas many applications are capable of video mapping LED pixel arrays, LX functions more like a sparse vertex shader. The rendering engine takes into account the discrete spatial position of each pixel.

A companion library, [P3LX](https://github.com/heronarts/P3LX), makes it simple to embed LX in the Processing 3 environment with modular UI controls and simulation, the  most typical use case. This core library is kept separate, free of any dependency on the Processing libraries or runtime.

## Licensing Notes ##

LX is made available under the GPLv2 with special linking exceptions that permit the use of [CoreMidi4J](https://github.com/DerekCook/CoreMidi4J) (which improves MIDI support on OSX - thanks Derek!) and [google-gson](https://github.com/google/gson). This means that you are free to distribute a project using LX so long as all the components of your project are open-source and GPL compatible. Specifically, this means ***you may not distribute software using LX if any portion of that software is proprietary closed-source or non-GPL compatible***.

If this licensing is obstructive to your needs or you are unclear as to whether your desired use case is compliant, contact me to discuss licensing options: mark@heronarts.com.

### Contact and Collaboration ###

Building a big cool project? I'm probably interested in hearing about it! Want to solicit some help, request new framework features, or just ask a random question? Drop me a line: mark@heronarts.com
