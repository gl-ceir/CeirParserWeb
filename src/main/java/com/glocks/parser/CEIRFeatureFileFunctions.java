package com.glocks.parser;

import com.gl.Rule_engine_Old.RuleEngineApplication;
import com.glocks.constants.PropertyReader;
import com.glocks.dao.SysConfigurationDao;
import com.glocks.parser.service.ConsignmentInsertUpdate;
import com.glocks.parser.service.StolenRecoverBlockUnBlockImpl;
import com.glocks.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.glocks.parser.MainController.appdbName;
import static com.glocks.parser.MainController.ip;

public class CEIRFeatureFileFunctions {

    public static PropertyReader propertyReader;

    Logger logger = LogManager.getLogger(CEIRFeatureFileFunctions.class);

    public HashMap<String, String> getFeatureMapping(Connection conn, String feature, String usertype_name) {
        HashMap<String, String> feature_mapping = new HashMap<String, String>();
        String addQuery = "";
        String limiter = " limit 1 ";
        if (conn.toString().contains("oracle")) {
            limiter = " fetch next 1 rows only ";
        }
        if (!usertype_name.equals("NOUSER")) {
            addQuery = " and usertype_name = '" + usertype_name + "'   ";
        }
        String query = "select * from " + appdbName + ".feature_table_mapping where  feature='" + feature + "'  " + addQuery + "    " + limiter + "   ";
        logger.info("Query  File Details [" + query + "]");
        try (var stmt = conn.createStatement(); var rs = stmt.executeQuery(query);) {
            while (rs.next()) {
                feature_mapping.put("usertype", rs.getString("usertype"));
                feature_mapping.put("feature", feature);
                feature_mapping.put("mgnt_table_db", rs.getString("mgnt_table_db"));
                feature_mapping.put("output_device_db", rs.getString("output_device_db"));
                feature_mapping.put("USERTYPE_NAME", rs.getString("USERTYPE_NAME"));
            }
        } catch (Exception e) {
            logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) + "]");
        }
        return feature_mapping;

    }

    public HashMap<String, String> getFeatureFileManagement(Connection conn, String management_db, String txn_id) {
        HashMap<String, String> feature_file_management_details = new HashMap<String, String>();
        String query  = "select * from " + appdbName + "." + management_db + " where  txn_id='" + txn_id + "'";
            logger.info("Query to (getFeatureFileManagement) File Details [" + query + "]");
            try (var stmt = conn.createStatement(); var rs = stmt.executeQuery(query);) {
            while (rs.next()) {
                feature_file_management_details.put("user_id", rs.getString("user_id"));
                feature_file_management_details.put("file_name", rs.getString("file_name"));
                feature_file_management_details.put("created_on", rs.getString("created_on"));
                feature_file_management_details.put("modified_on", rs.getString("modified_on"));
                feature_file_management_details.put("delete_flag", rs.getString("delete_flag"));
            }
        } catch (Exception e) {
                logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) + "]");
        }

        return feature_file_management_details;

    }

    public void updateFeatureManagementStatus(Connection conn, String txn_id, int status, String table_name, String main_type) {
        if (table_name.equalsIgnoreCase("stolen_and_recovery_txn")) {
            main_type = "file";
        }
        String query = "update " + appdbName + "." + table_name + " set   " + main_type.trim().toLowerCase() + "_status=" + status
                + " where txn_id='" + txn_id + "'";
        logger.info("update  " + main_type.toLowerCase() + "_status db..[" + query + "]");
        try (var stmt = conn.createStatement();){
            stmt.executeUpdate(query);
        } catch (Exception e) {
            logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) + "]");
        }
    }

    public String getUserType(Connection conn, String user_id, String main_type, String txn_id) {
        String user_type = null;
        String query = "select b.user_type_name as user_type_name from " + appdbName + ".users a, " + appdbName + ".user_type b where a.user_type_id=b.id and a.id='" + user_id + "'";
        if (main_type.toLowerCase().equals("stock")) {
            query = "select  role_type  as user_type_name from " + appdbName + ".stock_mgmt  where txn_id = '" + txn_id + "'"; // hardcodeed
        }
        logger.info(" Query  for " + main_type + ":::" + query);
        try (var stmt = conn.createStatement(); var rs = stmt.executeQuery(query);) {
            while (rs.next()) {
                user_type = rs.getString(1);
            }
            logger.info("user_type.. +" + user_type);
        } catch (Exception e) {
            logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) + "]");
        }
        return user_type;
    }

    public void UpdateStatusViaApi(Connection conn, String txn_id, int Action, String feature) {
        logger.info("UpdateStatus ViaApi..  : 0 - Processing; 1 -Reject; 2- PEnding by admin  ");
        propertyReader = new PropertyReader();
        ResultSet rs1 = null;
        Statement stmt = null;
        String tag = null;
        String apiURI = null;
        String responseBody = null;
        String featureId = "";
        String requestType = "";
        // String txn_id = map.get("txn_id");;
        String userId = "";
        if (feature.equalsIgnoreCase("Register Device")) {
            apiURI = "RegisterDevice_api_URI";
            responseBody = "{\n"
                    + "\"action\": " + Action + "   ,\n"
                    + "\"txnId\": \"" + txn_id + "\",\n"
                    + "\"userType\": \"CEIRSYSTEM\"\n"
                    + "}";
        }
        if (feature.equalsIgnoreCase("Update Visa")) {
            apiURI = "VisaUpdate_api_URI";
            responseBody = "{\n"
                    + "\"action\": " + Action + "   ,\n"
                    + "\"txnId\": \"" + txn_id + "\",\n"
                    + "\"userType\": \"CEIRSYSTEM\"\n"
                    + "}";
        }
        if (feature.equalsIgnoreCase("stock")) {
            apiURI = "stock_api_URI";
            responseBody = "{  \"action\": " + Action + " ,  \"remarks\":\"\",  \"roleType\": \"CEIRSystem\",  \"txnId\": \"" + txn_id + "\"  ,\"featureId\" : 4 }";
        }
        if (feature.equalsIgnoreCase("consignment")) {
            apiURI = "mail_api_path";
            responseBody = "{  \"action\":    " + Action
                    + "    ,  \"requestType\": 0,  \"roleType\": \"CEIRSYSTEM\",  \"txnId\": \"" + txn_id
                    + "\"  ,\"featureId\" : 3 }";
        }

        if ((feature.equalsIgnoreCase("stolen") || feature.equalsIgnoreCase("recovery")
                || feature.equalsIgnoreCase("block") || feature.equalsIgnoreCase("unblock"))) {
            apiURI = "stolen-recovery_mailURI";
            HashMap<String, String> map = new StolenRecoverBlockUnBlockImpl().getStolenRecvryDetails(conn, txn_id);
            featureId = (map.get("request_type").equals("0") || map.get("request_type").equals("1")) ? "5" : "7";
            requestType = map.get("request_type");
            userId = map.get("user_id");

            responseBody = " {\n" + "\"action\":" + Action + ",\n" + "\"featureId\":" + featureId + ",\n"
                    + "\"remarks\":\"\",\n" + "\"requestType\":" + requestType + ",\n"
                    + "\"roleType\":\"CEIRSYSTEM\",\n" + "\"roleTypeUserId\":0,\n" + "\"txnId\":\"" + txn_id + "\",\n"
                    + "\"userId\":" + userId + ",\n" + "\"userType\": \"CEIRSYSTEM\"\n" + "}  ";
        }

        String query = "select value from " + appdbName + ".sys_param where tag='" + apiURI + "'";
        try {
            logger.info("Query is " + query);
            logger.info("............   " + responseBody + " ");
            stmt = conn.createStatement();
            rs1 = stmt.executeQuery(query);
            while (rs1.next()) {
                tag = rs1.getString("value");
            }
            stmt.close();
            logger.info(" Tag before Replace  " + tag);
            tag = tag.replace("$LOCAL_IP", ip);
            //logger.info("UNCOMMENT TO RUN THE API  " );
          //  new HttpURLConnectionExample().HttpApiConnecter(tag, responseBody);
        } catch (Exception e) {
            logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) + "]");
        }
    }

    public void updateFeatureManagementDeleteStatus(Connection conn, String txn_id, int status, String table_name) {
        String query = "update " + appdbName + "." + table_name + " set delete_status =" + status + " where txn_id='" + txn_id + "'";
        logger.info("update delete status [" + query + "]");
        try( var stmt = conn.createStatement();){
            stmt.executeUpdate(query);
            conn.commit();
        } catch (Exception e) {
            logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) + "]");
        }
    }

    public Map<String, String> getUserRoleType(Connection conn, String txn_id) {
        Statement stmt = null;
        ResultSet rs = null;
        String query = null;
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            query = "select role_type , user_type  from " + appdbName + ".stock_mgmt where  txn_id = '" + txn_id + "'   ";
            logger.info("Query to get getUserRoleType [" + query + "]");
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                map.put("user_type", rs.getString("user_type"));
                map.put("role_type", rs.getString("role_type"));
            }
        } catch (Exception e) {
            logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) + "]");
        }
        return map;
    }

    void getfromRegulizeEnterInCustom(Connection conn, String txn_id) {
        String device_db_query = null;
        String InsrtQry = null;
        boolean isOracle = conn.toString().contains("oracle");
        String dateFunction = Util.defaultDate(isOracle);
        String period = new SysConfigurationDao().getTagValue(conn, "GRACE_PERIOD_END_DATE");

        try {
            String ValImei = "";
            for (int i = 1; i < 5; i++) {
                if (i == 1) {
                    ValImei = "first_imei";
                }
                if (i == 2) {
                    ValImei = "second_imei";
                }
                if (i == 3) {
                    ValImei = "third_imei";
                }
                if (i == 4) {
                    ValImei = "fourth_imei";
                }
                String  query = "select * from " + appdbName + ".regularize_device_db where  txn_id = '" + txn_id + "' and  " + ValImei + " is not null   ";
                logger.info(" / " + query);// 0 tax paid , 1 - not tax Paid, 2 : regulized   , 3  ????
               Statement stmt1 = conn.createStatement();
                int dvcDbCounter = 0;
                try (var stmt = conn.createStatement(); var  rs = stmt.executeQuery(query);){
                    while (rs.next()) {
                        InsrtQry = "insert  into " + appdbName + ".device_custom_db( CREATED_ON , modified_on , DEVICE_ID_TYPE, DEVICE_STATUS,DEVICE_TYPE,IMEI_ESN_MEID,MULTIPLE_SIM_STATUS,FEATURE_NAME ,TXN_ID,user_id , period , actual_imei) "
                                + "values (" + dateFunction + " , " + dateFunction + " ,  '" + rs.getString("DEVICE_ID_TYPE") + "' , '" + rs.getString("DEVICE_STATUS") + "', '" + ((rs.getString("DEVICE_TYPE") == null || rs.getString("DEVICE_TYPE").equals("")) ? "NA" : rs.getString("DEVICE_TYPE")) + "'  , '" + rs.getString("" + ValImei + "").substring(0, 14) + "' , '" + rs.getString("MULTI_SIM_STATUS") + "' , 'Register Device' , '" + rs.getString("TXN_ID") + "','" + rs.getString("TAX_COLLECTED_BY") + "' , '" + period + "'   , '" + rs.getString("" + ValImei + "") + "'   )";
                        logger.info(" insert qury  [" + InsrtQry + "]");
                        stmt1.executeUpdate(InsrtQry);
                        dvcDbCounter = new ConsignmentInsertUpdate().getCounterFromDeviceDb(conn, rs.getString("" + ValImei + "").substring(0, 14));
                        if (dvcDbCounter == 0) {
                            device_db_query = "insert  into " + appdbName + ".device_db( counter ,  CREATED_ON , modified_on , DEVICE_ID_TYPE, DEVICE_STATUS,DEVICE_TYPE,IMEI_ESN_MEID,MULTIPLE_SIM_STATUS,FEATURE_NAME ,TXN_ID,period ,actual_imei ) "
                                    + "values (  1 , " + dateFunction + " , " + dateFunction + " ,  '" + rs.getString("DEVICE_ID_TYPE") + "' , '" + rs.getString("DEVICE_STATUS") + "', '" + ((rs.getString("DEVICE_TYPE") == null || rs.getString("DEVICE_TYPE").equals("")) ? "NA" : rs.getString("DEVICE_TYPE")) + "' , '" + rs.getString("" + ValImei + "").substring(0, 14) + "' , '" + rs.getString("MULTI_SIM_STATUS") + "' , 'Register Device' , '" + rs.getString("TXN_ID") + "', '" + period + "'  , '" + rs.getString("" + ValImei + "") + "'     )";
                        } else {
                            device_db_query = "update  " + appdbName + ".device_db set counter = " + (dvcDbCounter + 1) + " where imei_esn_meid =   '" + rs.getString("" + ValImei + "").substring(0, 14) + "'   ";
                        }
                        logger.info(" insert device_db_query  [" + device_db_query + "]");
                        stmt1.executeUpdate(device_db_query);
                        markUserRegtoAllowedActiveDb(conn, rs.getString("" + ValImei + ""));
                        removeImeiFromGreyBlackDb(conn, rs.getString("" + ValImei + ""));
                    }
                } catch (Exception e) {
                    logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) + "]");
                    stmt1.close();  // Optimise
                }
            }
        } catch (Exception e) {
            logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) + "]");
        }
    }

    private void markUserRegtoAllowedActiveDb(Connection conn, String ValImei) {
        String prdType = new SysConfigurationDao().getTagValue(conn, "GRACE_PERIOD_END_DATE");
        logger.info(" Current  PERIOD [" + prdType + "]");
        try(  var stmt = conn.createStatement();) {
            ValImei = ValImei.substring(0, 14);
            if (prdType.equalsIgnoreCase("post_grace")) {
                String query = " update " + appdbName + ".active_unique_imei set action = 'ALLOWED' where IMEI =  '" + ValImei + "'  and  action = 'USER_REG' ";
                logger.info("  " + query);
                stmt.executeUpdate(query);
            }
        } catch (Exception e) {
            logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) + "]");
        }
    }

    private void removeImeiFromGreyBlackDb(Connection conn, String ValImei) {
        try(var  stmt = conn.createStatement();) {
            ValImei = ValImei.substring(0, 14);
            String   qury = " delete from " + appdbName + ".grey_list where imei= '" + ValImei + "' ";
            logger.info("  " + qury);
            stmt.executeUpdate(qury);
        } catch (Exception e) {
            logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) + "]");
        }
    }

    void updateStatusOfRegularisedDvc(Connection conn, String txn_id) {
        ResultSet rs = null;
        try (var stmt = conn.createStatement();) {
            String ValImei = "";
            int resultValue = 1;
            String action_output = null;
            for (int i = 1; i < 5; i++) {
                if (i == 1) {
                    ValImei = "first_imei";
                }
                if (i == 2) {
                    ValImei = "second_imei";
                }
                if (i == 3) {
                    ValImei = "third_imei";
                }
                if (i == 4) {
                    ValImei = "fourth_imei";
                }
             String   query = "select * from " + appdbName + ".regularize_device_db  where  txn_id = '" + txn_id + "'  and  " + ValImei + " is not null  and tax_paid_status =  2 ";                         /////
                logger.info( query);
                rs = stmt.executeQuery(query);
                while (rs.next()) {
                    String[] ruleArr = {"EXIST_IN_END_USER_DEVICE_DB", "1", "IMEI", rs.getString("" + ValImei + "").substring(0, 14)};   // typeApproceTac with status =3
                    action_output = RuleEngineApplication.startRuleEngine(ruleArr, conn, null);
                    logger.debug("action_output is " + action_output);
                    if (action_output.equalsIgnoreCase("Yes")) {
                        resultValue = 0;
                        return;
                    } else {
                        insertinEndUserDvcDb(conn, ValImei, rs);
                    }
                    markUserRegtoAllowedActiveDb(conn, rs.getString("" + ValImei + ""));
                    removeImeiFromGreyBlackDb(conn, rs.getString("" + ValImei + ""));
                }
            }
            conn.commit();
        } catch (Exception e) {
            logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) + "]");
        }
    }

    private void insertinEndUserDvcDb(Connection conn, String ValImei, ResultSet rs) {
        String dfnc = Util.defaultDateNow(true);   // "+dfnc+"
        try (var stmt = conn.createStatement();){
            String qury = "insert into " + appdbName + ".end_user_device_db ( imei_esn_meid , actual_imei, created_on , modified_on, CURRENCY,	DEVICE_ID_TYPE,	DEVICE_SERIAL_NUMBER,	DEVICE_STATUS,	DEVICE_TYPE ,	TAX_PAID_STATUS,	TXN_ID	,USER_ID	,CREATOR_USER_ID	,ORIGIN   ) values"
                    + "( '" + rs.getString("" + ValImei + "").substring(0, 14) + "', '" + rs.getString("" + ValImei + "") + "',  " + dfnc + " ," + dfnc + ", '" + rs.getString("CURRENCY") + "', '" + rs.getString("DEVICE_ID_TYPE") + "', '" + rs.getString("DEVICE_SERIAL_NUMBER") + "', '" + rs.getString("DEVICE_STATUS") + "', "
                    + " '" + rs.getString("DEVICE_TYPE") + "','" + rs.getString("TAX_PAID_STATUS") + "', '" + rs.getString("TXN_ID") + "', '" + rs.getString("USER_ID") + "', '" + rs.getString("CREATOR_USER_ID") + "',  '" + rs.getString("ORIGIN") + "'   ) ";
            logger.info(" insertinEndUserDvcDb Query  " + qury);
            stmt.executeUpdate(qury);
        } catch (Exception e) {
            logger.error(e + "in [" + Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) + "]");
        }
    }

}

//        try {
//          URL  url = new URL(tag);
//            HttpURLConnection hurl = (HttpURLConnection) url.openConnection();
//            hurl.setRequestMethod("PUT");
//            hurl.setDoOutput(true);
//            hurl.setRequestProperty("Content-Type", "application/json");
//            hurl.setRequestProperty("Accept", "application/json");
//
//            String payload = "{  action: " + action + ",  requestType: 0,  roleType: CEIRSYSTEM,  txnId: " + txn_id + ",featureId : 3 }";
//
//            OutputStreamWriter osw = new OutputStreamWriter(hurl.getOutputStream());
//            osw.write(payload);
//            osw.flush();
//            osw.close();
//            logger.info("Consignment status have Update SuccessFully  with status" + action + " for txn_d" + txn_id);
//        } catch (MalformedURLException e) {
//            // TODO Auto-generated catch block
//            logger.info("errror" + e);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            logger.info("errror" + e);
//        }
// con = getHttpConnection(tag, "PUT");
//            //you can add any request body here if you want to post
//            logger.info("conn. Reutrned");
//            con.setDoInput(true);
//            con.setDoOutput(true);
//            DataOutputStream out = new DataOutputStream(con.getOutputStream());
//            out.writeBytes(reqbody);
//            out.flush();
//            out.close();
//	public void pdateFeatureManagementStatus(Connection conn, String txn_id,int status,String table_name) {
//		String query = "";
//		Statement stmt = null;
//		query = "update "+table_name+" set status="+status+" where txn_id='"+txn_id+"'";
//		logger.info("update management db status ["+query+"]");
//		 // System.out.println("update management db status["+query+"]");
//		try {
//			stmt = conn.createStatement();
//			stmt.executeUpdate(query);
//			conn.commit();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		finally{
//			try {
//				stmt.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//
//void deleteFromCustom1(Connection conn, String txn_id, String string0) {
//
//          Statement stmt = null;
//          ResultSet rs = null;
//          Statement stmt1 = null;
//          Statement stmt3 = null;
//          ResultSet rs1 = null;
//          String query = null;
//          String InsrtQry = null;
//          boolean isOracle = conn.toString().contains("oracle");
//          String dateFunction = Util.defaultDate(isOracle);
//
//          try {
//               String ValImei = "";
//               for (int i = 1; i < 5; i++) {
//                    if (i == 1) {
//                         ValImei = "first_imei";
//                    }
//                    if (i == 2) {
//                         ValImei = "second_imei";
//                    }
//                    if (i == 3) {
//                         ValImei = "third_imei";
//                    }
//                    if (i == 4) {
//                         ValImei = "fourth_imei";
//                    }
//                    query = "select * from regularize_device_db where  txn_id = '" + txn_id + "'  where " + ValImei + " is not null  ";
//
////                    stmt = conn.createStatement();
////                    rs = stmt.executeQuery(query);
////                    while (rs.next()) {
////                         InsrtQry = "insert  into device_custom_db_au d(CREATED_ON , DEVICE_ID_TYPE, DEVICE_STATUS,DEVICE_TYPE,IMEI_ESN_MEID,MULTIPLE_SIM_STATUS,FEATURE_NAME ,TXN_ID) "
////                                 + "values (" + dateFunction + " , '" + rs.getString("DEVICE_ID_TYPE") + "' , '" + rs.getString("DEVICE_STATUS") + "', '" + rs.getString("DEVICE_TYPE") + "' , '" + rs.getString("" + ValImei + "") + "' , '" + rs.getString("MULTIPLE_SIM_STATUS") + "' , 'Register Device' , '" + rs.getString("TXN_ID") + "'     )";
////                         logger.info(" insert qury  [" + InsrtQry + "]");
////
////                         stmt1 = conn.createStatement();
////                         stmt1.executeQuery(query);
////
////                    }
//               }
//               stmt3 = conn.createStatement();
//               stmt3.executeQuery("delete from device_custom_db  where  txn_id = '" + txn_id + "' ");
//
//               conn.commit();
//          } catch (Exception e) {
//               logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
//          }
//
//     }

