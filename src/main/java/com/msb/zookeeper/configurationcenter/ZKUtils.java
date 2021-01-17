package com.msb.zookeeper.configurationcenter;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * zookeeper连接
 * @author 程治玮
 * @since 2021/1/17 1:05 下午
 */
public class ZKUtils {

    private static ZooKeeper zk;
    private static String address = "192.168.1.82:2181,192.168.1.82:2182,192.168.1.82:2183,192.168.1.82:2184/testConf"; //testConf作为zookeeper目录的根，可以用于区分项目组
    private static DefaultWatch watch = new DefaultWatch();
    private static CountDownLatch init = new CountDownLatch(1);

    public static ZooKeeper getZK() {
        try {
            zk = new ZooKeeper(address, 1000, watch);
            watch.setCc(init);
            init.await();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zk;
    }
}
