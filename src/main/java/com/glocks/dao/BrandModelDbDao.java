/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.glocks.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static com.glocks.parser.MainController.appdbName;




/**
 *
 * @author sachin
 */
public class BrandModelDbDao {

    Logger logger = LogManager.getLogger(BrandModelDbDao.class);

    public void updateModelBrandNameByTxnId(Connection conn, String txnId, String TableName) {

        String query = " select tac from  "+appdbName+"." + TableName + " where  txn_id =   '" + txnId + "'   ";
        Statement stmt = null;
        Statement stmt2 = null;
        logger.info("tac       ...[" + query + "]");
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            stmt2 = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                String updtQry = " update  "+appdbName+"." + TableName + " set model_name = (select  MODEL_NAME_NEW  from "+appdbName+".gsma_tac_db where device_id = '" + rs.getString("tac") + "'  ) "
                        + " ,   brand_name = ( select BRAND_NAME_NEW  from "+appdbName+".gsma_tac_db where device_id = '" + rs.getString("tac") + "' )    where tac =   '" + rs.getString("tac") + "'  ";
                logger.info(updtQry);
                stmt2.executeUpdate(updtQry);
            }
        } catch (Exception e) {
            logger.info("Error" + e);
        } finally {
            try {
                rs.close();
                stmt.close();
                stmt2.close();
                conn.commit();

            } catch (Exception e) {
                logger.error("" + e);
            }
        }

    }

}
