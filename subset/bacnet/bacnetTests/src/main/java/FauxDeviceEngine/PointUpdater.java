package FauxDeviceEngine;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.enumerated.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PointUpdater {

    private LocalDevice localDevice;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private int successExiteCode = 0;
    private int failedExiteCode = 1;

    public PointUpdater(LocalDevice localDevice) {
        this.localDevice = localDevice;
    }

    public Future<Integer> update(BACnetObject bacnetObject, PropertyIdentifier propertyIdentifier, Encodable encodable) {
        return executor.submit(() -> setProperty(bacnetObject, propertyIdentifier, encodable));
    }

    private int setProperty(BACnetObject bacnetObject, PropertyIdentifier propertyIdentifier, Encodable encodable) {
        try {
            bacnetObject.setProperty(propertyIdentifier, encodable);
            return successExiteCode;
        } catch (BACnetServiceException e) {
            System.out.println(e.getMessage());
            return failedExiteCode;
        }
    }
}