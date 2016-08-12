
package com.dundun.model;

/**
 *  分库路由
 * Created by dunxuliu on 2015/8/6.
 */
public class Route {

    /**
     * 正则表达式
     */
    private String expression;

    /**
     * 数据源名称
     */
    private String instance;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }
}
