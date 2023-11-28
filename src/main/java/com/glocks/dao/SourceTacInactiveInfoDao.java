package com.glocks.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SourceTacInactiveInfoDao {

    static Logger logger = LogManager.getLogger(SourceTacInactiveInfoDao.class);
    static String GENERIC_DATE_FORMAT = "dd-MM-yyyy";

    public int deleteFromSourceTacInactiveInfo(Connection conn, String txnId) {
        String query = "";
        Statement stmt = null;
        int executeStatus = 0;

        query = "delete from source_tac_inactive_info where txn_id='" + txnId + "'";
        logger.info("delete source_tac_inactive_info [" + query + "]");
        // System.out.println("delete device_importer_db ["+query+"]");

        try {
            stmt = conn.createStatement();
            executeStatus = stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return executeStatus;
    }

    public void insertSourceTac(Connection conn, String tac, String txnFile, Long tacCount, String dbName) {
        Statement stmt = null;
        logger.info("tacCount " + tacCount);
        String raw_query = "insert into " + dbName + "(tac , TXN_ID, RECORD_COUNT   ) "
                + "  values('" + tac + "', '" + txnFile + "', " + tacCount + " )";
        try {
            logger.info(" " + raw_query);
            stmt = conn.createStatement();
            stmt.executeUpdate(raw_query);
        } catch (Exception e) {
            logger.warn("  " + e);
        } finally {
            try {
                stmt.close();
            } catch (SQLException ex) {
                org.apache.logging.log4j.LogManager.getLogger(SourceTacInactiveInfoDao.class.getName());
            }
        }
    }
}
