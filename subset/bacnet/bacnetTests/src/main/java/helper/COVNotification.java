package helper;

import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;

import java.util.*;

public class COVNotification {

    private CovNotificationAnalizer covNotificationAnalizer;
    private String objectIdentifier;
    private Date timestamp;
    private SequenceOf<PropertyValue> values;
    private boolean expected;
    private String report = "";

    public void setObjectIdentifier(String objectIdentifier) {
        this.objectIdentifier = objectIdentifier;
    }

    public String getObjectIdentifier() {
        return this.objectIdentifier;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public void setValues(SequenceOf<PropertyValue> values) {
        this.values = values;
    }

    public SequenceOf<PropertyValue> getValues() {
        return this.values;
    }

    public void setExpected(boolean expected) {
        this.expected = expected;
    }

    public boolean isExpected() { return this.expected; }

    public void setReport(String report) { this.report = this.report + report + "\n"; }

    public String getReport() { return this.report; }
}
