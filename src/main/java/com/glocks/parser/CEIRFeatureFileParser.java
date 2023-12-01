package com.glocks.parser;

import com.glocks.dao.SysConfigurationDao;
import com.glocks.dao.WebActionDbDao;
import com.glocks.parser.service.ApproveConsignment;
import com.glocks.parser.service.ConsignmentDelete;
import com.glocks.parser.service.ConsignmentInsertUpdate;
import com.glocks.parser.service.RegisterTac;
import com.glocks.parser.service.StockDelete;
import com.glocks.parser.service.WithdrawnTac;

import static com.glocks.parser.MainController.appdbName;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CEIRFeatureFileParser {

    public static Logger logger = LogManager.getLogger(CEIRFeatureFileParser.class);

//    public static void mai n(String args[]) {
//        Connection conn = null;
//        conn = new com.glocks.db.MySQLConnection().getConnection();
////          cEIRFeatureFileParser(conn);
//    }
    public static void cEIRFeatureFileParser(Connection conn, String featureNam) {
        logger.info(" ...........................................................................................  ");
        logger.info(" CEIRFeatureFileParser.class ");
        String feature = null;
        CEIRFeatureFileFunctions ceirfunction = new CEIRFeatureFileFunctions();
        WebActionDbDao webActionDbDao = new WebActionDbDao();
        SysConfigurationDao SysConfigurationDao = new SysConfigurationDao();
        ResultSet featurers = webActionDbDao.getFileDetails(conn, 2, featureNam);     // Select * from "+appdbName+".web_action_db
        try {
            while (featurers.next()) {
                System.out.println("" + featurers.getString("txn_id"));
                webActionDbDao.updateFeatureFileStatus(conn, featurers.getString("txn_id"), 3, featurers.getString("feature"), featurers.getString("sub_feature"));  // update web_action
                logger.info("  webAction 3 don3e ");
                if (featurers.getString("feature").equalsIgnoreCase("Register Device")) {
                    logger.info("  Register Device::  " + featurers.getString("feature"));
                    if ((featurers.getString("sub_feature").equalsIgnoreCase("Register")) || (featurers.getString("sub_feature").equalsIgnoreCase("Add Device"))) {     //'Add Device'
                        ceirfunction.updateStatusOfRegularisedDvc(conn, featurers.getString("txn_id"));
                        ceirfunction.UpdateStatusViaApi(conn, featurers.getString("txn_id"), 2, featurers.getString("feature"));
                        webActionDbDao.updateFeatureFileStatus(conn, featurers.getString("txn_id"), 4, featurers.getString("feature"), featurers.getString("sub_feature")); // update web_action_db
                        break;
                    } else {
                        webActionDbDao.updateFeatureFileStatus(conn, featurers.getString("txn_id"), 4, featurers.getString("feature"), featurers.getString("sub_feature")); // update web_action_db
                        break;
                    }
                } else if (featurers.getString("feature").equalsIgnoreCase("Update Visa")) {
                    ceirfunction.UpdateStatusViaApi(conn, featurers.getString("txn_id"), 2, featurers.getString("feature"));
                    webActionDbDao.updateFeatureFileStatus(conn, featurers.getString("txn_id"), 4, featurers.getString("feature"), featurers.getString("sub_feature")); // update web_action_db
                    break;
                } else {
                    HashMap<String, String> feature_file_mapping = new HashMap<String, String>();
                    feature_file_mapping = ceirfunction.getFeatureMapping(conn, featurers.getString("feature"), "NOUSER");
                    HashMap<String, String> feature_file_management = new HashMap<String, String>();
                    feature_file_management = ceirfunction.getFeatureFileManagement(conn, feature_file_mapping.get("mgnt_table_db"), featurers.getString("txn_id"));   //  select * from " + management_db
                    String user_type = ceirfunction.getUserType(conn, feature_file_management.get("user_id"), featurers.getString("feature"), featurers.getString("txn_id"));
                    logger.info("user_type is [" + user_type + "] ");
                    feature = featurers.getString("feature");
                    ArrayList<Rule> rulelist = new ArrayList<Rule>();
                    String period = SysConfigurationDao.getTagValue(conn, "GRACE_PERIOD_END_DATE");
                    logger.info("Period is [" + period + "] ");
                    logger.info("State is [" + featurers.getInt("state") + "] ");
                    rulelist = CEIRFeatureFileParser.getRuleDetails(feature, conn, "", period, "", user_type);
                    addCDRInProfileWithRule(feature, conn, rulelist, "", featurers.getString("txn_id"), featurers.getString("sub_feature"), user_type, featurers.getInt("state"));
                }
            }
            conn.close();
        } catch (SQLException e) {

            try {
                new ErrorFileGenrator().gotoErrorFile(conn, featurers.getString("txn_id"), "  Something went Wrong. Please Contact to Ceir Admin.  ");
                new CEIRFeatureFileFunctions().UpdateStatusViaApi(conn, featurers.getString("txn_id"), 1, feature);       //1 for reject
                webActionDbDao.updateFeatureFileStatus(conn, featurers.getString("txn_id"), 5, feature, featurers.getString("sub_feature")); // update web_action_db
            } catch (SQLException ex) {
                logger.error("" + ex);
            }
            logger.error("" + e);

        } finally {
            System.out.println("Exit");
            try {
//                sleep(20000);
            } catch (Exception ex) {
                logger.error(" sleep err " + ex);
            }
            String args[] = null;
            //  CEIRFeatureFileParser.mai n(args); //
            System.exit(0);
        }
    }

//    public static ResultSet getFeatureFileDetails(Connection conn) {
//        Statement stmt = null;
//        ResultSet rs = null;
//        String query = null;
//        String limiter = " limit 1 ";
//        if (conn.toString().contains("oracle")) {
//            limiter = " fetch next 1 rows only ";
//        }
//        try { // and feature = '"+main_type+"'
//            query = "select * from feature_f ile_config_db where status='Init'  order by sno asc  " + limiter + " ";
//            stmt = conn.createStatement();
//            logger.info("get feature file details [" + query + "] ");
//            return rs = stmt.executeQuery(query);
//        } catch (Exception e) {
//            logger.info("" + e);
//        }
//        return rs;
//    }
//    public static String checkGraceStatus(Connection conn) {
//        String period = "";
//        String query = null;
//        ResultSet rs1 = null;
//        Statement stmt = null;
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        Date currentDate = new Date();
//        Date graceDate = null;
//        try {
//            query = "select value from sys_param where tag='GRACE_PERIOD_END_DATE'";
//            logger.info("Query(checkGraceStatus)is " + query);
//            stmt = conn.createStatement();
//            rs1 = stmt.executeQuery(query);
//            while (rs1.next()) {
//                graceDate = sdf.parse(rs1.getString("value"));
//                if (currentDate.compareTo(graceDate) > 0) {
//                    period = "post_grace";
//                } else {
//                    period = "grace";
//                }
//            }
//        } catch (Exception ex) {
//            logger.error("Error.." + ex);
//        } finally {
//            try {
//                stmt.close();
//                rs1.close();
//            } catch (SQLException e) {
//                // TODO Auto-generated catch block
//                logger.error("Error.." + e);
//            }
//
//        }
//        return period;
//    }
    public static String getOperatorTag(Connection conn, String operator) {
        String operator_tag = "";
        String query = null;
        ResultSet rs1 = null;
        Statement stmt = null;
        try {
            query = "select * from "+appdbName+".system_config_list_db where tag='OPERATORS' and interp='" + operator + "'";
            logger.info("Query is " + query);
            stmt = conn.createStatement();
            rs1 = stmt.executeQuery(query);
            while (rs1.next()) {
                operator_tag = rs1.getString("tag_id");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                stmt.close();
                rs1.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                logger.error("Error.." + e);
            }

        }
        return operator_tag;

    }

    public static void addCDRInProfileWithRule(String operator, Connection conn, ArrayList<Rule> rulelist, String operator_tag, String txn_id, String sub_feature, String usertype_name, int webActState) {
        CEIRFeatureFileFunctions ceirfunction = new CEIRFeatureFileFunctions();
        WebActionDbDao webActionDbDao = new WebActionDbDao();
        try {
            if (((sub_feature.equalsIgnoreCase("Register") || sub_feature.equalsIgnoreCase("update") || sub_feature.equalsIgnoreCase("UPLOAD"))) && !operator.equalsIgnoreCase("TYPE_APPROVED")) {
                logger.info(" NOTE.. ** NOT FOR TYPE APPROVE  ::" + sub_feature);
                new ConsignmentInsertUpdate().process(conn, operator, sub_feature, rulelist, txn_id, operator_tag, usertype_name, webActState);
            } else if (operator.equalsIgnoreCase("consignment") && (sub_feature.equalsIgnoreCase("delete") || sub_feature.equalsIgnoreCase("REJECT"))) {
                logger.info("running consignment delete/REJECT  process.");
                new ConsignmentDelete().process(conn, operator, sub_feature, rulelist, txn_id, operator_tag, usertype_name);
                webActionDbDao.updateFeatureFileStatus(conn, txn_id, 4, operator, sub_feature);
            } else if (operator.equalsIgnoreCase("consignment") && (sub_feature.equalsIgnoreCase("approve"))) {
                logger.info("running consignment approve process.");
                new ApproveConsignment().process(conn, operator, sub_feature, rulelist, txn_id, operator_tag, usertype_name);
                webActionDbDao.updateFeatureFileStatus(conn, txn_id, 4, operator, sub_feature);
            } else if (operator.equalsIgnoreCase("TYPE_APPROVED") && (sub_feature.equalsIgnoreCase("REGISTER") || sub_feature.equalsIgnoreCase("update"))) {
                logger.info("running tac register process.");
                new RegisterTac().process(conn, operator, sub_feature, rulelist, txn_id, operator_tag, usertype_name);
                webActionDbDao.updateFeatureFileStatus(conn, txn_id, 4, operator, sub_feature);
            } else if (operator.equalsIgnoreCase("TYPE_APPROVED") && (sub_feature.equalsIgnoreCase("delete"))) {
                logger.info("running tac delete process.");
                new WithdrawnTac().process(conn, operator, sub_feature, rulelist, txn_id, operator_tag, usertype_name);
                webActionDbDao.updateFeatureFileStatus(conn, txn_id, 4, operator, sub_feature);
            } else if (operator.equalsIgnoreCase("STOCK") && (sub_feature.equalsIgnoreCase("DELETE") || sub_feature.equalsIgnoreCase("reject"))) {
                logger.info("running stock delete/reject process.");
                new StockDelete().process(conn, operator, sub_feature, rulelist, txn_id, operator_tag, usertype_name, "");
                webActionDbDao.updateFeatureFileStatus(conn, txn_id, 4, operator, sub_feature);
            } else {
                logger.info("Skipping the process.");
                webActionDbDao.updateFeatureFileStatus(conn, txn_id, 4, operator, sub_feature);
            }

        } catch (Exception e) {
            logger.error("addCDRInProfileWithRule " + e);
            e.printStackTrace();
        } finally {

        }
    }

//    public static String getErrorFilePath(Connection conn) {
//        String errorFilePath = "";
//        String query = null;
//        ResultSet rs1 = null;
//        Statement stmt = null;
//
//        try {
//            query = "select * from sys_param where tag='system_error_filepath'";
//            logger.info("Query (getErrorFilePath)  " + query);
//            stmt = conn.createStatement();
//            rs1 = stmt.executeQuery(query);
//            while (rs1.next()) {
//                errorFilePath = rs1.getString("value");
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        } finally {
//            try {
//                stmt.close();
//                rs1.close();
//            } catch (SQLException e) {
//                logger.error("Error.." + e);
//            }
//
//        }
//        return errorFilePath;
//
//    }
    public static int getCustomData(Connection conn, String txn_id) {
        String query = null;
        Statement stmt = null;
        ResultSet rs1 = null;
        String rslt = "";
        int rst = 0;
        query = " select user_type from  "+appdbName+".stock_mgmt   where txn_id =  '" + txn_id + "'  ";
        logger.info("getCustomData query .." + query);
        try {
            stmt = conn.createStatement();
            rs1 = stmt.executeQuery(query);
            while (rs1.next()) {
                rslt = rs1.getString(1);
            }

            if (rslt.equalsIgnoreCase("Custom")) {
                logger.info("IT is  Custom");
                rst = 1;
            }
//

        } catch (SQLException e) {
            logger.error("Error.." + e);
        } finally {
            try {
                rs1.close();
                stmt.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                logger.error("Error.." + e);
            }

        }
        return rst;
    }

    public static void updateRawData(Connection conn, String operator, String id, String status) {
        String query = null;
        Statement stmt = null;
        query = "update "+appdbName+"." + operator + "_raw" + " set status='" + status + "' where sno='" + id + "'";
        logger.info("updateRawData query .." + query);
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
            conn.commit();
        } catch (SQLException e) {
            logger.error("Error.." + e);
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                logger.error("Error.." + e);
            }
        }

    }

    public static ArrayList<Rule> getRuleDetails(String operator, Connection conn, String operator_tag, String period, String feature, String usertype_name) {
        ArrayList rule_details = new ArrayList<Rule>();
        String query = null;
        ResultSet rs1 = null;
        Statement stmt = null;
        try {
            query = "select a.id as rule_id,a.name as rule_name,b.output as output,b.grace_action, b.post_grace_action, b.failed_rule_action_grace, b.failed_rule_action_post_grace from "+appdbName+".rule_engine a, "+appdbName+".rule_engine_mapping b where  a.name=b.name  and a.state='Enabled' and b.feature='"
                    + operator + "' and b.user_type='" + usertype_name + "'  and  b." + period + "_action !='NA'       order by b.rule_order asc";

            logger.info("Query is  (getRuleDetails) " + query);
            stmt = conn.createStatement();
            rs1 = stmt.executeQuery(query);
            if (!rs1.isBeforeFirst()) {
                query = "select a.id as rule_id,a.name as rule_name,b.output as output,b.grace_action, b.post_grace_action, b.failed_rule_action_grace, b.failed_rule_action_post_grace from "+appdbName+".rule_engine a, "+appdbName+".rule_engine_mapping b where  a.name=b.name  and a.state='Enabled' and b.feature='"
                        + operator + "' and b.user_type='default' order by b.rule_order asc";
                stmt = conn.createStatement();
                rs1 = stmt.executeQuery(query);
            }
            while (rs1.next()) {
//                if (rs1.getString("rule_name").equalsIgnoreCase("IMEI_LENGTH")) {
//                    if (operator_tag.equalsIgnoreCase("GSM")) {
//                        // Rule rule = new
//                        // Rule(rs1.getString("rule_name"),rs1.getString("output"),rs1.getString("rule_id"),period,
//                        // rs1.getString(period+"_action"));
//                        Rule rule = new Rule(rs1.getString("rule_name"), rs1.getString("output"),
//                                rs1.getString("rule_id"), period, rs1.getString(period + "_action"),
//                                rs1.getString("failed_rule_action_" + period));
//                        rule_details.add(rule);
//                    }
//                } else
                {
                    Rule rule = new Rule(rs1.getString("rule_name"), rs1.getString("output"), rs1.getString("rule_id"),
                            period, rs1.getString(period + "_action"), rs1.getString("failed_rule_action_" + period));
                    rule_details.add(rule);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                stmt.close();
                rs1.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                logger.error("Error.getRuleDetails ." + e);
            }
        }
        return rule_details;
    }

//     public static ResultSet operatorDetails(Connection conn, String operator) {
//          Statement stmt = null;
//          ResultSet rs = null;
//          String query = null;
//          try {
//               query = "select * from re p_schedule_config_db where operator='" + operator + "'";
//               stmt = conn.createStatement();
//               return rs = stmt.executeQuery(query);
//          } catch (Exception e) {
//               logger.info("" + e);
//          }
//          return rs;
//     }
    public static void updateLastStatuSno(Connection conn, String operator, int id, int limit) {
        String query = null;
        Statement stmt = null;
        query = "update "+appdbName+"." + operator + "_raw" + " set status='Start' where sno>'" + id + "'"; //
        logger.info(query);
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
            conn.commit();
        } catch (SQLException e) {
            logger.error("Error.." + e);
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                logger.error("Error.." + e);
            }
        }
    }

//     public static void updateRawLastSno(Connection conn, String operator, int sno) {
//          String query = null;
//          Statement stmt = null;
//          query = "update re p_schedule_config_db set last_upload_sno=" + sno + " where operator='" + operator + "'";
//           try {
//               stmt = conn.createStatement();
//               stmt.executeUpdate(query);
//               conn.commit();
//          } catch (SQLException e) {
//               logger.error("Error.." + e);
//          } finally {
//               try {
//                    stmt.close();
//               } catch (SQLException e) {
//                    // TODO Auto-generated catch block
//                    logger.error("Error.." + e);
//               }
//          }
//     }
    public static int imeiCountfromRawTable(Connection conn, String txn_id, String operator) {
        int rsslt = 0;
        String query = null;
        Statement stmt = null;
        query = "select count(*) as cnt from  "+appdbName+"." + operator + "_raw  where txn_id ='" + txn_id + "'  ";
        logger.info(" select imeiCountRawTable .. " + query);
        try {
            ResultSet rs = null;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                rsslt = rs.getInt("cnt");
            }
            conn.commit();
        } catch (SQLException e) {
            logger.error("Error.." + e);
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                logger.error("Error.." + e);
            }
        }

        return rsslt;
    }

}
