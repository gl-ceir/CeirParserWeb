/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.glocks.dao;

import static com.glocks.parser.MainController.appdbName;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebActionDbDao {

    private static final Logger logger = LogManager.getLogger(WebActionDbDao.class);
    static StackTraceElement l = new Exception().getStackTrace()[0];

    public void updateFeatureFileStatus(Connection conn, String txn_id, int status, String feature, String subfeature) {
        Statement stmt = null;
        try {
            String query = "update  "+appdbName+".web_action_db set state=" + status + "  where    txn_id='" + txn_id + "' and feature='" + feature
                    + "' and sub_feature='" + subfeature + "' ";
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
        } catch (Exception e) {
            logger.info("errror" + e);
        } finally {
            try {
                stmt.close();
                conn.commit();
            } catch (Exception e) {
                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            }
        }

    }

    public ResultSet getFileDetails(Connection conn, int state, String feature) {
        String featureStmt = "";
        if (feature != null) {
            featureStmt = " and feature =  '" + feature + "' ";
        }
        Statement stmt = null;
        ResultSet rs = null;
        String query = null;
        String limiter = " limit 1 ";
        if (conn.toString().contains("oracle")) {
            limiter = " fetch next 1 rows only ";
        }
        String stater = "";
        if (state == 0) {
            stater = " ( state  = 0  or  state  = 1  )  ";
        } else {
            stater = "  ( state  = 2   or  state  = 3 ) ";
        }
        try {                               //where state =  " + state + "
            query = "select * from "+appdbName+".web_action_db where " + stater + featureStmt + " and retry_count < 20  order by state desc , id asc " + limiter + "  ";
            logger.info("Query to get File Details [" + query + "]");
            stmt = conn.createStatement();
            return rs = stmt.executeQuery(query);
        } catch (Exception e) {
            logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
            // System.out.println("" + e);
        }
        return rs;
    }

//    public void updateFeatureFileStatus(Connection conn, String txn_id, int status, String feature, String subfeature) {
//        Statement stmt = null;
//        try {
//            String query = "update "+appdbName+".web_action_db set state=" + status + "  where    txn_id='" + txn_id + "' and feature='" + feature
//                    + "' and sub_feature='" + subfeature + "' ";
//            stmt = conn.createStatement();
//            stmt.executeUpdate(query);
//        } catch (Exception e) {
//            logger.info("errror" + e);
//        } finally {
//            try {
//                stmt.close();
//                conn.commit();
//            } catch (Exception e) {
//                logger.error("" + l.getClassName() + "/" + l.getMethodName() + ":" + l.getLineNumber() + e);
//            }
//        }
//
//    }
}
