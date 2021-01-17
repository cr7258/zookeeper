package com.msb.zookeeper.configurationcenter;

import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * 配置中心
 * @author 程治玮
 * @since 2021/1/17 12:56 下午
 */
public class TestConfig {

    ZooKeeper zk;

    @Before
    public void conn() {
        zk = ZKUtils.getZK();
    }

    @After
    public void close() {
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getConf() {
        //判断目录是否存在
        //这里的/AppConf代表/testConf/AppConf,因为之前连接的时候添加的前缀
        WatchCallBack watchCallBack = new WatchCallBack();
        watchCallBack.setZk(zk);
        MyConf myConf = new MyConf();
        watchCallBack.setConf(myConf);
        //场景一：节点不存在
        //场景二：节点存在
        watchCallBack.aWait();

        while (true) {
            if (myConf.getConf().equals("")) {
                System.out.println("conf丢失....");
                watchCallBack.aWait();
            } else {
                System.out.println(myConf.getConf());
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
