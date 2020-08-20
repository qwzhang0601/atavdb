package org.atavdb.service.util;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.atavdb.exception.DatabaseException;
import org.springframework.stereotype.Service;

/**
 *
 * @author nick
 */
@Service
public class DBManager {

    private PoolingDataSource<PoolableConnection> dataSource;
    private Connection connection;

    private final String dbDriver = "com.mysql.cj.jdbc.Driver";
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    
    public DBManager() {
        init();
    }

    public void init() {
        try {
            initDataSource();

            initConnection();
        } catch (Exception ex) {
            throw new DatabaseException(ex);
        }
    }

    private void initDataFromSystemConfig() {
        // server config from tomcat $CATALINA_HOME/bin/setenv.sh
        dbUrl = System.getenv("DB_URL");
        dbUser = System.getenv("DB_USER");
        dbPassword = System.getenv("DB_PASSWORD");

        // local config without tomcat
//        dbUrl = "jdbc:mysql://localhost:3306/WalDB?serverTimezone=UTC";
//        dbUser = "test";
//        dbPassword = "test";
    }

    private void initDataSource() throws ClassNotFoundException {
        if (dataSource == null) {
            Class.forName(dbDriver);

            initDataFromSystemConfig();

            ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                    dbUrl, dbUser, dbPassword);
            PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
                    connectionFactory, null);

            GenericObjectPoolConfig<PoolableConnection> config = new GenericObjectPoolConfig<>();
            config.setMaxTotal(100);
            config.setMaxIdle(100);
            config.setMinIdle(10);

            ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(
                    poolableConnectionFactory, config);
            poolableConnectionFactory.setPool(connectionPool);

            dataSource = new PoolingDataSource<>(connectionPool);
        }
    }

    private void initConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = dataSource.getConnection();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
