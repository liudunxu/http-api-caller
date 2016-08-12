
package com.dundun;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dundun.model.GenerateResult;

import com.dundun.model.CommonClass;

/**
 * Created by dunxuliu on 2015/8/24.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class TestMain {

    @Autowired
    HttpService httpService;



    @Test
    public void testaa() throws InterruptedException {
        for (int i = 0; i < 1; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<Map<String, Object>> db = httpService.getDbInstance(1);
                    System.out.println(db.size());
                    System.out.println(httpService.getRawExample());
                }
            }).start();
        }
        Thread.sleep(2000L);
    }

    @Test
    public void testGenera() {
        GenerateResult<Integer> result = httpService.getGener();
        System.out.println(result.getErrorCode());
    }

    @Test
    public void testCommon() {
        CommonClass result = httpService.getCommon();
        System.out.println(result.getErrorCode());
    }

    @Test
    public void testIsSignIn() {
        System.out.println(httpService.getRawExample());
    }
}
