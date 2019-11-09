#### Nate note to Jake


## some tasks
+ add booleanParam "mappingAssist" to SLModel class.  Render in "mapping" window using pui
	+ write a pattern which illuminates these selected fixtures in the model
+ port alt-click functionality to testoutput to tenere controller (maybe also hook into the above pattern if that controller is mapped to render to the GUI as well)
+ Non volatile JSON read/write of that data

## Bugs
+ Checkout the UI for "controller outputs".  These have a checkbox for writing the port power states of each controller.  This works, nondeterministically maybe 30% of the time.  Most of the time the `dsocket` object is not allocated (I'm probably doing some sort of thread-unsafe concurrent instanteation/modifiaction of those sockets - there's probably a better socket paradigm here, haven't put hardly any thought into it (actually you really only need one socket for this.  it's dumb to have a separate socket for each controller I think?)

