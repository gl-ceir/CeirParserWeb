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

/**
 *
 * @author sachin
 */
public class SysConfigurationDao {

    static Logger logger = LogManager.getLogger(SysConfigurationDao.class);

    public String getTagValue(Connection conn, String tag_type) {
        String file_path = "";
        String query = null;
        ResultSet rs = null;
        Statement stmt = null;
        try {
            query = "select value from "+appdbName+".sys_param where tag='" + tag_type + "'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            logger.info("to get configuration" + query);
            while (rs.next()) {
                file_path = rs.getString("value");
                logger.info("in function file path " + file_path);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("e " + ex);
        } finally {
            try {
                rs.close();
                stmt.close();
            } catch (Exception ex) {
                logger.error("e " + ex);
            }

        }
        return file_path;

    }
}
