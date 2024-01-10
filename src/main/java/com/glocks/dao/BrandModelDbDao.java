/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.glocks.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static com.glocks.parser.MainController.appdbName;




/**
 *
 * @author sachin
 */
public class BrandModelDbDao {

    Logger logger = LogManager.getLogger(BrandModelDbDao.class);

    public void updateModelBrandNameByTxnId(Connection conn, String txnId, String TableName) { // need to check
        String query = " select tac from  "+appdbName+"." + TableName + " where  txn_id =   '" + txnId + "'   ";
        try( Statement stmt = conn.createStatement();ResultSet rs = stmt.executeQuery(query); ) {
            while (rs.next()) {
                String updtQry = " update  "+appdbName+"." + TableName + " set model_name = (select  model_name  from "+appdbName+".gsma_tac_details where tac = '" + rs.getString("tac") + "'  ) "
                        + " ,   brand_name = ( select brand_name  from "+appdbName+".gsma_tac_details where tac = '" + rs.getString("tac") + "' )    where tac =   '" + rs.getString("tac") + "'  ";
                logger.info(updtQry);
                stmt.executeUpdate(updtQry);
            }
        }catch (Exception e) {
            logger.info("Error" + e);
        }

    }

}
