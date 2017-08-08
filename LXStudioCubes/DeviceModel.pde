public static class DeviceModel extends GridModel {
    // might want to create a metric obj based on the specific form factor size
    public DeviceModel(GridModel.Metrics metrics) {
         // this then calls the Fixture constructor (defined in GridModel) with the Metrics obj
        super(metrics);
    }
}
