package com.glocks.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.glocks.pojo.UserWithProfile;
import static com.glocks.parser.MainController.appdbName;





public class UserWithProfileDao {
	static Logger logger = LogManager.getLogger(UserWithProfileDao.class);
	static String GENERIC_DATE_FORMAT = "dd-MM-yyyy";

	public UserWithProfile getUserWithProfileById(Connection conn, Long userId) {
		Statement stmt = null;
		ResultSet rs = null;
		String query = null;

		try{
			query = "select "+appdbName+".users.id as id, "+appdbName+".user_profile.first_name as first_name, "
					+ ""+appdbName+".usertype.usertype_name as usertype_name "
					+ "from "+appdbName+".users "
					+ "inner join "+appdbName+".user_profile on "+appdbName+".users.id="+appdbName+".user_profile.userid "
					+ "inner join "+appdbName+".usertype on "+appdbName+".users.usertype_id="+appdbName+".usertype.id " 
					+ "where "+appdbName+".users.id=" + userId;

			logger.info("Query ["+query+"]"); 
			stmt  = conn.createStatement();
			rs = stmt.executeQuery(query);

			if(rs.next()){
				return new UserWithProfile(rs.getLong("id"), rs.getString("first_name"), rs.getString("usertype_name"));
			}
		}
		catch(Exception e){
			logger.info("Exception in getFeatureMapping"+e);
			e.printStackTrace();
		}
		finally{
			try {
				if(rs!=null)
					rs.close();
				if(stmt!=null)
					stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}

		return null;
	}
}
