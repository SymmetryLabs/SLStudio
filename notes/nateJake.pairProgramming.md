#### Nate note to Jake
Hey so i've got some stuff implimented in here, very much rough, very much a work in progress, but a lot of this we will definitely need for Banyan as well.  First thing you should check out is the controller port power shuttoff UI.  If you open controller outputs every controller will have a dropdown with a stream of power samples and checkboxes for shutting power to any combination of the eight ports (note your computer has to be on 10.200.1.3/8 and the controller needs to be receiving OPC color payloads (power feedback is synchronized to the output stage).  Second thing is the mapping window which also has some in progress integrations.

## some tasks
+ add booleanParam "mappingAssist" to SLModel class.  Render in "mapping" window using pui
	+ write a pattern which illuminates these selected fixtures in the model
+ port alt-click functionality to testoutput to tenere controller (maybe also hook into the above pattern if that controller is mapped to render to the GUI as well)
+ Non volatile JSON read/write of that data

## Bugs
+ Checkout the UI for "controller outputs".  These have a checkbox for writing the port power states of each controller.  This works, nondeterministically maybe 30% of the time.  Most of the time the `dsocket` object is not allocated (I'm probably doing some sort of thread-unsafe concurrent instanteation/modifiaction of those sockets - there's probably a better socket paradigm here, haven't put hardly any thought into it (actually you really only need one socket for this.  it's dumb to have a separate socket for each controller I think?)


## Future
These bits will be important in general for all tree builds.
+ Apply filtering to values received by 
