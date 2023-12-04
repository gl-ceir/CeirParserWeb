package com.glocks.db;

import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.stereotype.Repository;
//
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.stereotype.Repository;

@Repository
public class ConnectionConfiguration {
	
	@PersistenceContext
    private EntityManager em;
	
	public Connection getConnection() {
		EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) em.getEntityManagerFactory();
	    try {
   Connection conn = info.getDataSource().getConnection();
            conn.setAutoCommit(false);
            return conn;                        
		} catch (SQLException e) {
			return null;
		}
	}
	
}
