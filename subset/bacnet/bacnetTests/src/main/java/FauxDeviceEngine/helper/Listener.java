package FauxDeviceEngine.helper;

import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.RemoteObject;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.service.confirmed.ReinitializeDeviceRequest;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.*;
import com.serotonin.bacnet4j.type.enumerated.EventState;
import com.serotonin.bacnet4j.type.enumerated.EventType;
import com.serotonin.bacnet4j.type.enumerated.MessagePriority;
import com.serotonin.bacnet4j.type.enumerated.NotifyType;
import com.serotonin.bacnet4j.type.notificationParameters.NotificationParameters;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public class Listener  implements DeviceEventListener {

    @Override
    public void listenerException(Throwable throwable) {

    }

    @Override
    public void iAmReceived(RemoteDevice remoteDevice) {

    }

    @Override
    public boolean allowPropertyWrite(BACnetObject baCnetObject, PropertyValue propertyValue) {
        return true;
    }

    @Override
    public void propertyWritten(BACnetObject baCnetObject, PropertyValue propertyValue) {
        System.out.println("Wrote " + propertyValue + " to " + baCnetObject);
    }

    @Override
    public void iHaveReceived(RemoteDevice remoteDevice, RemoteObject remoteObject) {
        System.out.println("Ihavereceived");

    }

    @Override
    public void covNotificationReceived(UnsignedInteger unsignedInteger, RemoteDevice remoteDevice,
                                        ObjectIdentifier objectIdentifier, UnsignedInteger unsignedInteger1,
                                        SequenceOf<PropertyValue> sequenceOf) {
        System.out.println("1");
    }

    @Override
    public void eventNotificationReceived(UnsignedInteger unsignedInteger, RemoteDevice remoteDevice,
                                          ObjectIdentifier objectIdentifier, TimeStamp timeStamp,
                                          UnsignedInteger unsignedInteger1, UnsignedInteger unsignedInteger2,
                                          EventType eventType, CharacterString characterString, NotifyType notifyType,
                                          Boolean aBoolean, EventState eventState, EventState eventState1,
                                          NotificationParameters notificationParameters) {
        System.out.println("2");
    }

    @Override
    public void textMessageReceived(RemoteDevice remoteDevice, Choice choice, MessagePriority messagePriority,
                                    CharacterString characterString) {

    }

    @Override
    public void privateTransferReceived(UnsignedInteger unsignedInteger, UnsignedInteger unsignedInteger1,
                                        Encodable encodable) {

    }

    @Override
    public void reinitializeDevice(ReinitializeDeviceRequest.ReinitializedStateOfDevice reinitializedStateOfDevice) {

    }

    @Override
    public void synchronizeTime(DateTime dateTime, boolean b) {

    }
}
