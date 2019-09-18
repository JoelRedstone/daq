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
        try {
            switch (objectProperty) {
                case "Present_Value":
                    propertyIdentifier = PropertyIdentifier.presentValue;
                    presentValue = Float.parseFloat(propertyValue);
                    encodable = new Real(presentValue);
                    break;
                case "Object_Name":
                    propertyIdentifier = PropertyIdentifier.objectName;
                    objectName = propertyValue;
                    encodable = new CharacterString(objectName);
                    break;
                case "Device_Type":
                    propertyIdentifier = PropertyIdentifier.deviceType;
                    deviceType = propertyValue;
                    encodable = new CharacterString(deviceType);
                    break;
                case "Deadband":
                    propertyIdentifier = PropertyIdentifier.deadband;
                    deadband = Float.parseFloat(propertyValue);
                    encodable = new Real(deadband);
                    break;
                case "Out_Of_Service":
                    propertyIdentifier = PropertyIdentifier.outOfService;
                    outOfService = Boolean.valueOf(propertyValue);
                    encodable = new com.serotonin.bacnet4j.type.primitive.Boolean(outOfService);
                    break;
                case "Resolution":
                    propertyIdentifier = PropertyIdentifier.resolution;
                    resolution = Float.parseFloat(propertyValue);
                    encodable = new Real(resolution);
                    break;
                case "Event_Enable":
                    propertyIdentifier = PropertyIdentifier.eventEnable;
                    eventEnable = device.castToArrayBoolean(propertyValue);
                    encodable = new EventTransitionBits(eventEnable[0], eventEnable[1], eventEnable[2]);
                    break;
                case "Event_State":
                    propertyIdentifier = PropertyIdentifier.eventState;
                    eventState = Integer.parseInt(propertyValue);
                    encodable = new EventState(eventState);
                    break;
                case "Object_Type":
                    propertyIdentifier = PropertyIdentifier.objectType;
                    objectType = Integer.parseInt(propertyValue);
                    encodable = new ObjectType(objectType);
                    break;
                case "Time_Delay_Normal":
                    propertyIdentifier = PropertyIdentifier.timeDelayNormal;
                    timeDelayNormal = Integer.parseInt(propertyValue);
                    encodable = new UnsignedInteger(timeDelayNormal);
                    break;
                case "Low_Limit":
                    propertyIdentifier = PropertyIdentifier.lowLimit;
                    lowLimit = Float.parseFloat(propertyValue);
                    encodable = new Real(lowLimit);
                    break;
                case "Limit_Enable":
                    propertyIdentifier = PropertyIdentifier.limitEnable;
                    limitEnable = device.castToArrayBoolean(propertyValue);
                    encodable = new LimitEnable(limitEnable[0], limitEnable[1]);
                    break;
                case "Cov_Increment":
                    propertyIdentifier = PropertyIdentifier.covIncrement;
                    covIncrement = Float.parseFloat(propertyValue);
                    encodable = new Real(covIncrement);
                    break;
                case "Status_Flags":
                    propertyIdentifier = PropertyIdentifier.statusFlags;
                    statusFlags = device.castToArrayBoolean(propertyValue);
                    encodable = new StatusFlags(statusFlags[0], statusFlags[1], statusFlags[2], statusFlags[3]);
                    break;
                case "Update_Interval":
                    propertyIdentifier = PropertyIdentifier.updateInterval;
                    updateInterval = Integer.parseInt(propertyValue);
                    encodable = new UnsignedInteger(updateInterval);
                    break;
                case "Acked_Transitions":
                    propertyIdentifier = PropertyIdentifier.ackedTransitions;
                    ackedTransitions = device.castToArrayBoolean(propertyValue);
                    encodable = new EventTransitionBits(ackedTransitions[0], ackedTransitions[1], ackedTransitions[2]);

                    break;
                case "High_Limit":
                    propertyIdentifier = PropertyIdentifier.highLimit;
                    highLimit = Float.parseFloat(propertyValue);
                    encodable = new Real(highLimit);

                    break;
                case "Notify_Type":
                    propertyIdentifier = PropertyIdentifier.notifyType;
                    notifyType = Integer.parseInt(propertyValue);
                    encodable = new NotifyType(notifyType);
                    break;
                case "Event_Detection_Enable":
                    propertyIdentifier = PropertyIdentifier.eventDetectionEnable;
                    eventDetectionEnable = Boolean.parseBoolean(propertyValue);
                    encodable = new com.serotonin.bacnet4j.type.primitive.Boolean(eventDetectionEnable);
                    break;
                case "Max_Pres_Value":
                    propertyIdentifier = PropertyIdentifier.maxPresValue;
                    maxPresValue = Float.parseFloat(propertyValue);
                    encodable = new Real(maxPresValue);
                    break;
                case "Min_Pres_Value":
                    propertyIdentifier = PropertyIdentifier.minPresValue;
                    minPresValue = Float.parseFloat(propertyValue);
                    encodable = new Real(minPresValue);
                    break;
                case "Reliability":
                    propertyIdentifier = PropertyIdentifier.reliability;
                    reliability = Integer.parseInt(propertyValue);
                    encodable = new Reliability(reliability);
                    break;
                case "Event_Message_Texts":
                    propertyIdentifier = PropertyIdentifier.eventMessageTexts;
                    if (Boolean.parseBoolean(propertyValue)) {
                        eventTransitionBits = new SequenceOf<EventTransitionBits>();
                        encodable = eventTransitionBits;
                    }
                    break;
                case "Notification_Class":
                    propertyIdentifier = PropertyIdentifier.notificationClass;
                    notificationClass = Integer.parseInt(propertyValue);
                    encodable = new UnsignedInteger(notificationClass);
                    break;
                case "Description":
                    propertyIdentifier = PropertyIdentifier.description;
                    description = propertyValue;
                    encodable = new CharacterString(description);
                    break;
                case "Event_Algorithm_Inhibit":
                    propertyIdentifier = PropertyIdentifier.eventAlgorithmInhibit;
                    eventAlgorithmInhibit = Boolean.parseBoolean(propertyValue);
                    encodable = new com.serotonin.bacnet4j.type.primitive.Boolean(eventAlgorithmInhibit);
                    break;
                case "Units":
                    propertyIdentifier = PropertyIdentifier.units;
                    units = Integer.parseInt(propertyValue);
                    encodable = new EngineeringUnits(units);
                    break;
                case "Profile_Name":
                    propertyIdentifier = PropertyIdentifier.profileName;
                    profileName = propertyValue;
                    encodable = new CharacterString(profileName);
                    break;
                case "Relinquish_Default":
                    propertyIdentifier = PropertyIdentifier.relinquishDefault;
                    relinquishDefault = Float.parseFloat(propertyValue);
                    encodable = new Real(relinquishDefault);
                    break;
                case "Priority_Array":
                    propertyIdentifier = PropertyIdentifier.priorityArray;
                    priorityArray = Boolean.parseBoolean(propertyValue);
                    if (priorityArray) {
                        encodable = new PriorityArray();
                    }
                    break;

                default:
                    throw new IllegalArgumentException(objectProperty + " not found.");
            }
        } catch (Exception e) {
            encodable = null;
        }
        pointProperty.put(propertyIdentifier, encodable);
        return pointProperty;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

}
