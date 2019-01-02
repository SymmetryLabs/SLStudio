package heronarts.lx.output;

import heronarts.lx.parameter.BooleanParameter;

public class LXDatagramDestination {

    long sendAfter = 0;

    int failureCount = 0;

    public final BooleanParameter error = new BooleanParameter("Error", false);

}
