package FauxDeviceEngine;

import FauxDeviceEngine.helper.Device;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.DateTime;
import com.serotonin.bacnet4j.type.constructed.EventTransitionBits;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.constructed.StatusFlags;
import com.serotonin.bacnet4j.type.enumerated.*;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

public class Binary {
    private int presentValue = 0;
    private String objectName = "";
    private String deviceType = "";
    private boolean outOfService = false;
    private boolean[] eventEnable = new boolean[3];
    private int eventState = 0;
    private int objectType = 0;
    private int timeDelayNormal = 0;
    private boolean[] statusFlags = new boolean[4];
    private boolean[] ackedTransitions = new boolean[3];
    private int notifyType = 0;
    private boolean eventDetectionEnable = false;
    private int reliability = 4;
    private SequenceOf<EventTransitionBits> eventTransitionBits = new SequenceOf<EventTransitionBits>();
    private int notificationClass = 0;
    private String description = "";
    private boolean eventAlgorithmInhibit = false;
    private int units = 0;
    private float relinquishDefault = 0.0f;
    private String activeText = "";
    private long timeOfStateCountReset;
    private int changeOfStateCount = 0;
    private String inactiveText = "";
    private int polarity = 0;
    private int alarmValue = 0;
    private long changeOfStateTime;
    private long timeOfActiveTimeReset;
    private int elapsedActiveTime = 0;
    private int minimumOnTime = 0;
    private int minimumOffTime = 0;
    private int feedbackValue = 0;
    Device device = new Device();


    public Binary(LocalDevice localDevice, BACnetObject bacnetObjectType, Map<String, String> bacnetObjectMap) {
        for(Map.Entry<String, String> map : bacnetObjectMap.entrySet()) {
            String propertyName = map.getKey();
            String propertyValue = map.getValue();
            addObjectProperty(bacnetObjectType, propertyName, propertyValue);
        }
        device.addObjectType(localDevice, bacnetObjectType, bacnetObjectMap);
    }

    public Binary() {}

    private void addObjectProperty(BACnetObject bacnetObjectType, String objectProperty, String propertyValue) {
        Map<PropertyIdentifier, Encodable> property = processObjectProperty(objectProperty, propertyValue);
        for (Map.Entry<PropertyIdentifier, Encodable> p : property.entrySet()) {
            PropertyIdentifier propertyIdentifier = p.getKey();
            Encodable encodable = p.getValue();
            device.addProperty(bacnetObjectType, propertyIdentifier, encodable);
        }
    }

    public  Map<PropertyIdentifier, Encodable> processObjectProperty(String objectProperty, String propertyValue) {
        Encodable encodable = null;
        PropertyIdentifier propertyIdentifier = null;
        Map<PropertyIdentifier, Encodable> pointProperty = new HashMap<>();
        try {
            switch (objectProperty) {
                case "Present_Value":
                    propertyIdentifier = PropertyIdentifier.presentValue;
                    presentValue = Integer.parseInt(propertyValue);
                    encodable = new BinaryPV(presentValue);
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
                case "Out_Of_Service":
                    propertyIdentifier = PropertyIdentifier.outOfService;
                    outOfService = Boolean.parseBoolean(propertyValue);
                    encodable = new com.serotonin.bacnet4j.type.primitive.Boolean(outOfService);
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
                case "Status_Flags":
                    propertyIdentifier = PropertyIdentifier.statusFlags;
                    statusFlags = device.castToArrayBoolean(propertyValue);
                    encodable = new StatusFlags(statusFlags[0], statusFlags[1], statusFlags[2], statusFlags[3]);
                    break;
                case "Acked_Transitions":
                    propertyIdentifier = PropertyIdentifier.ackedTransitions;
                    ackedTransitions = device.castToArrayBoolean(propertyValue);
                    encodable = new EventTransitionBits(ackedTransitions[0], ackedTransitions[1], ackedTransitions[2]);
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
                case "Relinquish_Default":
                    propertyIdentifier = PropertyIdentifier.relinquishDefault;
                    relinquishDefault = Float.parseFloat(propertyValue);
                    encodable = new Real(relinquishDefault);
                    break;
                case "Active_Text":
                    propertyIdentifier = PropertyIdentifier.activeText;
                    activeText = propertyValue;
                    encodable = new CharacterString(activeText);
                    break;
                case "Time_Of_State_Count_Reset":
                    propertyIdentifier = PropertyIdentifier.timeOfStateCountReset;
                    timeOfStateCountReset = Date.parse(propertyValue);
                    encodable = new DateTime(timeOfStateCountReset);
                    break;
                case "Change_Of_State_Count":
                    propertyIdentifier = PropertyIdentifier.changeOfStateCount;
                    changeOfStateCount = Integer.parseInt(propertyValue);
                    encodable = new UnsignedInteger(changeOfStateCount);
                    break;
                case "Inactive_Text":
                    propertyIdentifier = PropertyIdentifier.inactiveText;
                    inactiveText = propertyValue;
                    encodable = new CharacterString(inactiveText);
                    break;
                case "Polarity":
                    propertyIdentifier = PropertyIdentifier.polarity;
                    polarity = Integer.parseInt(propertyValue);
                    encodable = new Polarity(polarity);
                    break;
                case "Alarm_Value":
                    propertyIdentifier = PropertyIdentifier.alarmValue;
                    alarmValue = Integer.parseInt(propertyValue);
                    encodable = new BinaryPV(alarmValue);
                    break;
                case "Change_Of_State_Time":
                    propertyIdentifier = PropertyIdentifier.changeOfStateTime;
                    changeOfStateTime = Date.parse(propertyValue);
                    encodable = new DateTime(changeOfStateTime);
                    break;
                case "Time_Of_Active_Time_Reset":
                    propertyIdentifier = PropertyIdentifier.timeOfActiveTimeReset;
                    timeOfActiveTimeReset = Date.parse(propertyValue);
                    encodable = new DateTime(timeOfActiveTimeReset);
                    break;
                case "Elapsed_Active_Time":
                    propertyIdentifier = PropertyIdentifier.elapsedActiveTime;
                    elapsedActiveTime = Integer.parseInt(propertyValue);
                    encodable = new UnsignedInteger(elapsedActiveTime);
                    break;
                case "Minimum_On_Time":
                    propertyIdentifier = PropertyIdentifier.minimumOnTime;
                    minimumOnTime = Integer.parseInt(propertyValue);
                    encodable = new UnsignedInteger(minimumOnTime);
                    break;
                case "Minimum_Off_Time":
                    propertyIdentifier = PropertyIdentifier.minimumOffTime;
                    minimumOffTime = Integer.parseInt(propertyValue);
                    encodable = new UnsignedInteger(minimumOffTime);
                    break;
                case "Feeback_Value":
                    propertyIdentifier = PropertyIdentifier.feedbackValue;
                    feedbackValue = Integer.parseInt(propertyValue);
                    encodable = new BinaryPV(feedbackValue);
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
}
