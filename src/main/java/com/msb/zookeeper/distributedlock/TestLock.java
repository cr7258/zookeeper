package com.msb.zookeeper.distributedlock;

import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 分布式锁
 * @author 程治玮
 * @since 2021/1/17 3:56 下午
 */
public class TestLock {

    ZooKeeper zk;

    @Before
    public void conn(){
        zk = ZKUtils.getZK();
    }

    @After
    public void close(){
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void lock(){
        for (int i = 0; i < 10; i++) {
            new Thread(){
                @Override
                public void run() {
                    //每一个线程
                    WatchCallBack watchCallBack = new WatchCallBack();
                    watchCallBack.setZk(zk);
                    String threadName = Thread.currentThread().getName();
                    watchCallBack.setThreadName(threadName);
                    //抢锁
                    watchCallBack.tryLock();
                    //干活
                    System.out.println("干活中.....");
                    //有可能前面一个人干完活释放了锁 后面一个人还没来得及监控，就会导致卡住
                    //可以如下增加睡眠时间，或者让前面一个添加数据稍微慢一点
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

                    //释放锁
                    watchCallBack.unLock();
                }
            }.start();
        }
        while (true){

        }
    }
}
