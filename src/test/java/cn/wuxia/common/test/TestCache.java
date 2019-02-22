/*
* Created on :2017年8月3日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.common.test;

import cn.wuxia.common.cached.memcached.MemcachedUtils;
import cn.wuxia.common.cached.memcached.XMemcachedClient;
import cn.wuxia.common.util.DateUtil;

import java.util.Date;

public class TestCache {
    
    public static void main(String[] args) {
//        String key = "access_token";
//        key += "wx8c625ac17367e886";
//        System.out.println(key);
//        System.out.println(MemcachedUtils.shaKey(key));
//
//        XMemcachedClient mc = new XMemcachedClient();
//        mc.init("127.0.0.1:11211");
//        mc.add("abc", "10", 20);
//        //mc.incr("abc", 1, 1L);
//        boolean t = true;
//        while(t){
//            System.out.println(DateUtil.format(new Date(), "HH:mm:ss")+"="+mc.get("abc"));
//            if(mc.get("abc") == null){
//                t = false;
//            }
//        }
//        //        TestMemcachedServer ms = new TestMemcachedServer();
////                ms.start("127.0.0.1", 11211);
//        // String key = "hello_" + 1;
//        //        mc.set("abc", "1323", 60 * 60 * 1);
//        //        String result = mc.get("abc");
//        //        logger.debug("1:" + result);
//        //        mc.delete("abcd");
//        //        result = mc.get("abc");
//        //        logger.debug("2:" + result);
//        //        mc.delete(MemcachedUtils.shaKey("access_token"));
//
//        //        mc.set(MemcachedUtils.shaKey("access_token"), "aafsdfsdfsdfsdff");
//        //        System.out.println("1111111==="+mc.get(MemcachedUtils.shaKey("access_token")));
//         key = "classcn.daoming.basic.api.open.service.impl.AuthorizerAccountServiceImpl.findAuthorizerByAppidwxdc282971b0b8af0a";
//        //System.out.println("======"+(String)mc.get(key, "1DayData"));
//        //mc.memcachedClient.endWithNamespace();
//
//        // mc.set(key, "999999999999999999999");
//        // System.out.println("dm ********************hello_" + mc.get(key));
//        // XMemcachedClient mc1 = new XMemcachedClient();
//        // mc1.init(ad1);
//        // XMemcachedClient mc2 = new XMemcachedClient();
//        // mc2.init(ad2);
//        // XMemcachedClient mc3 = new XMemcachedClient(ad3);
//        // System.out.println(ad1+" ********************hello_" + mc1.get(key));
//        // System.out.println(ad2+" ********************hello_" + mc2.get(key));
//        // System.out.println(ad3+" ********************hello_" + mc3.get(key));
//        // String addrs = ad3 + "," + ad2;
//        // int[] a = new int[2];
//        // a[0] = 1;
//        // a[1] = 2;
//        // MemcachedClientBuilder builder = new XMemcachedClientBuilder(
//        // AddrUtil.getAddressMap(StringUtil.join(addrs, " ")), a);
//        try {
//            // MemcachedClient memcachedClient = builder.build();
//            // System.out.println(builder.isFailureMode());
//            // builder.setFailureMode(true);
//
//            // memcachedClient.set("key1", 0, "123");
//            // memcachedClient.set("key2", 0, "456");
//            // System.out.println(memcachedClient.getStateListeners());
//            // System.out.println(memcachedClient.getStats());
//            // System.out.println(memcachedClient.getServersDescription());
//            // close memcached client
//            // System.out.println("======================"+memcachedClient.get("key1"));
//            // System.out.println("======================"+memcachedClient.get("key2"));
//            // memcachedClient.shutdown();
//            mc.shutdown();
//            // mc2.memcachedClient.shutdown();
//            // mc3.memcachedClient.shutdown();
//        } catch (Exception e) {
//            System.err.println("Shutdown MemcachedClient fail");
//            e.printStackTrace();
//        }



    }


}
