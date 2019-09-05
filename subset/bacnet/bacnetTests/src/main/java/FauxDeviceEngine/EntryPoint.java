package FauxDeviceEngine;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.RemoteObject;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.service.confirmed.ReinitializeDeviceRequest;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.*;
import com.serotonin.bacnet4j.type.enumerated.*;
import com.serotonin.bacnet4j.type.notificationParameters.NotificationParameters;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import helper.FileManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class EntryPoint {

    private static int deviceId = 0;
    private static IpNetwork network;
    private static LocalDevice localDevice;
    private static String fauxDeviceJSONFilename = "";
    private static int timeout = 1000;
    private static int updateTime = 1500;
    public static Analog analog;

    public static void main(String[] args) {
        if (args.length != 3) {
            throw new RuntimeException("Usage: localIpAddr broadcastIpAddr fauxDeviceJSONFilename");
        }
        String localIpAddr = args[0];
        String broadcastIpAddr = args[1];
        fauxDeviceJSONFilename = args[2];

        int port = IpNetwork.DEFAULT_PORT;
        network = new IpNetwork(broadcastIpAddr, port,
                IpNetwork.DEFAULT_BIND_IP, 0, localIpAddr);
        Transport transport = new Transport(network);
        transport.setTimeout(timeout);

        try {
            JSONArray bacnetObjectArray = readJSONFile();
            getDeviceID(bacnetObjectArray);
            if(deviceId == 0) {
                System.out.println("Device ID not found in JSON file. Generating random ID...");
                deviceId = (int) Math.floor(Math.random() * 1000.0);
            }
            System.out.println("Creating LoopDevice id " + deviceId);
            localDevice = new LocalDevice(deviceId, transport);
            localDevice.getConfiguration().setProperty(PropertyIdentifier.modelName,
                    new CharacterString("BACnet4J LoopDevice"));
            localDevice.getEventHandler().addListener(new Listener());
            System.out.println("Local device is running with device id " + deviceId);
            addBacnetProperties(bacnetObjectArray);
            localDevice.initialize();
            // Send an iam.
            localDevice.sendGlobalBroadcast(localDevice.getIAm());
            System.out.println("Device initialized...");


            startUpdatingPoints();
//            BACnetPoint bacnetPoint = new BACnetPoint("device_run_command",localDevice);
//            bacnetPoint.updatePropertyValue("Present_Value", "0.9");
//            bacnetPoint.updatePropertyValue("Out_Of_Service", "true");
        } catch (RuntimeException e) {
            System.out.println("Ex in LoopDevice() ");
            e.printStackTrace();
            localDevice.terminate();
            localDevice = null;
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONArray readJSONFile() {
        String jsonFile = fauxDeviceJSONFilename;
        FileManager fileManager = new FileManager();
        String absolute_path = fileManager.getAbsolutePath();
        JSON json = new JSON(absolute_path + "tmp/" + jsonFile);
        JSONArray bacnetObjectTypesList = json.read();
        return bacnetObjectTypesList;
    }

    private static void addBacnetProperties(JSONArray bacnetObjectsList) {
        bacnetObjectsList.forEach(bacnetObject -> addProperty((JSONObject) bacnetObject));
    }

    private static void getDeviceID(JSONArray bacnetObjectsList) {
        bacnetObjectsList.forEach(bacnetObject -> getID((JSONObject) bacnetObject));
    }

    private static void getID(JSONObject bacnetObject) {
        List<String> bacnetObjectTypeArr = new ArrayList<>(bacnetObject.keySet());
        String bacnetObjectType = bacnetObjectTypeArr.get(0);
        if(bacnetObjectType.contains("DeviceID")) {
            String IDString = (String) bacnetObject.get(bacnetObjectType);
            int DeviceID = Integer.parseInt(IDString);
            System.out.println("Device ID found in JSON file.");
            deviceId = DeviceID;
        }
    }

    private static void addProperty(JSONObject bacnetObject) {
        try {
            List<String> bacnetObjectTypeArr = new ArrayList<>(bacnetObject.keySet());
            String bacnetObjectType = bacnetObjectTypeArr.get(0);
            ObjectType objectTypeValue = null;
            if(bacnetObjectType.contains("AnalogInput")){
                objectTypeValue = ObjectType.analogInput;
            } else if(bacnetObjectType.contains("AnalogOutput")) {
                objectTypeValue = ObjectType.analogOutput;
            } else if(bacnetObjectType.contains("AnalogValue")) {
                objectTypeValue = ObjectType.analogValue;
            } else if(bacnetObjectType.contains("BinaryInput")) {
                objectTypeValue = ObjectType.binaryInput;
            } else if(bacnetObjectType.contains("BinaryOutput")) {
                objectTypeValue = ObjectType.binaryOutput;
            } else if(bacnetObjectType.contains("BinaryValue")) {
                objectTypeValue = ObjectType.binaryValue;
            } else { return; }
            BACnetObject bacnetType = new BACnetObject(localDevice,
                    localDevice.getNextInstanceObjectIdentifier(objectTypeValue), false);
            Map<String, String > map = (Map<String, String>) bacnetObject.get(bacnetObjectType);
            int objectTypeIntValue = objectTypeValue.intValue();
            if(objectTypeIntValue >= 0 && objectTypeIntValue < 3) {
                analog = new Analog(localDevice, bacnetType, map);
            }else if(objectTypeIntValue >= 3 && objectTypeIntValue < 6) {
                new Binary(localDevice, bacnetType, map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private static void startUpdatingPoints() {
        Runnable updaterRunnable =
                () -> {
                    try {
                        update();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                };
        Thread updaterThread = new Thread(updaterRunnable);
        updaterThread.start();
    }

    private static void update() throws ExecutionException, InterruptedException {
        float value = 0.3f;
        int value1 = 0;
        while (true) {
            if(value > 100f) { value = 0f; }
            value += 0.3f;
            value1 = value1 == 1 ? 0 : 1;
            String valueStr = String.valueOf(value);
            String valueStr1 = String.valueOf(value1);
            BACnetPoint bacnetPoint = new BACnetPoint("device_run_command",localDevice);
            bacnetPoint.updatePropertyValue("Present_Value", valueStr);
            BACnetPoint bacnetPoint1 = new BACnetPoint("chiller_water_valve_percentage_command",localDevice);
            bacnetPoint1.updatePropertyValue("Present_Value", valueStr1);
            Thread.sleep(updateTime);
        }
    }
}

class Listener implements DeviceEventListener {

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
    public void covNotificationReceived(UnsignedInteger unsignedInteger, RemoteDevice remoteDevice, ObjectIdentifier objectIdentifier, UnsignedInteger unsignedInteger1, SequenceOf<PropertyValue> sequenceOf) {
        System.out.println("1");
    }

    @Override
    public void eventNotificationReceived(UnsignedInteger unsignedInteger, RemoteDevice remoteDevice, ObjectIdentifier objectIdentifier, TimeStamp timeStamp, UnsignedInteger unsignedInteger1, UnsignedInteger unsignedInteger2, EventType eventType, CharacterString characterString, NotifyType notifyType, Boolean aBoolean, EventState eventState, EventState eventState1, NotificationParameters notificationParameters) {
        System.out.println("2");
    }

    @Override
    public void textMessageReceived(RemoteDevice remoteDevice, Choice choice, MessagePriority messagePriority, CharacterString characterString) {

    }

    @Override
    public void privateTransferReceived(UnsignedInteger unsignedInteger, UnsignedInteger unsignedInteger1, Encodable encodable) {

    }

    @Override
    public void reinitializeDevice(ReinitializeDeviceRequest.ReinitializedStateOfDevice reinitializedStateOfDevice) {

    }

    @Override
    public void synchronizeTime(DateTime dateTime, boolean b) {

    }
}
