package com.glocks.log;

import java.io.FileWriter;

public class LogWriter {

    public void writeEvents(FileWriter pw, String servedIMEI, String recordType, // Write what is in CDR file (COMMENTTED)   && also writes What rules aplied on CDR File Records
            String servedIMSI, String servedMSISDN, String systemType, String operator, String file_name,
            String record_time, String type, String rule_id, String rule_name, String status, String time) {
        try {
            pw.write(servedIMEI + "," + recordType + "," + servedIMSI + "," + servedMSISDN + "," + systemType + "," + operator + ","
                    + file_name + "," + record_time + "," + type + "," + rule_id + "," + rule_name + "," + status + "," + time + String.format("%n"));
            pw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeFeatureEvents(FileWriter pw, String IMEIESNMEID, String DeviceType, // Writes Rules appled on NONCDR records    &&    writes what records are processed on  NON-CDR P2  (Commented)
            String MultipleSIMStatus, String SNofDevice, String Devicelaunchdate, String DeviceStatus, String txn_id,
            String operator, String file_name, String type, String rule_id, String rule_name, String status, String time) {
        try {
            pw.write(IMEIESNMEID + "," + DeviceType + "," + MultipleSIMStatus + "," + SNofDevice + "," + Devicelaunchdate + "," + DeviceStatus + ","
                    + txn_id + "," + operator + "," + file_name + "," + type + "," + rule_id + "," + rule_name + "," + status + "," + time + String.format("%n"));

            pw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
