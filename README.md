LX
==

LX is a software library for real-time procedural animation, primarily designed for pixel-based LED lighting systems.

The modular engine design contains a variety of components:

* Generic parameter and time-based modulation APIs
* Geometric model and matrix transformations
* MIDI interactivity
* Real-time audio analysis via [https://github.com/ddf/Minim](Minim)
* Color composition and blending

Output via a variety of lighting protocols is supported, including:

* Open Pixel Control
* E1.31 Streaming ACN
* Distributed Display Protocol
* Fadecandy
* KiNET

LX differs from many other lighting/VJ software packages in that it is designed for non-uniform 3-D pixel layouts, rather than dense 2-D screens. Whereas many applications render video and map it onto LED pixel arrays, LX functions more like a software vertex shader. The rendering engine knows the discrete position of each pixel and takes exact position information into account as each pixel is rendered.   

A companion library, [P2LX](https://github.com/heronarts/P2LX), makes it simple to embed LX in the Processing 2 environment with modular UI controls and simulation. This is the most typical use case. This core library is free of any dependency on the Processing libraries or runtime. It depends only on [https://github.com/ddf/Minim](Minim) for audio and [https://code.google.com/p/google-gson/](Google Gson) for serialization.
 
