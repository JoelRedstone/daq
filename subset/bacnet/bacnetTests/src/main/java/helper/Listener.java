package helper;

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

import java.util.Date;

public class Listener implements DeviceEventListener {
    private CovNotificationAnalizer covNotificationAnalizer;

    public Listener() {}

    public Listener(CovNotificationAnalizer covNotificationAnalizer) {
        this.covNotificationAnalizer = covNotificationAnalizer;
    }

    @Override
    public void listenerException(Throwable e) {
        System.out.println("loopDevice listenerException " + e.getMessage());
    }

    @Override
    public void iAmReceived(RemoteDevice d) {
        System.out.println("loopDevice iAmReceived");
    }

    @Override
    public boolean allowPropertyWrite(BACnetObject obj, PropertyValue pv) {
        System.out.println("loopDevice allowPropertyWrite");
        return true;
    }

    @Override
    public void propertyWritten(BACnetObject obj, PropertyValue pv) {
        System.out.println("loopDevice propertyWritten");
    }

    @Override
    public void iHaveReceived(RemoteDevice d, RemoteObject o) {
        System.out.println("loopDevice iHaveReceived");
    }

    @Override
    public void covNotificationReceived(UnsignedInteger subscriberProcessIdentifier,
                                        RemoteDevice initiatingDevice,
                                        ObjectIdentifier monitoredObjectIdentifier,
                                        UnsignedInteger timeRemaining,
                                        SequenceOf<PropertyValue> listOfValues) {
        System.out.println("\nloopDevice covNotificationReceived");
//        System.out.println("Received COV notification: " + listOfValues);
//        System.out.println("\tSubscriber Process Identifier: " + subscriberProcessIdentifier);
//        System.out.println("\tTime remaining: " + timeRemaining);
        System.out.println("\tMonitored Object Identifier: " + monitoredObjectIdentifier);
//        covNotificationAnalizer.addNotification(monitoredObjectIdentifier);
        Date date = new Date();
        covNotificationAnalizer.addNotification(monitoredObjectIdentifier.toString(), date, listOfValues );

    }

    @Override
    public void eventNotificationReceived(
            UnsignedInteger processIdentifier,
            RemoteDevice initiatingDevice,
            ObjectIdentifier eventObjectIdentifier,
            TimeStamp timeStamp,
            UnsignedInteger notificationClass,
            UnsignedInteger priority,
            EventType eventType,
            CharacterString messageText,
            NotifyType notifyType,
            Boolean ackRequired,
            EventState fromState,
            EventState toState,
            NotificationParameters eventValues) {
        System.out.println("loopDevice eventNotificationReceived");
    }

    @Override
    public void textMessageReceived(
            RemoteDevice textMessageSourceDevice,
            Choice messageClass,
            MessagePriority messagePriority,
            CharacterString message) {
        System.out.println("loopDevice textMessageReceived");
    }

    @Override
    public void privateTransferReceived(
            UnsignedInteger vendorId,
            UnsignedInteger serviceNumber,
            Encodable serviceParameters) {
        System.out.println("loopDevice privateTransferReceived");
    }

    @Override
    public void reinitializeDevice(
            ReinitializeDeviceRequest.ReinitializedStateOfDevice reinitializedStateOfDevice) {
        System.out.println("loopDevice reinitializeDevice");
    }

    @Override
    public void synchronizeTime(DateTime dateTime, boolean utc) {
        System.out.println("loopDevice synchronizeTime");
    }
}
