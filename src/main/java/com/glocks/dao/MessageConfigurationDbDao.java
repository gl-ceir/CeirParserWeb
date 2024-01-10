package com.glocks.dao;

import com.glocks.pojo.MessageConfigurationDb;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;

public class MessageConfigurationDbDao {
	static Logger logger = LogManager.getLogger(MessageConfigurationDbDao.class);
	static String GENERIC_DATE_FORMAT = "dd-MM-yyyy";

    public Optional<MessageConfigurationDb> getMessageDbTag(Connection conn, String tag, String appdbName) {
		String query = "select id, description, tag, value, channel, active, subject "
				+ "from " + appdbName + ".msg_cfg where tag='" + tag + "'";
		logger.info("Query ["+query+"]");
		 try( Statement stmt = conn.createStatement();ResultSet rs = stmt.executeQuery(query); ){
			if(rs.next()){
				return Optional.of(new MessageConfigurationDb(rs.getLong("id"), rs.getString("tag"), 
						rs.getString("value"), rs.getString("description"), rs.getInt("channel"), 
						rs.getString("subject")));
			}
		}
		catch(Exception e){
			logger.info(e.getMessage(), e);
		}
		return Optional.empty();
	}
}
