
package com.dundun;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.dundun.api.annotation.ApiInfo;
import com.dundun.api.annotation.ApiParam;
import com.dundun.api.annotation.ApiRateLimiter;
import com.dundun.api.parser.ReqMethod;
import com.dundun.model.CommonClass;
import com.dundun.model.GenerateResult;

/**
 * Created by dunxuliu on 2015/8/24.
 */
@Service
public interface HttpService {

    /**
    * @param envId
     * @return
     */
    @ApiRateLimiter(10)
    @ApiInfo(url = "/api/config/list", resultPath = "data/list",
                validatePath = "errorCode", retryCount = 3,timeout = 2000)
    List<Map<String,Object>> getDbInstance(@ApiParam(paramKey = "envId") int envId);

    @ApiInfo(url = "/api/config/list", resultPath = "data.signIn",
                validatePath = "errorCode", retryCount = 3,timeout = 2000)
    Integer getIsSignin(@ApiParam(paramKey = "envId") int envId);

    /**
     * 获取数字
     * @return
     */
    @ApiInfo(url="/api/account/session",method = ReqMethod.GET,resultPath = "errorMsg")
    String getRawExample();

    /**
     * 获取数字
     * @return
     */
    @ApiInfo(url="/api/account/session",method = ReqMethod.GET)
    GenerateResult<Integer> getGener();

    /**
     * 获取数字
     * @return
     */
    @ApiInfo(url="/api/account/session",method = ReqMethod.GET)
    CommonClass getCommon();
}
