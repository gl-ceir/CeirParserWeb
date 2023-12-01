/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.glocks.parser.service;

import com.glocks.dao.BrandModelDbDao;
import static com.glocks.parser.MainController.appdbName;




import com.glocks.dao.SysConfigurationDao;
import com.glocks.parser.CEIRFeatureFileParser;
import com.glocks.util.Util;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

/**
 *
 * @author sachin
 */
@Service
public class StolenRecoverBlockUnBlockImpl {

    public static Logger logger = LogManager.getLogger(StolenRecoverBlockUnBlockImpl.class);
    static StackTraceElement l = new Exception().getStackTrace()[0];

    public void removeDeviceDetailsByTxnId(Connection conn, String txn_id, String tableName) {
        Statement stmt = null;
        Statement stmt1 = null;
        Statement stmt2 = null;
        Statement stmt3 = null;
        Statement stmt4 = null;
        ResultSet rs = null;
//          String rawTable =  tableName == "device_lawful_db" ? "Recovery" : "Unblock" ;
        String expdate = getExpiryDateValue(conn, txn_id);
        CEIRFeatureFileParser cEIRFeatureFileParser = new CEIRFeatureFileParser();
        HashMap<String, String> stolnRcvryDetails = getStolenRecvryDetails(conn, txn_id);   //IMEI_ESN_MEID
        boolean isOracle = conn.toString().contains("oracle");
        String dateFunction = Util.defaultDate(isOracle);

//          String query = "select imei_esn_meid , user_id from  " + tableName + "  where SN_OF_DEVICE in ( "
//                  + "select SN_OF_DEVICE from  " + tableName + " where  TXN_ID = '" + txn_id + "'  "
//                  + "and  SN_OF_DEVICE is not null)  union  select   imei_esn_meid   , user_id "
//                  + "from   " + tableName + " where  TXN_ID = '" + txn_id + "'  "
//                  + "and  SN_OF_DEVICE is null  ";
        String query = " select actual_imei,  imei_esn_meid , user_id , DEVICE_ID_type  , DEVICE_TYPE  from   "+appdbName+"." + tableName + "  where SN_OF_DEVICE in (   select SN_OF_DEVICE from   "+appdbName+"." + tableName + " where imei_esn_meid in "
                + "(select SUBSTR( IMEIESNMEID, 1,14)    from  "+appdbName+"." + stolnRcvryDetails.get("reason") + "_raw  where  TXN_ID =  '" + txn_id + "'   ) "
                + "  and  SN_OF_DEVICE is not null and SN_OF_DEVICE != 'null' )      "
                + "  UNION      select actual_imei,  imei_esn_meid   , user_id , DEVICE_ID_type  , DEVICE_TYPE  from    "+appdbName+"." + tableName + "  "
                + " where imei_esn_meid  in  ( select  SUBSTR( IMEIESNMEID, 1,14)    from  "+appdbName+"." + stolnRcvryDetails.get("reason") + "_raw where  TXN_ID =  '" + txn_id + "'   )    and   SN_OF_DEVICE =  'null'   ";
//           query = "select imei_esn_meid , user_id  from  " + tableName + "  where txn_id =   ";
        logger.info(" ..:::   " + query);
        try {
            stmt1 = conn.createStatement();
            stmt = conn.createStatement();
            stmt2 = conn.createStatement();
            stmt3 = conn.createStatement();
            stmt4 = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {

                query = "insert into   "+appdbName+".greylist_db_history ( EXPIRY_DATE, modified_on ,  created_on , imei, user_id , txn_id , mode_type  , request_type, user_type  , complain_type ,operation    , operator_id , operator_name  ,actual_imei , DEVICE_ID_type  , DEVICE_TYPE  )   "
                        + "values(   " + expdate + " , " + dateFunction + ",   " + dateFunction + ",    " + "'" + rs.getString("imei_esn_meid")
                        + "'," + " ( select username from users where users.id=  "
                        + rs.getString("user_id") + "  )  ,  " + " '" + txn_id + "', " + "'"
                        + stolnRcvryDetails.get("source") + "'," + "'" + stolnRcvryDetails.get("reason") + "',"
                        + " ( select USERTYPE_NAME from "+appdbName+".usertype  where ID = (select  usertype_id from users where id =  " + rs.getString("user_id") + "  ) )   ," + "'"
                        + stolnRcvryDetails.get("complaint_type") + "' ,"
                        + "  1    , (select OPERATOR_TYPE_ID  from "+appdbName+".user_profile where USERID =   " + rs.getString("user_id") + "  )  , (select OPERATOR_TYPE_NAME  from "+appdbName+".user_profile where USERID =   " + rs.getString("user_id") + "  ) , '" + rs.getString("actual_imei") + "'  , '" + rs.getString("device_id_type") + "'  , '" + rs.getString("device_type") + "'         )";

                logger.info(" ..:::: " + query);
                stmt1.executeUpdate(query);

                try {

                    query = "delete from "+appdbName+"." + tableName + " where imei_esn_meid = '" + rs.getString("imei_esn_meid") + "' ";
                    logger.info(" ..:::: " + query);

                    stmt2.executeUpdate(query);

                } catch (Exception e) {
                    logger.error(" ..@ :" + e);
                }

                try {
                    query = "delete from "+appdbName+".greylist_db where imei  = '" + rs.getString("imei_esn_meid") + "' ";
                    logger.info(" ___ " + query);
                    stmt3.executeUpdate(query);

                } catch (Exception e) {
                    logger.error(" .. $ :" + e);
                }
                try {
                    query = "delete from "+appdbName+".black_list where imei = '" + rs.getString("imei_esn_meid") + "' ";

                    logger.info(" ___ " + query);
                    stmt3.executeUpdate(query);
                } catch (Exception e) {
                    logger.error(" .. $$ :" + e);
                }

            }
            rs.close();
            stmt.close();
            stmt1.close();
            stmt2.close();
            stmt3.close();
            stmt4.close();
            conn.commit();
        } catch (Exception e) {
            logger.error(" ..1:" + e);
        }

    }

    private static String getExpiryDateValue(Connection conn, String txnId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        Statement stmt = null;
        ResultSet rs = null;
        String finalDate = null;
        String BLOCKING_TYPE = null;
        String BLOCKING_TIME_PERIOD = null;
        String defaultDays = null;
        try {
            stmt = conn.createStatement();
            String qry = "select BLOCKING_TYPE , BLOCKING_TIME_PERIOD  from "+appdbName+".stolenand_recovery_mgmt where  txn_id  = '" + txnId + "' ";
            logger.info("" + qry);
            rs = stmt.executeQuery(qry);
            while (rs.next()) {
                BLOCKING_TYPE = rs.getString("BLOCKING_TYPE");
                BLOCKING_TIME_PERIOD = rs.getString("BLOCKING_TIME_PERIOD");
            }
            logger.info("BLOCKING_TYPE " + BLOCKING_TYPE);
            if (BLOCKING_TYPE.equalsIgnoreCase("default")) {
//                rs = stmt.executeQuery("select value from sys_param where tag = ''   ");
//                while (rs.next()) {
//                    defaultDays = rs.getString("value");
//                }
                defaultDays = new SysConfigurationDao().getTagValue(conn, "GREY_TO_BLACK_MOVE_PERIOD_IN_DAY");

                logger.info(" defaultDays " + defaultDays);
                cal.add(Calendar.DAY_OF_MONTH, Integer.parseInt(defaultDays));
                finalDate = sdf.format(cal.getTime());
            } else if (BLOCKING_TYPE.equalsIgnoreCase("Immediate")) {
                cal.add(Calendar.DAY_OF_MONTH, 0);
                finalDate = sdf.format(cal.getTime());
            } else if (BLOCKING_TYPE.equalsIgnoreCase("tillDate")) {
                finalDate = BLOCKING_TIME_PERIOD;
            }
            logger.info(" finalDate " + finalDate);

        } catch (Exception e) {
            logger.error(e);
        }
        return "TO_DATE('" + finalDate + "','YYYY-MM-DD HH24:MI:SS')";
    }

    public static HashMap<String, String> getStolenRecvryDetails(Connection conn, String txn_id) {
        HashMap<String, String> map = new HashMap<String, String>();
        String errorFilePath = "";
        String query = null;
        String source_type = null;
        String request_type = null;
        String complaint_type = null;
        ResultSet rs1 = null;
        Statement stmt = null;
        String operation = null;
        String txnId = null;
        String reason = null;
        String usertype = null;
        String source = null;
        String divceStatus = null;

        String user_id = null;

        try {
            query = "select request_type ,source_type  ,complaint_type  ,txn_id ,user_id from "+appdbName+".stolenand_recovery_mgmt   where txn_id = '"
                    + txn_id + "'";
            logger.info("Query   " + query);
            stmt = conn.createStatement();
            rs1 = stmt.executeQuery(query);
            while (rs1.next()) {
                source_type = rs1.getString("source_type");
                request_type = rs1.getString("request_type");
                complaint_type = rs1.getString("complaint_type");
                txnId = rs1.getString("txn_id");
                user_id = rs1.getString("user_id");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                stmt.close();
                rs1.close();
            } catch (SQLException e) {
                logger.error("Error.." + e);
            }
        }

        if (request_type.equals("0")) {
            reason = "Stolen";
            usertype = "Lawful Agency";
            operation = "0";
            divceStatus = "Pending For Addition";
        }
        if (request_type.equals("1")) {
            reason = "Recovery";
            usertype = "Lawful Agency";
            operation = "1";
            divceStatus = "Pending For Deletion";
        }

        if (request_type.equals("2")) {
            reason = "Block";
            usertype = "Operator";
            operation = "0";
            divceStatus = "Pending For Addition";
        }

        if (request_type.equals("3")) {
            reason = "UnBlock";
            usertype = "Operator";
            operation = "1";
            divceStatus = "Pending For Deletion";
        }

        if (source_type.equals("4") || source_type.equals("5")) {
            source = "Single";
        } else {
            source = "Bulk";
        }

        map.put("source_type", source_type);
        map.put("request_type", request_type);
        map.put("reason", reason);
        map.put("usertype", usertype);
        map.put("source", source);
        map.put("complaint_type", (complaint_type == null) ? "NA" : complaint_type);
        map.put("operation", operation);
        map.put("txnId", txnId);
        map.put("divceStatus", divceStatus);
        map.put("user_id", user_id);

        if (txnId == null || txnId.equals("")) {
            map.put("source_type", "");
            map.put("request_type", "");
            map.put("reason", "");
            map.put("usertype", "");
            map.put("source", "");
            map.put("complaint_type", "");
            map.put("operation", "");
            map.put("txnId", "");
            map.put("divceStatus", "");
            map.put("user_id", "");

        }

        return map;

    }

    public void updateDeviceDetailsByTxnId(Connection conn, String txnId, String TableName) {
        insertInGreyListByTxnId(conn, txnId, TableName);
        HashMap<String, String> stolnRcvryDetails = new HashMap<String, String>();
        stolnRcvryDetails = getStolenRecvryDetails(conn, txnId);   //IMEI_ESN_MEID
//          String dfnc =;   // "+ Util.defaultDateNow(true) +"

        String query = "update "+appdbName+"." + TableName + " set DEVICE_STATUS  = 'Approved' , modified_on =    " + Util.defaultDateNow(true) + " "
                + " where actual_imei  in   "
                + "(select       IMEIESNMEID   from  "+appdbName+"." + stolnRcvryDetails.get("reason") + "_raw  where  TXN_ID =  '" + txnId + "'   ) "
                + "   ";
        Statement stmt = null;
        logger.info("update   as  APPROVED  ...[" + query + "]");

        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
        } catch (Exception e) {
            logger.info("Error" + e);
        } finally {
            try {
                stmt.close();
                conn.commit();
                new BrandModelDbDao().updateModelBrandNameByTxnId(conn, txnId, TableName);
            } catch (Exception e) {
                logger.error("" + e);
            }
        }
    }

    public static void insertInGreyListByTxnId(Connection conn, String txnId, String tableName) {
        ResultSet rs = null;
        String expdate = getExpiryDateValue(conn, txnId);

        Statement stmt = null;
        Statement stmt1 = null;
        Statement stmt2 = null;
        HashMap<String, String> stolnRcvryDetails = new HashMap<String, String>();
        stolnRcvryDetails = getStolenRecvryDetails(conn, txnId);   //IMEI_ESN_MEID
//                    String query = "select imei_esn_meid , user_id  from  " + tableName + "  where txn_id = '" + txnId + "'   ";
        // mar9
//        String query = " select actual_imei, imei_esn_meid , user_id , DEVICE_ID_TYPE , DEVICE_TYPE  from   " + tableName + "  where SN_OF_DEVICE in (   select SN_OF_DEVICE from   " + tableName + " where actual_imei in "
//                + "(select IMEIESNMEID  from  " + stolnRcvryDetails.get("reason") + "_raw  where  TXN_ID =  '" + txnId + "'   ) "
//                + "  and  SN_OF_DEVICE is not null and SN_OF_DEVICE != 'null' )      "
//                + "  UNION      select actual_imei,  imei_esn_meid   , user_id , DEVICE_ID_TYPE , DEVICE_TYPE from    " + tableName + "  "
//                + " where actual_imei  in  ( select IMEIESNMEID from  " + stolnRcvryDetails.get("reason") + "_raw where  TXN_ID =  '" + txnId + "'   )    and   SN_OF_DEVICE =  'null'   ";

        String query = " select actual_imei, imei_esn_meid , user_id , DEVICE_ID_TYPE , DEVICE_TYPE  from   "+appdbName+"." + tableName + " "
                + "  where   actual_imei     in (select IMEIESNMEID  from "+appdbName+"." + stolnRcvryDetails.get("reason") + "_raw  where  TXN_ID =   '" + txnId + "'   )  ";

        logger.info(" ...[" + query + "]");
        String device_greylist_db_qry = null;
        String device_greylist_History_db_qry = null;
        boolean isOracle = conn.toString().contains("oracle");
        String dateFunction = Util.defaultDate(isOracle);
        try {
            stmt = conn.createStatement();
            stmt1 = conn.createStatement();
            stmt2 = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
//                    {
//                         if (stolnRcvryDetails.get("operation").equals("0")) {
                device_greylist_db_qry = "insert into   "+appdbName+".greylist_db (EXPIRY_DATE,created_on ,modified_on , imei, user_id , txn_id , mode_type  , request_type, user_type  , complain_type , operator_id , operator_name ,actual_imei , DEVICE_ID_type  , DEVICE_TYPE  )   "
                        + "values(  " + expdate + "    ,  " + dateFunction + ",    " + dateFunction + "," + "'" + rs.getString("imei_esn_meid")
                        + "'," + " ( select username from "+appdbName+".users where users.id=  "
                        + rs.getString("user_id") + "  )  ,  " + " '" + txnId + "', " + "'"
                        + stolnRcvryDetails.get("source") + "'," + "'" + stolnRcvryDetails.get("reason")
                        + "'," + "   ( select USERTYPE_NAME from "+appdbName+".usertype  where ID = (select  usertype_id from "+appdbName+".users where id =  " + rs.getString("user_id") + "  ) )     ," + "'"
                        + stolnRcvryDetails.get("complaint_type") + "' , (select OPERATOR_TYPE_ID  from "+appdbName+".user_profile where USERID =   " + rs.getString("user_id") + "  )  , (select OPERATOR_TYPE_NAME  from "+appdbName+".user_profile where USERID =   " + rs.getString("user_id") + "  )   , '" + rs.getString("actual_imei") + "' , '" + rs.getString("device_id_type") + "'  , '" + rs.getString("device_type") + "'        )";
//                         }
                device_greylist_History_db_qry = "insert into   "+appdbName+".greylist_db_history ( EXPIRY_DATE,modified_on ,  created_on , imei, user_id , txn_id , mode_type  , request_type, user_type  , complain_type ,operation  ,   operator_id , operator_name ,actual_imei  , DEVICE_ID_type  , DEVICE_TYPE  )   "
                        + "values(   " + expdate + " ,  " + dateFunction + ",       " + dateFunction + "," + "'" + rs.getString("imei_esn_meid")
                        + "'," + " ( select username from "+appdbName+".users where users.id=  "
                        + rs.getString("user_id") + "  )  ,  " + " '" + txnId + "', " + "'"
                        + stolnRcvryDetails.get("source") + "'," + "'" + stolnRcvryDetails.get("reason") + "',"
                        + "  ( select USERTYPE_NAME from "+appdbName+".usertype  where ID = (select  usertype_id from "+appdbName+".users where id =  " + rs.getString("user_id") + "  ) )     ," + "'"
                        + stolnRcvryDetails.get("complaint_type") + "' , "
                        + " 0   , (select OPERATOR_TYPE_ID  from "+appdbName+".user_profile where USERID =   " + rs.getString("user_id") + "  )  , (select OPERATOR_TYPE_NAME  from "+appdbName+".user_profile where USERID =   " + rs.getString("user_id") + "  )  , '" + rs.getString("actual_imei") + "' , '" + rs.getString("device_id_type") + "'  , '" + rs.getString("device_type") + "'   )";
                logger.info(" " + device_greylist_db_qry);
                try {
                    stmt1.executeUpdate(device_greylist_db_qry);
                } catch (Exception e) {
//                         logger.error(" . " + e);
                }
                logger.info("" + device_greylist_History_db_qry);
                try {
                    stmt2.executeUpdate(device_greylist_History_db_qry);
                } catch (Exception e) {
                    logger.error(" .histry  " + e);
                }

//                    }
            }
            rs.close();
        } catch (Exception e) {
            logger.error("Error:: " + e);
        } finally {
            try {
                rs.close();
                stmt.close();
                stmt1.close();
                stmt2.close();
                conn.commit();
            } catch (Exception e) {
                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            }
        }
    }
}
