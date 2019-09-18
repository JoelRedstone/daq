import FauxDeviceEngine.JSON;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.service.confirmed.SubscribeCOVRequest;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import helper.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class ComplianceTest {

    private Connection connection;
    private BacnetValidation validator;
    private String testName = "protocol.bacnet.compliance";
    private String passedTestReport = String.format("RESULT pass %s\n", testName);
    private String failedTestReport = String.format("RESULT fail %s\n", testName);
    private String skippedTestReport = String.format("RESULT skip %s", testName);
    private String reportAppendix = "";
    private String localIp = "";
    private String broadcastIp = "";
    boolean bacnetSupported = false;
    boolean csvFound = true;
    boolean verboseOutput = false;
    private static LocalDevice localDevice;
    private String jsonFilePath = "bacnet_compliance.json";
    private int monitorTime;
    private List<ObjectIdentifier> objectIdentifierList = new ArrayList<>();

    private CovNotificationAnalizer covNotificationAnalizer;

    private boolean debug = true;

    public ComplianceTest(String localIp, String broadcastIp, boolean verboseOutput) throws Exception {
        this.localIp = localIp;
        this.broadcastIp = broadcastIp;
        this.verboseOutput = verboseOutput;
        discoverDevices();
    }

    private void discoverDevices() throws Exception {
        JSONArray bacnetCompliance = readComplianceJSON();
        getMonitoringtime(bacnetCompliance);
        JSONObject jsonObject = (JSONObject) bacnetCompliance.get(1);
        covNotificationAnalizer = new CovNotificationAnalizer(jsonObject);

        connection = new Connection(broadcastIp, IpNetwork.DEFAULT_PORT, localIp, covNotificationAnalizer);
        while (!connection.isTerminate()) {
            localDevice = connection.getLocalDevice();
            System.err.println("Sending whois...");
            localDevice.sendGlobalBroadcast(new WhoIsRequest());
            System.err.println("Waiting...");
            Thread.sleep(5000);
            System.err.println("Processing...");
            validator = new BacnetValidation(localDevice);
            bacnetSupported = validator.checkIfBacnetSupported();
            if (bacnetSupported) {
                addPointsToBeMonitoredToList(bacnetCompliance);
                subscribeToPoints();
                generateReport();
            } else {
                reportAppendix += " Bacnet device not found... Compliance test cannot be performed.\n";
                System.out.println(reportAppendix);
                generateReport();
            }
            connection.doTerminate();
        }
    }

    private void getMonitoringtime(JSONArray bacnetCompliance) {
        try {
            JSONObject jsonObject = (JSONObject) bacnetCompliance.get(0);
            String time_str = (String) jsonObject.get("monitoring_time");
            int time = Integer.parseInt(time_str);
            this.monitorTime = time * 60000;
        } catch (Exception e) {
            System.out.println("Monitoring time has not been specified.");
        }
    }

    private JSONArray readComplianceJSON() {
        JSONArray bacnetCompliance = null;
        try {
            FileManager fileManager = new FileManager();
            String auxFolder = fileManager.getAuxFolderPath();
            jsonFilePath = auxFolder + "/" + jsonFilePath;
            JSON json = null;
            if(debug) {
                json = new JSON("src/main/resources/bacnet_compliance.json"); // for debug purpose only
            } else { json = new JSON(jsonFilePath); }
            bacnetCompliance = json.read();
        } catch (Exception e) {
            System.err.println("Error reading JSON file: " + jsonFilePath +
                    "\n" + e.getMessage());
        }
        return bacnetCompliance;
    }

    private void addPointsToBeMonitoredToList(JSONArray bacnetCompliance) {
        try {
            JSONObject jsonObject = (JSONObject) bacnetCompliance.get(1);
            for (Object name : jsonObject.keySet()) {
                String bacnetObjectName = name.toString();
                String[] splittedName = name.toString().split("_");
                int instanceNumber = Integer.parseInt(splittedName[splittedName.length - 1]);
                ObjectIdentifier objectIdentifier = getObjectIdentifier(bacnetObjectName, instanceNumber);
                objectIdentifierList.add(objectIdentifier);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private ObjectIdentifier getObjectIdentifier(String oid_str, int instanceNumber) {
        ObjectIdentifier objectIdentifier = null;
        oid_str = oid_str.replace("_", "");
        if(oid_str.toLowerCase().contains("analoginput")) {
            objectIdentifier = new ObjectIdentifier(ObjectType.analogInput, instanceNumber);
        } else if (oid_str.toLowerCase().contains("analogoutput")) {
            objectIdentifier = new ObjectIdentifier(ObjectType.analogOutput, instanceNumber);
        } else if (oid_str.toLowerCase().contains("analogvalue")) {
            objectIdentifier = new ObjectIdentifier(ObjectType.analogValue, instanceNumber);
        } else if (oid_str.toLowerCase().contains("binaryinput")) {
            objectIdentifier = new ObjectIdentifier(ObjectType.binaryInput, instanceNumber);
        } else if (oid_str.toLowerCase().contains("binaryoutput")) {
            objectIdentifier = new ObjectIdentifier(ObjectType.binaryOutput, instanceNumber);
        } else if (oid_str.toLowerCase().contains("binaryvalue")) {
            objectIdentifier = new ObjectIdentifier(ObjectType.binaryValue, instanceNumber);
        } else if (oid_str.toLowerCase().contains("binaryinput")) {
            objectIdentifier = new ObjectIdentifier(ObjectType.binaryInput, instanceNumber);
        } else if (oid_str.toLowerCase().contains("multistateinput")) {
            objectIdentifier = new ObjectIdentifier(ObjectType.multiStateInput, instanceNumber);
        } else if (oid_str.toLowerCase().contains("multistateoutput")) {
            objectIdentifier = new ObjectIdentifier(ObjectType.multiStateOutput, instanceNumber);
        } else if (oid_str.toLowerCase().contains("multistatevalue")) {
            objectIdentifier = new ObjectIdentifier(ObjectType.multiStateValue, instanceNumber);
        }
        return objectIdentifier;
    }

    private void subscribeToPoints() throws BACnetException, InterruptedException {
        for (RemoteDevice remoteDevice : localDevice.getRemoteDevices()) {
            try {
                for (ObjectIdentifier objectIdentifier : objectIdentifierList) {
                    // Subscribe to object
                    SubscribeCOVRequest req = new SubscribeCOVRequest(new UnsignedInteger(1), objectIdentifier,
                            new com.serotonin.bacnet4j.type.primitive.Boolean(true), new UnsignedInteger(0));
                    localDevice.send(remoteDevice, req);
                }
                Thread.sleep(monitorTime);
            } finally {
                if (remoteDevice != null) {
                    for (ObjectIdentifier objectIdentifier : objectIdentifierList) {
                        // Unsubscribe
                        localDevice.send(remoteDevice, new SubscribeCOVRequest(new UnsignedInteger(1), objectIdentifier,
                                null, null));
                    }
                }
            }
        }
    }

    private void generateReport() {
        Report report = new Report("tmp/BacnetComplianceTestReport.txt");
        Report appendix = new Report("tmp/BacnetComplianceTest_APPENDIX.txt");
        boolean passedTest = covNotificationAnalizer.getTestResult();
        reportAppendix += covNotificationAnalizer.getReport();
        System.out.println("\nReport: \n"+reportAppendix);
        if (this.bacnetSupported && passedTest) {
            report.writeReport(passedTestReport);
        } else if (this.bacnetSupported && !passedTest) {
            report.writeReport(failedTestReport);
        } else {
            report.writeReport(skippedTestReport);
        }
        appendix.writeReport(reportAppendix);
    }
}