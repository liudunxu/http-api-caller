
package com.dundun.model;

/**
 * 逻辑数据库属性（可能对应多个数据库连接）
 * Created by dunxuliu on 2015/8/6.
 */
public class DbInstance {

    // 单节点类型
    public static final String SINGLE_DB_TYPE = "singler";
    // 路由节点类型
    public static final String ROUTER_DB_TYPE = "router";

    /**
     * 数据库类型
     * 默认为singler
     */
    private String type = SINGLE_DB_TYPE;

    /**
     * 连接数据库名
     */
    private String catalog;

    /**
     * 写库
     */
    private DbServer writeServer;

    /**
     * 读库
     */
    private DbServer[] readServers;

    /**
     * 分库路由
     */
    private Route[] routes;

    /**
     * 最后更新事件
     */
    private Long lastUpdateDate;

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DbServer getWriteServer() {
        return writeServer;
    }

    public void setWriteServer(DbServer writeServer) {
        this.writeServer = writeServer;
    }

    public DbServer[] getReadServers() {
        return readServers;
    }

    public void setReadServers(DbServer[] readServers) {
        this.readServers = readServers;
    }

    public Route[] getRoutes() {
        return routes;
    }

    public void setRoutes(Route[] routes) {
        this.routes = routes;
    }

    public Long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }
}

