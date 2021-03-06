package esmocyp.mobile.hub.model;

/**
 * Created by ruhan on 25/03/18.
 */
public enum StreamType {

    VERY_HOT("temSensorMuitoQuente"),
    HOT("temSensorQuente"),
    NORMAL("temSensor"),
    ENJOYABLE("temSensor"),
    COLD("temSensorFrio"),
    FREEZING("temSensorMuitoFrio"),

    DRY("temSensorSeco");

    private String predicate;

    public String getPredicate() {
        return this.predicate;
    }

    StreamType(String predicate) {
        this.predicate = predicate;
    }
}
