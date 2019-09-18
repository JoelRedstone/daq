package FauxDeviceEngine;

import FauxDeviceEngine.helper.Listener;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.enumerated.*;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import helper.FileManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class EntryPoint {

    private static int deviceId = 0;
    private static IpNetwork network;
    private static LocalDevice localDevice;
    private static String fauxDeviceJSONFilename = "";
    private static int timeout = 1000;
    private static int updateTime = 3000;
    public static Analog analog;
    private static Map<String, Object> bacnetObjectMap = new HashMap<>();

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
                bacnetObjectMap.put(objectTypeValue.toString(), analog);
            }else if(objectTypeIntValue >= 3 && objectTypeIntValue < 6) {
                new Binary(localDevice, bacnetType, map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private static void startUpdatingPoints() {
        Analog analog01 = (Analog) bacnetObjectMap.get("Analog Input");
        int updateInterval = analog01.getUpdateInterval();
        if(updateInterval == 0) {
            System.out.println("update interval == 0");
            return;
        }
        PointUpdaterManager pointUpdaterManager = new PointUpdaterManager(localDevice, updateInterval);
        Runnable updaterRunnable =
                () -> {
                    try {
                        pointUpdaterManager.update(updateTime, 30, 30000);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                };
        Thread updaterThread = new Thread(updaterRunnable);
        updaterThread.start();
    }
}
