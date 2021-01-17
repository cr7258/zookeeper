package com.msb.zookeeper.configurationcenter;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.concurrent.CountDownLatch;

/**
 * 默认的watcher，监听session
 * @author 程治玮
 * @since 2021/1/17 1:11 下午
 */
public class DefaultWatch implements Watcher {

    CountDownLatch cc;
    public void setCc(CountDownLatch cc){
        this.cc = cc;
    }

    @Override
    public void process(WatchedEvent event) {
        System.out.println("默认的watcher");
        System.out.println(event.toString());
        switch (event.getState()) {
            case Unknown:
                break;
            case Disconnected:
                break;
            case NoSyncConnected:
                break;
            case SyncConnected:
                cc.countDown(); //CountDownLatch有个正数的计数器，countDown(); 对计数器做减法操作，await(); 等待计数器等于0
            case AuthFailed:
                break;
            case ConnectedReadOnly:
                break;
            case SaslAuthenticated:
                break;
            case Expired:
                break;
            case Closed:
                break;
        }
    }
}
