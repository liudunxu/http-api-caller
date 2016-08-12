
package com.dundun.model;

import java.io.Serializable;

/**
 * 物理数据库连接属性
 * Created by dunxuliu on 2015/8/6.
 */
public class DbServer implements Serializable{

    // mysql数据库类型
    public static final String MYSQL_DB_TYPE = "mysql";
    // mysql数据库驱动
    public static final String CLASSNAME_MYSQL = "com.mysql.jdbc.Driver";

    /**
     * 物理数据库类型
     * 默认为mysql
     */
    private String type = MYSQL_DB_TYPE;

    /**
     * 数据库ip
     */
    private String host;

    /**
     * 数据库端口号
     */
    private Integer port;

    /**
     * 数据库名
     */
    private String database;

    /**
     * 用户名
     */
    private String user;

    /**
     * 密码
     */
    private String password;

    /**
     * 连接编码
     */
    private String charset;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DbServer dbServer = (DbServer) o;

        if (type != null ? !type.equals(dbServer.type) : dbServer.type != null) {
            return false;
        }
        if (host != null ? !host.equals(dbServer.host) : dbServer.host != null) {
            return false;
        }
        if (port != null ? !port.equals(dbServer.port) : dbServer.port != null) {
            return false;
        }
        if (database != null ? !database.equals(dbServer.database) : dbServer.database != null) {
            return false;
        }
        if (user != null ? !user.equals(dbServer.user) : dbServer.user != null) {
            return false;
        }
        if (password != null ? !password.equals(dbServer.password) : dbServer.password != null) {
            return false;
        }
        return !(charset != null ? !charset.equals(dbServer.charset) : dbServer.charset != null);

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (port != null ? port.hashCode() : 0);
        result = 31 * result + (database != null ? database.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (charset != null ? charset.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DbServer{" +
                   "type='" + type + '\'' +
                   ", host='" + host + '\'' +
                   ", port=" + port +
                   ", database='" + database + '\'' +
                   ", user='" + user + '\'' +
                   ", password='" + password + '\'' +
                   ", charset='" + charset + '\'' +
                   '}';
    }
}
