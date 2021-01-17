package com.msb.zookeeper.configurationcenter;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/**
 * 监听回调
 * @author 程治玮
 * @since 2021/1/17 1:25 下午
 */
public class WatchCallBack implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback {

    ZooKeeper zk;
    MyConf conf;
    CountDownLatch cc = new CountDownLatch(1);

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public MyConf getConf() {
        return conf;
    }

    public void setConf(MyConf conf) {
        this.conf = conf;
    }

    public void aWait() {
        //String path, Watcher watcher, StatCallback cb, Object ctx
        zk.exists("/AppConf", this, this, "ABC");
        try {
            cc.await(); //开始阻塞，等待取完数据
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //数据回调 DataCallback cb
    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        if (data != null) {
            String s = new String(data);
            conf.setConf(s);
            cc.countDown(); //节点存在并且取完数据，结束阻塞
        }
    }

    //状态回调 StatCallback cb
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if (stat != null) {
            zk.getData("/AppConf", this, this, "abc");
        }
    }


    //watcher 节点事件
    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated: //当节点被创建时，取数据，重新监听
                zk.getData("/AppConf", this, this, "abc");
                break;
            case NodeDeleted:  //当节点数据被删除时，重新阻塞，将数据清空
                conf.setConf("");
                cc = new CountDownLatch(1);
                break;
            case NodeDataChanged:  //节点数据变更时，取数据，重新监听
                zk.getData("/AppConf", this, this, "abc");
                break;
            case NodeChildrenChanged:
                break;
            case DataWatchRemoved:
                break;
            case ChildWatchRemoved:
                break;
        }
    }


}
