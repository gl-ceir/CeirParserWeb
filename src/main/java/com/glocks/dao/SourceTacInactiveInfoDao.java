package com.glocks.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static com.glocks.parser.MainController.appdbName;

public class SourceTacInactiveInfoDao {

    static Logger logger = LogManager.getLogger(SourceTacInactiveInfoDao.class);
    static String GENERIC_DATE_FORMAT = "dd-MM-yyyy";

    public int deleteFromSourceTacInactiveInfo(Connection conn, String txnId) {
        String query = "";
        int executeStatus = 0;

        query = "delete from "+appdbName+".source_tac_inactive_info where txn_id='" + txnId + "'";
        logger.info("delete source_tac_inactive_info [" + query + "]");
        try( Statement stmt = conn.createStatement(); ) {
            executeStatus = stmt.executeUpdate(query);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return executeStatus;
    }

    public void insertSourceTac(Connection conn, String tac, String txnFile, Long tacCount, String dbName) {
        logger.info("tacCount " + tacCount);
        String query = "insert into "+appdbName+"." + dbName + "(tac , txn_id, record_count ) "
                + "  values('" + tac + "', '" + txnFile + "', " + tacCount + " )";
        try( Statement stmt = conn.createStatement(); ){
            logger.info(" " + query);
            stmt.executeUpdate(query);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
