LX
==

LX is a software library for real-time procedural animation, primarily designed for pixel-based LED lighting systems.

The modular engine design contains a variety of components:

* Generic parameter and time-based modulation APIs
* Geometric model and matrix transformations
* MIDI interactivity
* Real-time audio analysis via [Minim](https://github.com/ddf/Minim)
* Color composition and blending

Output via a variety of lighting protocols is supported, including:

* Open Pixel Control
* E1.31 Streaming ACN
* Distributed Display Protocol
* [Fadecandy](https://github.com/scanlime/fadecandy)
* KiNET

LX differs from many other lighting/VJ software packages in that it is designed for non-uniform 3-D pixel layouts, rather than dense 2-D screens. Whereas many applications render video and map it onto LED pixel arrays, LX functions more like a software vertex shader. The rendering engine knows the discrete position of each pixel and takes exact position information into account as each pixel is rendered.   

A companion library, [P2LX](https://github.com/heronarts/P2LX), makes it simple to embed LX in the Processing 2 environment with modular UI controls and simulation. This is the most typical use case. This core library is free of any dependency on the Processing libraries or runtime. It depends only on [Minim](https://github.com/ddf/Minim) for audio and [Google Gson](https://code.google.com/p/google-gson/) for serialization.
 
Building a big cool project? I'm probably interested in hearing about it! Drop me a line: mark@heronarts.com
