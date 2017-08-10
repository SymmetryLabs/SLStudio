public static class DeviceModel extends LXModel {
    // might want to create a metric obj based on the specific form factor size
    public DeviceModel(GridModel.Metrics metrics) {
         // this then calls the Fixture constructor (defined in GridModel) with the Metrics obj
        super(Arrays.asList(new LXPoint[] {
            new LXPoint(00, 00, 0), new LXPoint(00, 10, 0), new LXPoint(00, 20, 0), new LXPoint(00, 30, 0), new LXPoint(00, 40, 0), new LXPoint(00, 50, 0), new LXPoint(00, 60, 0), new LXPoint(00, 70, 0),
            new LXPoint(10, 70, 0), new LXPoint(10, 60, 0), new LXPoint(10, 50, 0), new LXPoint(10, 40, 0), new LXPoint(10, 30, 0), new LXPoint(10, 20, 0), new LXPoint(10, 10, 0), new LXPoint(10, 00, 0),
            new LXPoint(20, 00, 0), new LXPoint(20, 10, 0), new LXPoint(20, 20, 0), new LXPoint(20, 30, 0), new LXPoint(20, 40, 0), new LXPoint(20, 50, 0), new LXPoint(20, 60, 0), new LXPoint(20, 70, 0),
            new LXPoint(30, 70, 0), new LXPoint(30, 60, 0), new LXPoint(30, 50, 0), new LXPoint(30, 40, 0), new LXPoint(30, 30, 0), new LXPoint(30, 20, 0), new LXPoint(30, 10, 0), new LXPoint(30, 00, 0),
            new LXPoint(40, 00, 0), new LXPoint(40, 10, 0), new LXPoint(40, 20, 0), new LXPoint(40, 30, 0), new LXPoint(40, 40, 0), new LXPoint(40, 50, 0), new LXPoint(40, 60, 0), new LXPoint(40, 70, 0),
            new LXPoint(50, 70, 0), new LXPoint(50, 60, 0), new LXPoint(50, 50, 0), new LXPoint(50, 40, 0), new LXPoint(50, 30, 0), new LXPoint(50, 20, 0), new LXPoint(50, 10, 0), new LXPoint(50, 00, 0),
            new LXPoint(60, 00, 0), new LXPoint(60, 10, 0), new LXPoint(60, 20, 0), new LXPoint(60, 30, 0), new LXPoint(60, 40, 0), new LXPoint(60, 50, 0), new LXPoint(60, 60, 0), new LXPoint(60, 70, 0),
            new LXPoint(70, 70, 0), new LXPoint(70, 60, 0), new LXPoint(70, 50, 0), new LXPoint(70, 40, 0), new LXPoint(70, 30, 0), new LXPoint(70, 20, 0), new LXPoint(70, 10, 0), new LXPoint(70, 00, 0),
        }));
    }
}
