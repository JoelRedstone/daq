package FauxDeviceEngine;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class BACnetPoint {

    BACnetObject bacnetObject;
    LocalDevice localDevice;


    public BACnetPoint(String name, LocalDevice localDevice) {
        this.localDevice = localDevice;
        this.bacnetObject = localDevice.getObject(name);
    }

    public void updatePropertyValue(String propertyIdentifierName, String value) throws ExecutionException, InterruptedException {
        Map<PropertyIdentifier, Encodable> property = null;
        if(bacnetObject.getId().toString().toLowerCase().contains("analog")) {
            property = new Analog().processObjectProperty(propertyIdentifierName, value);
        } else if (bacnetObject.getId().toString().toLowerCase().contains("binary")) {
            property = new Binary().processObjectProperty(propertyIdentifierName, value);
        } else { return; }
        for (Map.Entry<PropertyIdentifier, Encodable> p : property.entrySet()) {
            PropertyIdentifier propertyIdentifier = p.getKey();
            Encodable encodable = p.getValue();
            Future<Integer> future = new PointUpdater(localDevice).update(bacnetObject, propertyIdentifier, encodable);
            int exitCode = future.get();
            System.out.println(bacnetObject.getId().toString() + " // " +propertyIdentifier.toString() + ": " + encodable.toString() + " exit code: " + exitCode);
        }
    }

    public PropertyIdentifier getPointMap(String propertyIdentifierName) {
        Map<PropertyIdentifier, Encodable> property = null;
        PropertyIdentifier propertyIdentifier = null;
        if(bacnetObject.getId().toString().toLowerCase().contains("analog")) {
            property = new Analog().processObjectProperty(propertyIdentifierName, null);
        } else if (bacnetObject.getId().toString().toLowerCase().contains("binary")) {
            property = new Binary().processObjectProperty(propertyIdentifierName, null);
        } else { return null; }
        for (Map.Entry<PropertyIdentifier, Encodable> p : property.entrySet()) {
            propertyIdentifier = p.getKey();
        }
        return propertyIdentifier;
    }

    public BACnetObject getBacnetObject() {
        return bacnetObject;
    }
}
