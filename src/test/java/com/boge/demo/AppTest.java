package com.boge.demo;

import static org.junit.Assert.assertTrue;

import com.boge.demo.commons.DateUtil;
import com.boge.demo.commons.RedisUtils;
import com.boge.demo.controller.BaseController;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigInteger;
import java.util.Date;


/**
 * Unit test for simple App.
 */
public class AppTest {


    /**
     * Rigorous Test :-)
     */
    public final static Logger logger = Logger.getLogger(AppTest.class);
    @Autowired
    private RedisUtils redisUtils;

    @Test
    public void shouldAnswerWithTrue() {
        // assertTrue( true );
        // logger.info(2<<2);
        // String[] gg={"j","l"};

        // String rrr ="uuuuuuuuuu";
        // logger.info(gg[0]);
        String num1 = "9999999999999";
        String num2 = "99999999999";


    }


    public BigInteger doCount(BigInteger num3, BigInteger num4, String docalculate) {
        BigInteger result = new BigInteger("0");

        /*Integer num3 = Integer.parseInt(num1);
        Integer num4 = Integer.parseInt(num2);*/


        if (docalculate.equals("+")) {
            result = num3.add(num4);
        }
        if (docalculate.equals("*")) {
            result = num3.multiply(num4);
        }
        if (docalculate.equals("/")) {
            result = num3.divide(num4);
        }
        if (docalculate.equals("-")) {
            result = num3.subtract(num4);
        }

        return result;
    }

    @Test
    public void testDateUtil1() {
        // logger.info(DateUtil.getDateSpace("2019-01-02 18:23:22","2019-01-05 18:23:22"));
        // logger.info(DateUtil.getDaysSpace());

        //  redisUtils.set("promote_item_stock4",2391);
        //  redisUtils.get("promote_item_stock4");

        //redisUtils.del("item_3");
    }

}
