
package com.dundun;

import com.dundun.api.ApiInvocation;
import com.dundun.api.ApiResultResolver;

/**
 * Created by dunxuliu on 2015/8/24.
 */
public class TestApiResultResolver implements ApiResultResolver {
    @Override
    public Object resolveResult(ApiInvocation apiInvocation, Object formatedResult, Class<?> requiredType) {
        return null;
    }
}
