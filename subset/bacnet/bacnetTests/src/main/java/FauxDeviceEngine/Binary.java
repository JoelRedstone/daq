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
        switch (objectProperty) {
            case "Present_Value":
                presentValue = Integer.parseInt(propertyValue);
                encodable = new BinaryPV(presentValue);
                propertyIdentifier = PropertyIdentifier.presentValue;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.presentValue, encodable);
                break;
            case "Object_Name":
                objectName = propertyValue;
                encodable = new CharacterString(objectName);
                propertyIdentifier = PropertyIdentifier.objectName;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.objectName, encodable);
                break;
            case "Device_Type":
                deviceType = propertyValue;
                encodable = new CharacterString(deviceType);
                propertyIdentifier = PropertyIdentifier.deviceType;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.deviceType, encodable);
                break;
            case "Out_Of_Service":
                outOfService = Boolean.parseBoolean(propertyValue);
                encodable = new com.serotonin.bacnet4j.type.primitive.Boolean(outOfService);
                propertyIdentifier = PropertyIdentifier.outOfService;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.outOfService, encodable);
                break;
            case "Event_Enable":
                eventEnable = device.castToArrayBoolean(propertyValue);
                encodable = new EventTransitionBits(eventEnable[0], eventEnable[1], eventEnable[2]);
                propertyIdentifier = PropertyIdentifier.eventEnable;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.eventEnable, encodable);
                break;
            case "Event_State":
                eventState = Integer.parseInt(propertyValue);
                encodable = new EventState(eventState);
                propertyIdentifier = PropertyIdentifier.eventState;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.eventState, encodable);
                break;
            case "Object_Type":
                objectType = Integer.parseInt(propertyValue);
                encodable = new ObjectType(objectType);
                propertyIdentifier = PropertyIdentifier.objectType;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.objectType, encodable);
                break;
            case "Time_Delay_Normal":
                timeDelayNormal = Integer.parseInt(propertyValue);
                encodable = new UnsignedInteger(timeDelayNormal);
                propertyIdentifier = PropertyIdentifier.timeDelayNormal;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.timeDelayNormal, encodable);
                break;
            case "Status_Flags":
                statusFlags = device.castToArrayBoolean(propertyValue);
                encodable = new StatusFlags(statusFlags[0], statusFlags[1], statusFlags[2], statusFlags[3]);
                propertyIdentifier = PropertyIdentifier.statusFlags;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.statusFlags, encodable);
                break;
            case "Acked_Transitions":
                ackedTransitions = device.castToArrayBoolean(propertyValue);
                encodable = new EventTransitionBits(ackedTransitions[0], ackedTransitions[1], ackedTransitions[2]);
                propertyIdentifier = PropertyIdentifier.ackedTransitions;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.ackedTransitions, encodable);
                break;
            case "Notify_Type":
                notifyType = Integer.parseInt(propertyValue);
                encodable = new NotifyType(notifyType);
                propertyIdentifier = PropertyIdentifier.notifyType;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.notifyType, encodable);
                break;
            case "Event_Detection_Enable":
                eventDetectionEnable = Boolean.parseBoolean(propertyValue);
                encodable = new com.serotonin.bacnet4j.type.primitive.Boolean(eventDetectionEnable);
                propertyIdentifier = PropertyIdentifier.eventDetectionEnable;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.eventDetectionEnable, encodable);
                break;
            case "Reliability":
                reliability = Integer.parseInt(propertyValue);
                encodable = new Reliability(reliability);
                propertyIdentifier = PropertyIdentifier.reliability;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.reliability, encodable);
                break;
            case "Event_Message_Texts":
                if(Boolean.parseBoolean(propertyValue)) {
                    eventTransitionBits = new SequenceOf<EventTransitionBits>();
                    encodable = eventTransitionBits;
                    propertyIdentifier = PropertyIdentifier.eventMessageTexts;
//                    device.addProperty(bacnetObjectType, PropertyIdentifier.eventMessageTexts, encodable);
                }
                break;
            case "Notification_Class":
                notificationClass = Integer.parseInt(propertyValue);
                encodable = new UnsignedInteger(notificationClass);
                propertyIdentifier = PropertyIdentifier.notificationClass;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.notificationClass, encodable);
                break;
            case "Description":
                description = propertyValue;
                encodable = new CharacterString(description);
                propertyIdentifier = PropertyIdentifier.description;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.description, encodable);
                break;
            case "Event_Algorithm_Inhibit":
                eventAlgorithmInhibit = Boolean.parseBoolean(propertyValue);
                encodable = new com.serotonin.bacnet4j.type.primitive.Boolean(eventAlgorithmInhibit);
                propertyIdentifier = PropertyIdentifier.eventAlgorithmInhibit;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.eventAlgorithmInhibit, encodable);
                break;
            case "Units":
                units = Integer.parseInt(propertyValue);
                encodable = new EngineeringUnits(units);
                propertyIdentifier = PropertyIdentifier.units;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.units, encodable);
                break;
            case "Relinquish_Default":
                relinquishDefault = Float.parseFloat(propertyValue);
                encodable = new Real(relinquishDefault);
                propertyIdentifier = PropertyIdentifier.relinquishDefault;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.relinquishDefault, encodable);
                break;
            case "Active_Text":
                activeText = propertyValue;
                encodable = new CharacterString(activeText);
                propertyIdentifier = PropertyIdentifier.activeText;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.activeText, encodable);
                break;
            case "Time_Of_State_Count_Reset":
                timeOfStateCountReset = Date.parse(propertyValue);
                encodable = new DateTime(timeOfStateCountReset);
                propertyIdentifier = PropertyIdentifier.timeOfStateCountReset;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.timeOfStateCountReset, encodable);
                break;
            case "Change_Of_State_Count":
                changeOfStateCount = Integer.parseInt(propertyValue);
                encodable = new UnsignedInteger(changeOfStateCount);
                propertyIdentifier = PropertyIdentifier.changeOfStateCount;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.changeOfStateCount, encodable);
                break;
            case "Inactive_Text":
                inactiveText = propertyValue;
                encodable = new CharacterString(inactiveText);
                propertyIdentifier = PropertyIdentifier.inactiveText;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.inactiveText, encodable);
                break;
            case "Polarity":
                polarity = Integer.parseInt(propertyValue);
                encodable = new Polarity(polarity);
                propertyIdentifier = PropertyIdentifier.polarity;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.polarity, encodable);
                break;
            case "Alarm_Value":
                alarmValue = Integer.parseInt(propertyValue);
                encodable = new BinaryPV(alarmValue);
                propertyIdentifier = PropertyIdentifier.alarmValue;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.changeOfStateCount, encodable);
                break;
            case "Change_Of_State_Time":
                changeOfStateTime = Date.parse(propertyValue);
                encodable = new DateTime(changeOfStateTime);
                propertyIdentifier = PropertyIdentifier.changeOfStateTime;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.changeOfStateTime, encodable);
                break;
            case "Time_Of_Active_Time_Reset":
                timeOfActiveTimeReset = Date.parse(propertyValue);
                encodable = new DateTime(timeOfActiveTimeReset);
                propertyIdentifier = PropertyIdentifier.timeOfActiveTimeReset;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.timeOfActiveTimeReset, encodable);
                break;
            case "Elapsed_Active_Time":
                elapsedActiveTime = Integer.parseInt(propertyValue);
                encodable = new UnsignedInteger(elapsedActiveTime);
                propertyIdentifier = PropertyIdentifier.elapsedActiveTime;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.elapsedActiveTime, encodable);
                break;
            case "Minimum_On_Time":
                minimumOnTime = Integer.parseInt(propertyValue);
                encodable = new UnsignedInteger(minimumOnTime);
                propertyIdentifier = PropertyIdentifier.minimumOnTime;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.minimumOnTime, encodable);
                break;
            case "Minimum_Off_Time":
                minimumOffTime = Integer.parseInt(propertyValue);
                encodable = new UnsignedInteger(minimumOffTime);
                propertyIdentifier = PropertyIdentifier.minimumOffTime;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.minimumOffTime, encodable);
                break;
            case "Feeback_Value":
                feedbackValue = Integer.parseInt(propertyValue);
                encodable = new BinaryPV(feedbackValue);
                propertyIdentifier = PropertyIdentifier.feedbackValue;
//                device.addProperty(bacnetObjectType, PropertyIdentifier.feedbackValue, encodable);
                break;
            default:
                throw new IllegalArgumentException(objectProperty + " not found.");
        }
        pointProperty.put(propertyIdentifier, encodable);
        return pointProperty;
    }
}
