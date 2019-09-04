import FauxDeviceEngine.JSON;
import com.google.common.collect.Multimap;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import helper.*;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class TuningTest {
    private LocalDevice localDevice;
    private Connection connection;
    private BacnetValidation validator;
    private BacnetPoints bacnetPoints = new BacnetPoints();
    private String localIp = "";
    private String broadcastIp = "";
    private boolean verboseOutput = true;
    private boolean bacnetSupported = false;
    private boolean testPassed = true;
    private String reportAppendix = "";
    private String jsonFilePath = "bacnet_tuning.json";
    private String testName = "protocol.bacnet.tuning";
    private String testDescription = "Check if bacnet device is compliant with tuning policy provided.";
    private String passedTestReport = String.format("RESULT pass %s", testName);
    private String failedTestReport = String.format("RESULT fail %s", testName);
    private String skippedTestReport = String.format("RESULT skip %s", testName);
    private String formatProperty = "%-25s%-20s%-30s%-1s";
    private int sizeProperty = 35;
    private int failedPropertyCounter = 0;

    private boolean debug = true;

    String tuningObjectType;
    Map<String, String> tuningPointsMap;
    Multimap<String, Map<String, String>> devicePointsMap;
    ArrayList<String> deviceObjectTypeList;

    public TuningTest(String localIp, String broadcastIp, boolean verboseOutput) throws Exception {
        this.localIp = localIp;
        this.broadcastIp = broadcastIp;
        this.verboseOutput = verboseOutput;
        discoverDevices();
    }

    private void discoverDevices() throws Exception {
        connection = new Connection(broadcastIp, IpNetwork.DEFAULT_PORT, localIp);
        while (!connection.isTerminate()) {
            localDevice = connection.getLocalDevice();
            System.out.println("Sending whois...");
            localDevice.sendGlobalBroadcast(new WhoIsRequest());
            System.out.println("Waiting...");
            Thread.sleep(5000);
            System.out.println("Processing...");
            validator = new BacnetValidation(localDevice);
            bacnetSupported = validator.checkIfBacnetSupported();
            if (bacnetSupported) {
                JSONArray bacnetTuning = readTuningJSON();
                bacnetPoints.get(localDevice);
                iterateAndPerformTuningChecks(bacnetTuning);
                generateReport();
            } else {
                reportAppendix += "Bacnet device not found... Tuning Policy test cannot be performed.\n";
                generateReport();
            }
            connection.doTerminate();
        }
    }

    private JSONArray readTuningJSON() {
        JSONArray bacnetTuning = null;
        try{
            FileManager fileManager = new FileManager();
            String auxFolder = fileManager.getAuxFolderPath();
            jsonFilePath = auxFolder + "/" + jsonFilePath;
            JSON json = null;
            if(debug) {
                json = new JSON("src/main/resources/bacnet_tuning.json"); // for debug purpose only
            } else { json = new JSON(jsonFilePath); }
            bacnetTuning = json.read();
        } catch (Exception e) {
            System.out.println("Error reading JSON file: " + jsonFilePath +
                    "\n" + e.getMessage());
        }
        return bacnetTuning;
    }

    private void iterateAndPerformTuningChecks(JSONArray bacnetTuning) {
        try {
            reportAppendix += String.format(formatProperty, "Object Type", "Property Type",
                    StringUtils.center("Device - Tuning", sizeProperty), "Result" +"\n");
            bacnetTuning.forEach(bacnetObject -> {
                performTuningTest((JSONObject) bacnetObject);
            });
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void performTuningTest(JSONObject bacnetTuning) {
        setVariables(bacnetTuning);
        for (String deviceObjectType : deviceObjectTypeList) {
            if(deviceObjectType.contains(tuningObjectType.replace("_", " "))) {
                reportAppendix += "\n";
                compareKeyValuePair(devicePointsMap, deviceObjectType, bacnetTuning, tuningObjectType);
            }
        }
    }

    private void setVariables(JSONObject bacnetTuning) {
        List<String> bacnetObjectTypeArr = new ArrayList<>(bacnetTuning.keySet());
        tuningObjectType = bacnetObjectTypeArr.get(0); // Tuning Object Type
        tuningPointsMap = (Map<String, String>) bacnetTuning.get(tuningObjectType); // Tuning object
        devicePointsMap = bacnetPoints.getBacnetPointsMap(); // device points objects
        Set<String> keySet = devicePointsMap.keySet();
        deviceObjectTypeList = new ArrayList<>(keySet);
    }

    private void compareKeyValuePair(Multimap<String, Map<String, String>> devicePointsMap,
                                     String deviceObjectType, JSONObject bacnetTuning, String bacnetObjectType) {
        Collection<Map<String, String>> deviceObjectTypeCollection = devicePointsMap.get(deviceObjectType);
        Map<String, String> tuningObjectTypeMap = (Map<String, String>) bacnetTuning.get(bacnetObjectType);
        for (Map.Entry<String, String> tuningObject : tuningObjectTypeMap.entrySet()) {
            String tuningPropertyKey = tuningObject.getKey();
            Object tuningPropertyValue = tuningObject.getValue();
            if (!tuningPropertyKey.equals("range")) {
                compareFixedValues(deviceObjectTypeCollection, tuningPropertyKey, tuningPropertyValue.toString(),
                        deviceObjectType);
            } else {
                compareRangeValues(tuningPropertyValue, deviceObjectTypeCollection, deviceObjectType);
            }
        }
    }

    private void compareFixedValues(Collection<Map<String,String>> deviceObjectTypeCollection, String tuningPropertyKey,
                                    String tuningPropertyValue, String deviceObjectType) {
        ArrayList<String> matchDeviceProperty = getMatchPropertyKeyValuePair(deviceObjectTypeCollection, tuningPropertyKey);
        if (matchDeviceProperty.size() == 0) {
            appendToReport(deviceObjectType, tuningPropertyKey, "NOT FOUND", tuningPropertyValue, "FAILED");
            failedPropertyCounter ++;
            return;
        }
        String devicePropertyKey = matchDeviceProperty.get(0);
        String devicePropertyValue = matchDeviceProperty.get(1);
        boolean comparisonPassed = compareResult(devicePropertyValue, tuningPropertyValue);
        if (comparisonPassed) {
            if (verboseOutput) {
                appendToReport(deviceObjectType, devicePropertyKey, devicePropertyValue, tuningPropertyValue, "PASSED");
            }
        } else {
            appendToReport(deviceObjectType, devicePropertyKey, devicePropertyValue, tuningPropertyValue, "FAILED");
            failedPropertyCounter ++;
        }
    }

    private void compareRangeValues(Object tuningPropertyValue, Collection<Map<String, String>> deviceObjectTypeCollection,
                                    String deviceObjectType) {
        Collection<Map<String,String>> tuningPropertyCollection = (Collection<Map<String, String>>) tuningPropertyValue;
        for (Map<String,String> tuningPropertyMap : tuningPropertyCollection) {
            try {
                Set<String> rangeKeySet = tuningPropertyMap.keySet();
                ArrayList<String> rangeKeyArr = new ArrayList<>(rangeKeySet);
                String rangePropertyKey1 = rangeKeyArr.get(0);
                String rangePropertyValue1 = tuningPropertyMap.get(rangePropertyKey1);
                String rangePropertyKey2 = rangeKeyArr.get(1);
                String rangePropertyValue2 = tuningPropertyMap.get(rangePropertyKey2);
                ArrayList<String> matchDeviceProperty1 = getMatchPropertyKeyValuePair(deviceObjectTypeCollection, rangePropertyKey1);
                ArrayList<String> matchDeviceProperty2 = getMatchPropertyKeyValuePair(deviceObjectTypeCollection, rangePropertyKey2);
                if (matchDeviceProperty1.size() == 0) {
                    appendToReport(deviceObjectType, rangePropertyKey1, "NOT FOUND", rangePropertyValue1, "FAILED");
                    failedPropertyCounter ++;
                }
                if (matchDeviceProperty2.size() == 0) {
                    appendToReport(deviceObjectType, rangePropertyKey2, "NOT FOUND", rangePropertyValue2, "FAILED");
                    failedPropertyCounter ++;
                    continue;
                }
                String devicePropertyKey1 = matchDeviceProperty1.get(0);
                String devicePropertyValue1 = matchDeviceProperty1.get(1);
                String devicePropertyKey2 = matchDeviceProperty2.get(0);
                String devicePropertyValue2 = matchDeviceProperty2.get(1);
                checkTuning(deviceObjectType, devicePropertyKey1, devicePropertyValue1, devicePropertyKey2,
                        devicePropertyValue2, rangePropertyValue1, rangePropertyValue2);
            } catch (Exception e) {
                displayErrorMessage();
                failedTest();
            }
        }
    }

    private void checkTuning(String deviceObjectType, String deviceBACnetProperty_1, String deviceBACnetPropertyValue_1,
                             String deviceBACnetProperty_2, String deviceBACnetPropertyValue_2,
                             String tuningPropertyValue_1, String tuningPropertyValue_2) {
        if (!isMinValue(tuningPropertyValue_1, tuningPropertyValue_2)) {
            compareHighRange(tuningPropertyValue_1, deviceBACnetPropertyValue_1, deviceObjectType, deviceBACnetProperty_1);
            compareLowRange(tuningPropertyValue_2, deviceBACnetPropertyValue_2, deviceObjectType, deviceBACnetProperty_2);
        } else if (isMinValue(tuningPropertyValue_1, tuningPropertyValue_2)) {
            compareLowRange(tuningPropertyValue_1, deviceBACnetPropertyValue_1, deviceObjectType, deviceBACnetProperty_1);
            compareHighRange(tuningPropertyValue_2, deviceBACnetPropertyValue_2, deviceObjectType, deviceBACnetProperty_2);
        } else {
            displayErrorMessage();
        }
    }

    private void displayErrorMessage() {
        System.err.println("\n PROBLEM ENCOUNTERED... PLEASE CHECK THE JSON FILE YOU PROVIDED IS IN THE RIGHT FORMAT. \n");
    }

    private boolean compareLowRange(String tuningValue, String deviceValue, String deviceObjectType, String deviceBACnetProperty) {
        if (Float.parseFloat(deviceValue) >= Float.parseFloat(tuningValue)) {
            if(verboseOutput) {
                appendToReport(deviceObjectType, deviceBACnetProperty, deviceValue, tuningValue, "PASSED", " <= ");
            }
            return true;
        }
        appendToReport(deviceObjectType, deviceBACnetProperty, deviceValue, tuningValue, "FAILED", " >= ");
        failedPropertyCounter ++;
        failedTest();
        return false;
    }

    private boolean compareHighRange(String tuningValue, String deviceValue, String deviceObjectType, String deviceBACnetProperty) {
        if (Float.parseFloat(deviceValue) <= Float.parseFloat(tuningValue)) {
            if (verboseOutput) {
                appendToReport(deviceObjectType, deviceBACnetProperty, deviceValue, tuningValue, "PASSED", " <= ");
            }
            return true;
        }
        appendToReport(deviceObjectType, deviceBACnetProperty, deviceValue, tuningValue, "FAILED", " <= ");
        failedPropertyCounter ++;
        failedTest();
        return false;
    }

    private boolean isMinValue(String value1, String value2) {
        float v1 = Float.parseFloat(value1);
        float v2 = Float.parseFloat(value2);
        if(v1 < v2) { return true; };
        return false;
    }

    private void appendToReport(String deviceObjectType, String devicePropertyType, String devicePropertyValue,
                                String tunningValue, String result, String operation) {
        if(devicePropertyValue.isEmpty()){ devicePropertyValue = "NOT SET"; }
        reportAppendix += String.format(formatProperty, deviceObjectType, devicePropertyType,
                StringUtils.center(devicePropertyValue + operation + tunningValue, sizeProperty), result+"\n");
    }

    private void appendToReport(String devicePropertyType, String deviceProperty, String devicePropertyValue,
                                String propertyValue, String result) {
        if(propertyValue.isEmpty()){ propertyValue = "NOT SET"; }
        reportAppendix += String.format(formatProperty, devicePropertyType, deviceProperty,
                StringUtils.center(devicePropertyValue + " - " + propertyValue, sizeProperty), result+"\n");
        failedTest();
    }

    private ArrayList<String> getMatchPropertyKeyValuePair(Collection<Map<String,String>> deviceObjectTypeCollection, String tuningPropertyKey) {
        ArrayList<String> devicePropertyKeyValuePair = new ArrayList<>();
        for (Map<String,String> deviceObjectTypeMap : deviceObjectTypeCollection) {
            String devicePropertyKey = getkeyMap(deviceObjectTypeMap);
            String devicePropertyValue = deviceObjectTypeMap.get(devicePropertyKey);
            if (devicePropertyKey.toLowerCase().contains(tuningPropertyKey.replace("_", " ").toLowerCase())) {
                devicePropertyKeyValuePair.add(devicePropertyKey);
                devicePropertyKeyValuePair.add(devicePropertyValue);
            }
        }
        return devicePropertyKeyValuePair;
    }

    private boolean compareResult(String devicePropertyValue, String tuningPropertyValue) {
        if (devicePropertyValue.equals(tuningPropertyValue)) {
            return true;
        }
        return false;
    }

    private String getkeyMap(Map<?, ?> map) {
        Set<String> pendingProperty = (Set<String>) map.keySet();
        ArrayList<String> propertyKeySet = new ArrayList<>(pendingProperty);
        String key = propertyKeySet.get(0);
        return key;
    }

    private void failedTest() {
        testPassed = false;
    }

    private void generateReport() {
        Report report = new Report("tmp/BacnetTuningTestReport.txt");
        if (bacnetSupported && testPassed) {
            String formattedReport = "";
            if (verboseOutput) {
                formattedReport = buildReport(passedTestReport, reportAppendix, " Device is compliant to Tuning Policy provided.\n");
            } else {
                formattedReport = buildReport(passedTestReport, "N/A",  " Device is compliant to Tuning Policy provided.\n");
            }
            report.writeReport(formattedReport);
        } else if (bacnetSupported && !testPassed) {
            String desc = failedTestReport + " bacnet property found not compliant";
            String formattedReport = buildReport(failedTestReport, reportAppendix, desc);
            report.writeReport(formattedReport);
        } else {
            report.writeReport(skippedTestReport + " No BACnet device found\n");
        }
        System.out.println(reportAppendix);
    }

    private String buildReport(String testResult, String appendix, String reportDescription) {
        String textReport = "";
        textReport += getDashes();
        textReport += testName + "\n";
        textReport += getDashes();
        textReport += testDescription + "\n";
        textReport += getDashes();
        textReport += appendix + "\n";
        textReport += getDashes();
        textReport += testResult + " " + reportDescription;
        return textReport;
    }

    private String getDashes() {
        return "--------------------\n";
    }
}
