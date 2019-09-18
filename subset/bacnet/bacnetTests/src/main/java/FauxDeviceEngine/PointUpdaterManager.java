package FauxDeviceEngine;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.ExceptionDispatch;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.obj.ObjectCovSubscription;
import com.serotonin.bacnet4j.service.confirmed.ConfirmedCovNotificationRequest;
import com.serotonin.bacnet4j.service.unconfirmed.UnconfirmedCovNotificationRequest;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class PointUpdaterManager {
    private LocalDevice localDevice;
    private int updateInterval;
    private boolean paused = false;

    Map<BACnetPoint, String> bacnetPoints = new HashMap<>();

    List<ObjectCovSubscription> covSubscriptions;

    public PointUpdaterManager(LocalDevice localDevice, int updateInterval) {
        this.localDevice = localDevice;
        this.updateInterval = updateInterval;
    }

    public void update(int updateTime, int secondsBeforeLongPause, int pauseDuration) throws ExecutionException,
            InterruptedException {
        float value = 0.3f;
        int value1 = 0;
        long StartTime = System.currentTimeMillis() / 1000;
        System.out.println("\nStart updating value every " + updateTime / 1000 + " seconds for the next " +
                secondsBeforeLongPause + " seconds");

        newPointUpdate("device_run_command", "Present_Value");
        newPointUpdate("chiller_water_valve_percentage_command", "Present_Value");

        while (true) {
            if(value > 100f) { value = 0f; }
            value += 1.2f;
            value1 = value1 == 1 ? 0 : 1;
            String valueStr = String.valueOf(value);
            String valueStr1 = String.valueOf(value1);

            for (Map.Entry<BACnetPoint, String> bacPoint : bacnetPoints.entrySet()) {
                BACnetPoint point = bacPoint.getKey();
                String propertyIdentifierName = bacPoint.getValue();
                if(point.getBacnetObject().getId().toString().toLowerCase().contains("analog")) {
                    point.updatePropertyValue(propertyIdentifierName, valueStr);
                } else {
                    point.updatePropertyValue(propertyIdentifierName, valueStr1);
                }
            }
            if(paused) {StartTime = System.currentTimeMillis() / 1000; paused = false;}
            pause(StartTime, updateTime, secondsBeforeLongPause, pauseDuration);
        }
    }

    private void newPointUpdate(String pointName, String pointProperty) {
        BACnetPoint point = new BACnetPoint(pointName, localDevice);
        bacnetPoints.put(point, pointProperty);
    }

    private void pause( long startTime, int updateTime, int secondsBeforeLongPause, int longPauseDuration)
            throws InterruptedException {
        long currentTime = System.currentTimeMillis() / 1000;
        if ( currentTime - startTime > secondsBeforeLongPause) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            System.out.println("\nPausing for " + longPauseDuration / 1000 + " seconds... \nSending COV Notification every "
                    + this.updateInterval / 1000 + " seconds \nCurrent Time: " + formatter.format(date) + "\n");
            paused = true;
            newNotificationIntervalThread();
            Thread.sleep(longPauseDuration);
            System.out.println("\nStart updating value every " + updateTime / 1000 + " seconds for the next " +
                    secondsBeforeLongPause + " seconds");
        } else {
            Thread.sleep(updateTime);
        }
    }

    private Thread newNotificationIntervalThread() {
        Runnable runnable =
                () -> {
                    try {
                        sendIntervalNotification();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BACnetServiceException e) {
                        e.printStackTrace();
                    }
                };
        Thread notificationThread = new Thread(runnable);
        notificationThread.start();
        return notificationThread;
    }

    private void sendIntervalNotification() throws InterruptedException, BACnetServiceException {
        while(true) {
            Thread.sleep(this.updateInterval);
            if (paused) {
                for (Map.Entry<BACnetPoint, String> bacPoint : bacnetPoints.entrySet()) {
                    BACnetPoint bacnetPoint = bacPoint.getKey();
                    String propertyIdentifierName = bacPoint.getValue();
                    PropertyIdentifier propertyIdentifier = bacnetPoint.getPointMap(propertyIdentifierName);
                    Encodable encodable = bacnetPoint.getBacnetObject().getProperty(propertyIdentifier);
                    if(bacnetPoint.getBacnetObject().getId().toString().toLowerCase().contains("analog input")) {
                        BACnetObject bacnetObject = bacnetPoint.getBacnetObject();
                        covSubscriptions = bacnetObject.covSubscriptions;
                        sendNotificationImpl(bacnetPoint);
                    }
                }
            }
        }
    }

    private void sendNotificationImpl(BACnetPoint bacnetPoint) {
        long now = System.currentTimeMillis();
        for(int i = this.covSubscriptions.size() - 1; i >= 0; --i) {
            ObjectCovSubscription sub = (ObjectCovSubscription)this.covSubscriptions.get(i);
            if (sub.hasExpired(now)) {
                System.out.println("sub expired..");
                this.covSubscriptions.remove(i);
            } else {
                this.sendCovNotification(bacnetPoint, sub, now);
            }
        }
    }

    private void sendCovNotification(BACnetPoint bacnetPoint, ObjectCovSubscription sub, long now) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        BACnetObject bacnetObject = bacnetPoint.getBacnetObject();
        try {
            UnsignedInteger timeLeft = new UnsignedInteger(sub.getTimeRemaining(now));
            SequenceOf<PropertyValue> values = new SequenceOf(ObjectCovSubscription.getValues(bacnetObject));
            if (sub.isIssueConfirmedNotifications()) {
                ConfirmedCovNotificationRequest req = new ConfirmedCovNotificationRequest(
                        sub.getSubscriberProcessIdentifier(),
                        this.localDevice.getConfiguration().getId(),
                        bacnetObject.getId(), timeLeft, values);
                RemoteDevice d = this.localDevice.getRemoteDevice(sub.getAddress());
                System.out.println("Sending confirmed notification ..." + formatter.format(date));
                this.localDevice.send(d, req);
            } else {
                UnconfirmedCovNotificationRequest req = new UnconfirmedCovNotificationRequest(
                        sub.getSubscriberProcessIdentifier(),
                        this.localDevice.getConfiguration().getId(),
                        bacnetObject.getId(), timeLeft, values);
                System.out.println("Sending unconfirmed notification ..." + formatter.format(date));
                this.localDevice.sendUnconfirmed(sub.getAddress(), sub.getLinkService(), req);
            }
        } catch (BACnetException var8) {
            ExceptionDispatch.fireReceivedException(var8);
        }
    }
}
