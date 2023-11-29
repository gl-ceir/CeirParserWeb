package com.glocks.parser;

import com.glocks.constants.PropertyReader;
import com.glocks.dao.SysConfigurationDao;
import com.glocks.dao.WebActionDbDao;
import com.glocks.db.ConnectionConfiguration;
import com.glocks.parser.service.ConsignmentInsertUpdate;
import com.glocks.parser.service.StolenRecoverBlockUnBlockImpl;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class MainController {

    @Autowired
    StolenRecoverBlockUnBlockImpl stolenRecoverBlockUnBlockImpl;

    private static final Logger logger = LogManager.getLogger(MainController.class);
    static StackTraceElement l = new Exception().getStackTrace()[0];
   public  static String appdbName = null;
    static String auddbName = null;
    static String repdbName = null;
    static String serverName = null;
    static String dateFunction = null;
    public static PropertyReader propertyReader = null;
    static ConnectionConfiguration connectionConfiguration = null;
    static Connection conn = null;
    
    
    public void loader(ApplicationContext applicationContext) {
        
        propertyReader = (PropertyReader) applicationContext.getBean("propertyReader");
        connectionConfiguration = (ConnectionConfiguration) applicationContext.getBean("connectionConfiguration");
        logger.info("connectionConfiguration :" + connectionConfiguration.getConnection().toString());
        //  conn = (Connection) new com.glocks.db.MySQLConnection().getConnection();
        conn = connectionConfiguration.getConnection();
        appdbName = propertyReader.appdbName;
        auddbName = propertyReader.auddbName;
        repdbName = propertyReader.repdbName;
        
        

        logger.info(" MainController");
      //  Connection conn = new com.glocks.db.MySQLConnection().getConnection();
        String basePath = "";
        String complete_file_path = "";
        String[] rawDataResult = null;
        String feature = null;

//          new ErrorFileGenrator().apiConnectionErrorFileReader();     // required later
        HashMap<String, String> feature_file_management = new HashMap<String, String>();
        CEIRFeatureFileFunctions ceirfunction = new CEIRFeatureFileFunctions();
        HashMap<String, String> feature_file_mapping = new HashMap<String, String>();
        WebActionDbDao webActionDbDao = new WebActionDbDao();
        SysConfigurationDao sysConfigurationDao = new SysConfigurationDao();
        ResultSet file_details = webActionDbDao.getFileDetails(conn, 0, feature);  //select * from web_action_db limit 1
        try {
            while (file_details.next()) {
                logger.info("Feature :" + file_details.getString("feature") + "; SubFeature :" + file_details.getString("sub_feature") + ";State : " + file_details.getString("state"));
                if (file_details.getString("state").equalsIgnoreCase("2") || file_details.getString("state").equalsIgnoreCase("3")) {
                    CEIRFeatureFileParser.cEIRFeatureFileParser(conn, feature);
                    return;
                }
                webActionDbDao.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 1, file_details.getString("feature"), file_details.getString("sub_feature"));  //update web_action_db set state 1
                if (file_details.getString("feature").equalsIgnoreCase("Register Device")) {
                    if ((file_details.getString("sub_feature").equalsIgnoreCase("Register")) || (file_details.getString("sub_feature").equalsIgnoreCase("Add Device"))) {     //'Add Device'
                        ceirfunction.UpdateStatusViaApi(conn, file_details.getString("txn_id"), 0, file_details.getString("feature"));  // ravi sir api who update status
                        webActionDbDao.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 2, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db
                        break;
                    } else if (file_details.getString("sub_feature").equalsIgnoreCase("Clear")) {
                        new CEIRFeatureFileFunctions().getfromRegulizeEnterInCustom(conn, file_details.getString("txn_id"));
                        webActionDbDao.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db
                        break;
                    } else {
                        webActionDbDao.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 2, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db
                        break;
                    }
                }
                if (file_details.getString("feature").equalsIgnoreCase("Stolen") && (file_details.getString("sub_feature").equalsIgnoreCase("Approve") || file_details.getString("sub_feature").equalsIgnoreCase("Accept"))) {
                    stolenRecoverBlockUnBlockImpl.updateDeviceDetailsByTxnId(conn, file_details.getString("txn_id"), "device_lawful_db");
                    new ConsignmentInsertUpdate().rawTableCleanUp(conn, file_details.getString("feature"), file_details.getString("txn_id"));
                    webActionDbDao.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db
                    logger.debug("Web Action 4 done ");
                    break;
                }
                if (file_details.getString("feature").equalsIgnoreCase("Stolen") && file_details.getString("sub_feature").equalsIgnoreCase("Reject")) {
                    stolenRecoverBlockUnBlockImpl.removeDeviceDetailsByTxnId(conn, file_details.getString("txn_id"), "device_lawful_db");
                    webActionDbDao.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db
                    logger.debug("Web Action 4 done ");
                    break;
                }
                if (file_details.getString("feature").equalsIgnoreCase("Recovery") && (file_details.getString("sub_feature").equalsIgnoreCase("Approve") || file_details.getString("sub_feature").equalsIgnoreCase("Accept"))) {
                    stolenRecoverBlockUnBlockImpl.removeDeviceDetailsByTxnId(conn, file_details.getString("txn_id"), "device_lawful_db");
                    new ConsignmentInsertUpdate().rawTableCleanUp(conn, file_details.getString("feature"), file_details.getString("txn_id"));
                    webActionDbDao.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db
                    logger.debug("Web Action 4 done ");
                    break;

                }
                if (file_details.getString("feature").equalsIgnoreCase("Recovery") && file_details.getString("sub_feature").equalsIgnoreCase("Reject")) {
                    stolenRecoverBlockUnBlockImpl.updateDeviceDetailsByTxnId(conn, file_details.getString("txn_id"), "device_lawful_db");
                    webActionDbDao.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db
                    logger.debug("Web Action 4 done ");
                    break;
                }
                if (file_details.getString("feature").equalsIgnoreCase("Block") && (file_details.getString("sub_feature").equalsIgnoreCase("Approve") || file_details.getString("sub_feature").equalsIgnoreCase("Accept"))) {
                    stolenRecoverBlockUnBlockImpl.updateDeviceDetailsByTxnId(conn, file_details.getString("txn_id"), "device_operator_db");
                    new ConsignmentInsertUpdate().rawTableCleanUp(conn, file_details.getString("feature"), file_details.getString("txn_id"));
                    webActionDbDao.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db
                    logger.debug("Web Action 4 done ");
                    break;
                }
                if (file_details.getString("feature").equalsIgnoreCase("Block") && file_details.getString("sub_feature").equalsIgnoreCase("Reject")) {
                    stolenRecoverBlockUnBlockImpl.removeDeviceDetailsByTxnId(conn, file_details.getString("txn_id"), "device_operator_db");
                    webActionDbDao.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db
                    logger.debug("Web Action 4 done ");
                    break;
                }

                if (file_details.getString("feature").equalsIgnoreCase("Unblock") && (file_details.getString("sub_feature").equalsIgnoreCase("Approve") || file_details.getString("sub_feature").equalsIgnoreCase("Accept"))) {
                    stolenRecoverBlockUnBlockImpl.removeDeviceDetailsByTxnId(conn, file_details.getString("txn_id"), "device_operator_db");
                    new ConsignmentInsertUpdate().rawTableCleanUp(conn, file_details.getString("feature"), file_details.getString("txn_id"));
                    webActionDbDao.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db
                    logger.debug("Web Action 4 done ");
                    break;

                }
                if (file_details.getString("feature").equalsIgnoreCase("Unblock") && file_details.getString("sub_feature").equalsIgnoreCase("Reject")) {
                    stolenRecoverBlockUnBlockImpl.updateDeviceDetailsByTxnId(conn, file_details.getString("txn_id"), "device_operator_db");
                    webActionDbDao.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db
                    logger.debug("Web Action 4 done ");
                    break;

                }

                if (file_details.getString("feature").equalsIgnoreCase("Update Visa")) {
                    ceirfunction.UpdateStatusViaApi(conn, file_details.getString("txn_id"), 0, file_details.getString("feature"));
                    webActionDbDao.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 2, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db
                    break;
                }

                feature_file_mapping = ceirfunction.getFeatureMapping(conn, file_details.getString("feature"), "NOUSER");  //select * from feature_mapping_db
                feature_file_management = ceirfunction.getFeatureFileManagement(conn, feature_file_mapping.get("mgnt_table_db"), file_details.getString("txn_id"));   //select * from " + management_db + "

                long diffTime = 0L;
//                      diffTime = Util.timeDiff(feature_file_management.get("created_on"), feature_file_management.get("modified_on"));
                logger.info("Time Difference .." + diffTime);
                if (((file_details.getString("sub_feature").equalsIgnoreCase("Register") || file_details.getString("sub_feature").equalsIgnoreCase("UPLOAD")) && (diffTime != 0))) {
                    logger.debug("  It is Regsiter/Upload and different time" + feature_file_management.get("created_on") + " ||  " + feature_file_management.get("modified_on") + " OR delete Flaag : " + feature_file_management.get("delete_flag"));
                    webActionDbDao.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db
                    break;
                }
                try {     // check it for null
                    if (file_details.getString("feature").equalsIgnoreCase("CONSIGNMENT") || file_details.getString("feature").equalsIgnoreCase("STOCK")) {
                        if (file_details.getString("sub_feature").equalsIgnoreCase("REJECT") || file_details.getString("sub_feature").equalsIgnoreCase("DELETE")) {
                            webActionDbDao.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 2, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db
                            break;
                        }
                    } else {     // optimise
                        if (feature_file_management.get("delete_flag") == null) {
                            logger.info("Delete_flag null");
                        } else {
                            if (feature_file_management.get("delete_flag").equals("0")) {
                                logger.debug("  Other Than Stock/Consignment , delete Flag : " + feature_file_management.get("delete_flag"));
                                webActionDbDao.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 4, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
                }

                String errFilEpath = sysConfigurationDao.getTagValue(conn, "system_error_filepath");
                String error_file_path = errFilEpath + file_details.getString("txn_id") + "/" + file_details.getString("txn_id") + "_error.csv";
                File errorfile = new File(error_file_path);
                if (errorfile.exists()) {     // in case of update  ,, earlier file is remove
                    logger.debug("File  moving to old Folder ");
                    errorfile = new File(errFilEpath + file_details.getString("txn_id") + "/old/");
                    if (!errorfile.exists()) {
                        errorfile.mkdir();
                    }
                    logger.debug("MKdir Done ");
                    Path temp = Files.move(Paths.get(error_file_path),
                            Paths.get(errFilEpath + file_details.getString("txn_id") + "/old/" + file_details.getString("txn_id") + "_" + java.time.LocalDateTime.now() + "_P1_error.csv"));
                    if (temp != null) {
                        logger.info("File renamed and moved successfully for P1");
                    } else {
                        logger.warn("Failed to move the file");
                    }
                }

                String user_type = ceirfunction.getUserType(conn, feature_file_management.get("user_id"), file_details.getString("feature").toUpperCase(), file_details.getString("txn_id"));      //   usertype_name from users a, usertype b
                logger.debug("user_types *****" + user_type);
                if (!(feature_file_management.get("file_name") == null || feature_file_management.get("file_name").equals(""))) {
                    logger.info("File Found... " + file_details.getString("feature"));
                    basePath = new SysConfigurationDao().getTagValue(conn, "system_upload_filepath");  // filePath
                    if (!basePath.endsWith("/")) {
                        basePath += "/";
                    }
                    logger.info(" file basePath " + basePath);
                    complete_file_path = basePath + file_details.getString("txn_id") + "/" + feature_file_management.get("file_name");
                    logger.info("Complete file name is.... " + complete_file_path);

                    File f = new File(complete_file_path);
                    if (f.exists()) {
                        logger.info(" File Exists ");
                    } else {
                        try {
                            String query = "update web_action_db set state=" + 0 + " ,  retry_count = retry_count +1   where    txn_id='" + file_details.getString("txn_id") + "' and feature='" + file_details.getString("feature")
                                    + "' and sub_feature='" + file_details.getString("sub_feature") + "' ";
                            Statement stmt = conn.createStatement();
                            stmt.executeUpdate(query);
                            logger.info(" [ " + query + "]");
                        } catch (Exception e) {
                            logger.info("errror" + e);
                        }
                        return;
                    }

                    if (file_details.getString("sub_feature").equalsIgnoreCase("delete") || file_details.getString("sub_feature").equalsIgnoreCase("approve") || file_details.getString("sub_feature").equalsIgnoreCase("reject")) {
                        logger.info("sub_feature ...  DELETE   " + file_details.getString("sub_feature"));
                        webActionDbDao.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 2, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db
                        logger.info("WEb action status  2 Done ");
                        break;
                    }
                    if (file_details.getString("sub_feature").equalsIgnoreCase("register") || file_details.getString("sub_feature").equalsIgnoreCase("update") || file_details.getString("sub_feature").equalsIgnoreCase("upload")) {
                        logger.info("sub_feature ...   " + file_details.getString("sub_feature"));
                        if (file_details.getInt("state") == 1) {
                            logger.info(" state == 1  ");
                            getFinalDetailValues(conn, complete_file_path, feature_file_mapping.get("output_device_db"), file_details.getString("txn_id"), file_details.getString("feature"), file_details.getString("sub_feature"));
                            break;
                        } else {
                            ceirfunction.UpdateStatusViaApi(conn, file_details.getString("txn_id"), 0, file_details.getString("feature"));
                            rawDataResult = new CsvFileReader().readConvertedFeatureFile(conn, feature_file_management.get("file_name"), complete_file_path, file_details.getString("feature"), basePath, file_details.getString("txn_id"), file_details.getString("sub_feature"), feature_file_mapping.get("mgnt_table_db"), user_type);
                        }
                    }
                } else {
                    logger.info("No File Found.. ");
                    if (file_details.getString("feature").equalsIgnoreCase("TYPE_APPROVED")) {
                        logger.info("TYPE_APPROVED  with .. " + file_details.getString("sub_feature"));
                        webActionDbDao.updateFeatureFileStatus(conn, file_details.getString("txn_id"), 2, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db
                    } else {
                        logger.info("STOLEN/BLOCK/RECOVERY/UNBLOCK   ");
                        ceirfunction.UpdateStatusViaApi(conn, file_details.getString("txn_id"), 0, file_details.getString("feature"));
                        FeatureForSingleStolenBlock featureForSingleStolenBlock = new FeatureForSingleStolenBlock();
                        featureForSingleStolenBlock.readFeatureWithoutFile(conn, file_details.getString("feature"), file_details.getString("txn_id"), file_details.getString("sub_feature"), feature_file_mapping.get("mgnt_table_db"), user_type);
                    }
                }
            }
//               raw_upload_set_no = 1;
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
                new ErrorFileGenrator().gotoErrorFile(conn, file_details.getString("txn_id"), "  Something went Wrong. Please Contact to Ceir Admin.  ");
                new CEIRFeatureFileFunctions().UpdateStatusViaApi(conn, file_details.getString("txn_id"), 1, file_details.getString("feature"));       //1 for reject
                new WebActionDbDao().updateFeatureFileStatus(conn, file_details.getString("txn_id"), 5, file_details.getString("feature"), file_details.getString("sub_feature")); // update web_action_db
                conn.commit();
            } catch (Exception e1) {
                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e1);
            }
        } finally {
            try {
                CEIRFeatureFileParser.cEIRFeatureFileParser(conn, feature);
            } catch (Exception e) {
                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            }
        }
    }

    static void getFinalDetailValues(Connection conn, String complete_file_path, String outputDb, String txn_id, String feature, String sub_feature) {
        ResultSet rs = null;
        Statement stmt = null;
        String query = null;
        CEIRFeatureFileFunctions ceirfunction = new CEIRFeatureFileFunctions();
        try {
            query = "select count(*) as cont from  " + feature + "_raw  where txn_id ='" + txn_id + "'";
            logger.info("Query is " + query);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            int dbCount = 0;
            while (rs.next()) {
                dbCount = rs.getInt("cont");
            }

            Path path = Paths.get(complete_file_path);
            long lineCount = Files.lines(path).count();         //  if space is their , it fails
            logger.info("lineCount from File  is " + lineCount);
            logger.info("lineCount from outputDB   is " + dbCount);
            if (dbCount == lineCount) {
                new WebActionDbDao().updateFeatureFileStatus(conn, txn_id, 2, feature, sub_feature);
                logger.info("Web action 2   done");
            } else {
                query = "delete from   " + outputDb + " where txn_id ='" + txn_id + "'";
                logger.info(query);
                stmt.executeQuery(query);
                new WebActionDbDao().updateFeatureFileStatus(conn, txn_id, 0, feature, sub_feature);
                logger.info("Web action 0   done");
            }

        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);

        }

    }

}

//                ResultSet my_result_set = ceir_parser_main.operatorDetails(conn, file_details.getString("feature"));     //select * from re p_schedule_config_db
//                if (my_result_set.next()) {
//                    raw_upload_set_no = my_result_set.getInt("raw_upload_set_no");
//                }
//                logger.info("raw_upload_set_no >>>>>>" + raw_upload_set_no);
//                complete_file_path = basePath + file_details.getString("txn_id") + "/" + feature_file_management.get("file_name");
//                logger.info("Complete file name is.... " + complete_file_path);
//                File uplodedfile = new File(complete_file_path);
//                logger.info("File exist Type.... " + uplodedfile.exists());
//                if (uplodedfile.exists()) {
//                    logger.info("File Exists.... ");
//                    rawDataResult = hfr.readConvertedFeatureFile(conn, feature_file_management.get("file_name"), complete_file_path, file_details.getString("feature"), basePath, raw_upload_set_no, file_details.getString("txn_id"), file_details.getString("sub_feature"), feature_file_mapping.get("mgnt_table_db"), user_type);
//                } else {
//                    logger.info("File not exists.... ");
//                    hfr.readFeatureWithoutFile(conn, file_details.getString("feature"), raw_upload_set_no, file_details.getString("txn_id"), file_details.getString("sub_feature"), feature_file_mapping.get("mgnt_table_db"), user_type);
//                }

