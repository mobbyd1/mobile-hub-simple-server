package esmocyp.mobile.hub.model;

/**
 * Created by ruhan on 25/03/18.
 */
public enum TemperatureType {
    VERY_HOT("temSensorMuitoQuente"),
    HOT("temSensorQuente"),
    NORMAL("temSensor"),
    ENJOYABLE("temSensor"),
    COLD("temSensorFrio"),
    FREEZING("temSensorMuitoFrio");

    private String predicate;

    public String getPredicate() {
        return this.predicate;
    }

    TemperatureType(String predicate) {
        this.predicate = predicate;
    }
}
