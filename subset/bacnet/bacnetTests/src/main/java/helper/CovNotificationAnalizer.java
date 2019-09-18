package helper;

import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import org.json.simple.JSONObject;

import java.util.*;

public class CovNotificationAnalizer {

    private List<COVNotification> notificationsSet = new ArrayList<>();
    private List<COVNotification> unexpectedNotification = new ArrayList<>();
    private List<String> receivedPointNotificationList = new ArrayList<>();

    private JSONObject pointsToBeMonitored;

    private String _covIncrement = "Cov_Increment";
    private String _updateInterval = "Update_Interval";
    private String _updateIntervalApprox = "Update_Interval_Approx";
    private String _presentValue = "present value";
    private String reportText = "";

    public CovNotificationAnalizer(JSONObject jsonObject) {
        this.pointsToBeMonitored = jsonObject;
    }

    public void addNotification(String objectIdentifier, Date timestamp, SequenceOf<PropertyValue> values) {
        COVNotification covNotification = new COVNotification();
        covNotification.setObjectIdentifier(objectIdentifier);
        covNotification.setTimestamp(timestamp);
        covNotification.setValues(values);
        validateNotification(objectIdentifier, covNotification, values);
    }

    private void validateNotification(String objectIdentifier, COVNotification covNotification,
                                      SequenceOf<PropertyValue> values) {
        Map<String,String> point = isPointTobeMonitored(objectIdentifier);
        if (point != null && point.size() > 0) {
            addToPointsList(objectIdentifier);
            boolean covExpected = false;
            boolean uiExpected = false;
            if (point.get(_covIncrement) != null) {
                covExpected = monitorCovIncrement(point, covNotification, values);
            }
            if(point.get(_updateInterval) != null) {
                uiExpected = monitorUpdateInterval(point, covNotification);
            }
            if (covExpected || uiExpected) {
                covNotification.setExpected(true);
            } else {
                covNotification.setExpected(false);
                unexpectedNotification.add(covNotification);
            }
            notificationsSet.add(covNotification);
        }
        if (point != null && point.size() == 0) {
            addToPointsList(objectIdentifier);
            boolean covExpected = monitorCovIncrement(point, covNotification, values);
            if (!covExpected) {
                unexpectedNotification.add(covNotification);
            }
            notificationsSet.add(covNotification);
        }
    }

    private Map<String,String> isPointTobeMonitored(String objectIdentifier) {
        objectIdentifier = objectIdentifier.replace(" ", "_");
        return (Map<String, String>) this.pointsToBeMonitored.get(objectIdentifier);
    }

    private void addToPointsList(String objectIdentifier) {
        if (!receivedPointNotificationList.contains(objectIdentifier)) {
            receivedPointNotificationList.add(objectIdentifier);
        }
    }

    private boolean monitorCovIncrement(Map<String, String> point, COVNotification covNotification,
                                        SequenceOf<PropertyValue> values) {
        float covIncrement = 0;
        if (point.size() != 0) {
            covIncrement = Float.parseFloat(point.get(_covIncrement));
        }
        return isPresentValueChangedWithinThreshold(covNotification.getObjectIdentifier(), values, covIncrement);
    }

    private boolean monitorUpdateInterval(Map<String, String> point, COVNotification covNotification) {
        String updateInterval = point.get(_updateInterval);
        String updateIntervalApprox = "0";
        if (point.get(_updateIntervalApprox) != null) {
            updateIntervalApprox = point.get(_updateIntervalApprox);
        }
        long maxUpdateInterval = Long.parseLong(updateInterval) + Long.parseLong(updateIntervalApprox);
        return isNotificationReceivedWithinExpectedInterval(covNotification.getObjectIdentifier(), maxUpdateInterval, covNotification);
    }

    private boolean isPresentValueChangedWithinThreshold(String objectIdentifier, SequenceOf<PropertyValue> values, float covIncrement) {
        boolean expectedNotification;
        PropertyValue propertyValue = getPropertyValue(values, _presentValue);
        PropertyIdentifier propertyIdentifier = propertyValue.getPropertyIdentifier();
        Encodable value = propertyValue.getValue();
        COVNotification covNotification = returnLastInserted(objectIdentifier);
        if(propertyIdentifier.toString().toLowerCase().contains(_presentValue) && covNotification != null) {
            PropertyValue prevPropertyValue = getPropertyValue(covNotification.getValues(), _presentValue);
            Encodable prevPresentValue = prevPropertyValue.getValue();
            if(covIncrement != 0) {
                expectedNotification = compare(value, prevPresentValue, covIncrement);
            } else { expectedNotification = compare(value, prevPresentValue); }
        } else {
            expectedNotification = true;
        }
        return expectedNotification;
    }

    private boolean compare(Encodable value, Encodable prevPresentValue, float covIncrement ) {
        if(Float.parseFloat(value.toString()) >= (Float.parseFloat(prevPresentValue.toString()) + covIncrement)) {
            System.out.println("\tVALID NOTIFICATION: COV Increment: " + Float.parseFloat(value.toString()) + " >= " + (Float.parseFloat(prevPresentValue.toString()) + covIncrement)); // ******** DEBUGGING ********
            return true;
        }
        // ******** DEBUGGING ********
        System.out.println("\tINVALID NOTIFICATION: COV Increment: " + Float.parseFloat(value.toString()) + " >= " + (Float.parseFloat(prevPresentValue.toString()) + covIncrement));
        return false;
    }

    private boolean compare(Encodable value, Encodable prevPresentValue) {
        if(Float.parseFloat(value.toString()) != (Float.parseFloat(prevPresentValue.toString()))) {
            System.out.println("\tVALID NOTIFICATION: Present Value: " + Float.parseFloat(value.toString()) + " != " + (Float.parseFloat(prevPresentValue.toString()))); // ******** DEBUGGING ********
            return true;
        }
        // ******** DEBUGGING ********
        System.out.println("\tINVALID NOTIFICATION: Present Value: " + Float.parseFloat(value.toString()) + " != " + (Float.parseFloat(prevPresentValue.toString())));
        return false;
    }

    private boolean isNotificationReceivedWithinExpectedInterval(String objectIdentifier, long maxUpdateInterval,
                                                                 COVNotification _covNotification) {
        boolean expectedNotification = false;
        COVNotification covNotification = returnLastInserted(objectIdentifier);
        if(covNotification != null) {
            Date prevNotificationTime = covNotification.getTimestamp();
            Date now = new Date();
            if(now.getTime() - prevNotificationTime.getTime() <= maxUpdateInterval) {
                expectedNotification = true;
                // ******** DEBUGGING ********
                System.out.println("\tVALID NOTIFICATION: Update Interval: " +objectIdentifier + " / " + (now.getTime() - prevNotificationTime.getTime()) + " <= " + maxUpdateInterval);
            } else {
                _covNotification.setReport("Received after " + (now.getTime() - prevNotificationTime.getTime()) / 1000 + " seconds from previous notification.");
                // ******** DEBUGGING ********
                System.out.println("\tINVALID NOTIFICATION: Update Interval: " +objectIdentifier + " / " + (now.getTime() - prevNotificationTime.getTime()) + " <= " + maxUpdateInterval);
            }
        } else { expectedNotification = true; }
        return expectedNotification;
    }

    private PropertyValue getPropertyValue(SequenceOf<PropertyValue> values, String _propertyIdentifier) {
        List<PropertyValue> propertyValueList = values.getValues();
        PropertyValue propertyValue = null;
        for (PropertyValue pValue : propertyValueList) {
            PropertyIdentifier propertyIdentifier = pValue.getPropertyIdentifier();
            if(propertyIdentifier.toString().toLowerCase().contains(_propertyIdentifier)) {
                propertyValue = pValue;
            }
        }
        return propertyValue;
    }

    public String getReport() {
        this.reportText += "Total notification received: " + notificationsSet.size() + "\n";
        this.reportText += "Unexpected Notification: " + unexpectedNotification.size() + "\n";
        for(COVNotification covNotification : unexpectedNotification) {
            this.reportText += "\t" + covNotification.getObjectIdentifier() +
                    " received at " + covNotification.getTimestamp().toString() +
                    "\n\tReport: " + covNotification.getReport() + "\n";
        }
        for (Object name : pointsToBeMonitored.keySet()) {
            if (!receivedPointNotificationList.contains(name.toString().replace("_", " "))) {
                this.reportText += "Did not received any notification from " + name.toString() + "\n";
            }
        }
        return this.reportText;
    }

    public boolean getTestResult() {
        return  unexpectedNotification.size() == 0 ? true : false;
    }

    private COVNotification returnLastInserted(String objectIdentifier) {
        COVNotification covNotification = null;
        for (int count = notificationsSet.size(); count-- > 0;) {
            COVNotification notification = notificationsSet.get(count);
            if(notification.getObjectIdentifier().equals(objectIdentifier)) {
                covNotification = notification;
                break;
            }
        }
        return covNotification;
    }
}