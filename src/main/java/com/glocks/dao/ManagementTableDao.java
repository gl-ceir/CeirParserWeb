package com.glocks.dao;

import com.glocks.pojo.ManagementDb;
import com.glocks.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.glocks.parser.MainController.appdbName;

public class ManagementTableDao {

     static Logger logger = LogManager.getLogger(ManagementTableDao.class);
     static String GENERIC_DATE_FORMAT = "dd-MM-yyyy";

     public List<ManagementDb> getManagementDbByTxnId(Connection conn, String txnId, String managementTable) {
          List<ManagementDb> managementDbs = new LinkedList<>();
               String    query = "select id, created_on, modified_on, device_type, device_id_type, "
                       + "mul_sim_status, sno_of_device, imei_esn_meid, DEVICE_LAUNCH_DATE as launch_date, "
                       + "device_status, user_id, txn_id, period, feature_name "
                       + "from   "+appdbName+"." +  managementTable
                       + " where txn_id='" + txnId + "'";

               logger.info("Query [" + query + "]");
               try( Statement stmt = conn.createStatement();ResultSet rs = stmt.executeQuery(query); ){
                    while (rs.next()) {
                    managementDbs.add(new ManagementDb(rs.getLong("id"), 0, rs.getString("created_on"),
                            rs.getString("modified_on"), rs.getString("device_type"), rs.getString("device_id_type"),
                            rs.getString("mul_sim_status"), rs.getString("sno_of_device"), rs.getString("imei_esn_meid"),
                            rs.getString("launch_date"), rs.getLong("user_id"), rs.getString("txn_id"),
                            rs.getString("device_status"), rs.getString("period"), rs.getString("feature_name")));
               }
          } catch (Exception e) {
               logger.error(e.getMessage(), e);
          }

          return managementDbs;
     }

     public int deleteDevicesFromManagementDb(Connection conn, String txnId, String managementTable) {
          int executeStatus = 0;

               String qry = "select imei_esn_meid from "+appdbName+"." + managementTable + " where txn_id  = '" + txnId + "' ";
               logger.info("" + qry);
               try( Statement stmt = conn.createStatement();ResultSet rs = stmt.executeQuery(qry); ){
               while (rs.next()) {
                    new DeviceDbDao().deleteDevicesFromDeviceDb(conn, rs.getString(1));
               }
          } catch (Exception e) {
               logger.error(e);
          }

      String  query = "delete from "+appdbName+"." + managementTable + " where txn_id='" + txnId + "'";
          logger.info("Query [" + query + "]");
          try( Statement stmt = conn.createStatement() ){
               executeStatus = stmt.executeUpdate(query);
          } catch (SQLException e) {
               logger.error(e);
          }
          return executeStatus;
     }

     public void updateMgmtDeleteFlag(Connection conn, String tableName, String txnId, int deleteFlag) {
          boolean isOracle = conn.toString().contains("oracle");
          String dateFunction = Util.defaultDate(isOracle);

          String query = "update "+appdbName+"." + tableName + " set delete_flag=? where txn_id=?";
          logger.info("Query [" + query + " ]");
          // System.out.println("Query [" + query + " ]");

          PreparedStatement preparedStatement = null;

          try {
               preparedStatement = conn.prepareStatement(query);

               preparedStatement.setInt(1, deleteFlag);
               preparedStatement.setString(2, txnId);

               logger.info("Query " + preparedStatement);
               preparedStatement.execute();
               logger.info("Update delete flag in  " + tableName + " succesfully.");
          } catch (SQLException e) {
               logger.error(e.getMessage(), e);

          } finally {
               try {
                    if (Objects.nonNull(preparedStatement)) {
                         preparedStatement.close();
                    }
               } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                    logger.error(e);
               }
          }
     }

}

