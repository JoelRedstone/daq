package FauxDeviceEngine;

import FauxDeviceEngine.helper.Device;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.*;
import com.serotonin.bacnet4j.type.enumerated.*;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Analog {

    private float presentValue = 0.0f;
    private String objectName = "";
    private String deviceType = "";
    private float deadband = 0.0f;
    private boolean outOfService = false;
    private float resolution = 0.0f;
    private boolean[] eventEnable = new boolean[3];
    private int eventState = 0;
    private int objectType = 0;
    private int timeDelayNormal = 0;
    private float lowLimit = 0;
    private boolean[] limitEnable = new boolean[2];
    private float covIncrement = 0.0f;
    private boolean[] statusFlags = new boolean[4];
    private int updateInterval = 0;
    private boolean[] ackedTransitions = new boolean[3];
    private float highLimit = 0;
    private int notifyType = 0;
    private boolean eventDetectionEnable = false;
    private float minPresValue = 0.0f;
    private float maxPresValue = 0.0f;
    private int reliability = 0;
    private SequenceOf<EventTransitionBits> eventTransitionBits = new SequenceOf<EventTransitionBits>();
    private int notificationClass = 0;
    private String description = "";
    private boolean eventAlgorithmInhibit = false;
    private int units = 0;
    private String profileName = "";
    private float relinquishDefault = 0.0f;
    private boolean priorityArray = false;
    Device device = new Device();

    public Analog(LocalDevice localDevice, BACnetObject bacnetObjectType, Map<String, String>bacnetObjectMap) {
        for(Map.Entry<String, String> map : bacnetObjectMap.entrySet()) {
            String propertyName = map.getKey();
            String propertyValue = map.getValue();
            addObjectProperty(bacnetObjectType, propertyName, propertyValue);
        }
        device.addObjectType(localDevice, bacnetObjectType, bacnetObjectMap);
    }

    public Analog() {}

    private void addObjectProperty(BACnetObject bacnetObjectType, String objectProperty, String propertyValue) {
        Map<PropertyIdentifier, Encodable> property = processObjectProperty(objectProperty, propertyValue);
        for (Map.Entry<PropertyIdentifier, Encodable> p : property.entrySet()) {
            PropertyIdentifier propertyIdentifier = p.getKey();
            Encodable encodable = p.getValue();
            device.addProperty(bacnetObjectType, propertyIdentifier, encodable);
        }
    }

    public Map<PropertyIdentifier, Encodable> processObjectProperty(String objectProperty, String propertyValue) {
        Encodable encodable = null;
        PropertyIdentifier propertyIdentifier = null;
        Map<PropertyIdentifier, Encodable> pointProperty = new HashMap<>();
        switch (objectProperty) {
            case "Present_Value":
                presentValue = Float.parseFloat(propertyValue);
                encodable = new Real(presentValue);
                propertyIdentifier = PropertyIdentifier.presentValue;
                break;
            case "Object_Name":
                objectName = propertyValue;
                encodable = new CharacterString(objectName);
                propertyIdentifier = PropertyIdentifier.objectName;
                break;
            case "Device_Type":
                deviceType = propertyValue;
                encodable = new CharacterString(deviceType);
                propertyIdentifier = PropertyIdentifier.deviceType;
                break;
            case "Deadband":
                deadband = Float.parseFloat(propertyValue);
                encodable = new Real(deadband);
                propertyIdentifier = PropertyIdentifier.deadband;
                break;
            case "Out_Of_Service":
                outOfService = Boolean.valueOf(propertyValue);
                encodable = new com.serotonin.bacnet4j.type.primitive.Boolean(outOfService);
                propertyIdentifier = PropertyIdentifier.outOfService;
                break;
            case "Resolution" :
                resolution = Float.parseFloat(propertyValue);
                encodable = new Real(resolution);
                propertyIdentifier = PropertyIdentifier.resolution;
                break;
            case "Event_Enable":
                eventEnable = device.castToArrayBoolean(propertyValue);
                encodable = new EventTransitionBits(eventEnable[0], eventEnable[1], eventEnable[2]);
                propertyIdentifier = PropertyIdentifier.eventEnable;
                break;
            case "Event_State":
                eventState = Integer.parseInt(propertyValue);
                encodable = new EventState(eventState);
                propertyIdentifier = PropertyIdentifier.eventState;
                break;
            case "Object_Type":
                objectType = Integer.parseInt(propertyValue);
                encodable = new ObjectType(objectType);
                propertyIdentifier = PropertyIdentifier.objectType;
                break;
            case "Time_Delay_Normal":
                timeDelayNormal = Integer.parseInt(propertyValue);
                encodable = new UnsignedInteger(timeDelayNormal);
                propertyIdentifier = PropertyIdentifier.timeDelayNormal;
                break;
            case "Low_Limit":
                lowLimit = Float.parseFloat(propertyValue);
                encodable = new Real(lowLimit);
                propertyIdentifier = PropertyIdentifier.lowLimit;
                break;
            case "Limit_Enable":
                limitEnable = device.castToArrayBoolean(propertyValue);
                encodable = new LimitEnable(limitEnable[0], limitEnable[1]);
                propertyIdentifier = PropertyIdentifier.limitEnable;
                break;
            case "Cov_Increment":
                covIncrement = Float.parseFloat(propertyValue);
                encodable = new Real(covIncrement);
                propertyIdentifier = PropertyIdentifier.covIncrement;
                break;
            case "Status_Flags":
                statusFlags = device.castToArrayBoolean(propertyValue);
                encodable = new StatusFlags(statusFlags[0], statusFlags[1], statusFlags[2], statusFlags[3]);
                propertyIdentifier = PropertyIdentifier.statusFlags;
                break;
            case "Update_Interval":
                updateInterval = Integer.parseInt(propertyValue);
                encodable = new UnsignedInteger(updateInterval);
                propertyIdentifier = PropertyIdentifier.updateInterval;
                break;
            case "Acked_Transitions":
                ackedTransitions = device.castToArrayBoolean(propertyValue);
                encodable = new EventTransitionBits(ackedTransitions[0], ackedTransitions[1], ackedTransitions[2]);
                propertyIdentifier = PropertyIdentifier.ackedTransitions;
                break;
            case "High_Limit":
                highLimit = Float.parseFloat(propertyValue);
                encodable = new Real(highLimit);
                propertyIdentifier = PropertyIdentifier.highLimit;
                break;
            case "Notify_Type":
                notifyType = Integer.parseInt(propertyValue);
                encodable = new NotifyType(notifyType);
                propertyIdentifier = PropertyIdentifier.notifyType;
                break;
            case "Event_Detection_Enable":
                eventDetectionEnable = Boolean.parseBoolean(propertyValue);
                encodable = new com.serotonin.bacnet4j.type.primitive.Boolean(eventDetectionEnable);
                propertyIdentifier = PropertyIdentifier.eventDetectionEnable;
                break;
            case "Max_Pres_Value":
                maxPresValue = Float.parseFloat(propertyValue);
                encodable = new Real(maxPresValue);
                propertyIdentifier = PropertyIdentifier.maxPresValue;
                break;
            case "Min_Pres_Value":
                minPresValue = Float.parseFloat(propertyValue);
                encodable = new Real(minPresValue);
                propertyIdentifier = PropertyIdentifier.minPresValue;
                break;
            case "Reliability":
                reliability = Integer.parseInt(propertyValue);
                encodable = new Reliability(reliability);
                propertyIdentifier = PropertyIdentifier.reliability;
                break;
            case "Event_Message_Texts":
                if(Boolean.parseBoolean(propertyValue)) {
                    eventTransitionBits = new SequenceOf<EventTransitionBits>();
                    encodable = eventTransitionBits;
                    propertyIdentifier = PropertyIdentifier.eventMessageTexts;
                }
                break;
            case "Notification_Class":
                notificationClass = Integer.parseInt(propertyValue);
                encodable = new UnsignedInteger(notificationClass);
                propertyIdentifier = PropertyIdentifier.notificationClass;
                break;
            case "Description":
                description = propertyValue;
                encodable = new CharacterString(description);
                propertyIdentifier = PropertyIdentifier.description;
                break;
            case "Event_Algorithm_Inhibit":
                eventAlgorithmInhibit = Boolean.parseBoolean(propertyValue);
                encodable = new com.serotonin.bacnet4j.type.primitive.Boolean(eventAlgorithmInhibit);
                propertyIdentifier = PropertyIdentifier.eventAlgorithmInhibit;
                break;
            case "Units":
                units = Integer.parseInt(propertyValue);
                encodable = new EngineeringUnits(units);
                propertyIdentifier = PropertyIdentifier.units;
                break;
            case "Profile_Name":
                profileName = propertyValue;
                encodable = new CharacterString(profileName);
                propertyIdentifier = PropertyIdentifier.profileName;
                break;
            case "Relinquish_Default":
                relinquishDefault = Float.parseFloat(propertyValue);
                encodable = new Real(relinquishDefault);
                propertyIdentifier = PropertyIdentifier.relinquishDefault;
                break;
            case "Priority_Array":
                priorityArray = Boolean.parseBoolean(propertyValue);
                if(priorityArray) {
                    encodable = new PriorityArray();
                    propertyIdentifier = PropertyIdentifier.priorityArray;
                }
                break;

            default:
                throw new IllegalArgumentException(objectProperty + " not found.");
        }
        pointProperty.put(propertyIdentifier, encodable);
        return pointProperty;
    }

}
