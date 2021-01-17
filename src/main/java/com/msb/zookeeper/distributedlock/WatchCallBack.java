package com.msb.zookeeper.distributedlock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 监听回调
 * @author 程治玮
 * @since 2021/1/17 4:00 下午
 */
public class WatchCallBack implements Watcher, AsyncCallback.StringCallback, AsyncCallback.Children2Callback, AsyncCallback.StatCallback {

    ZooKeeper zk;
    String threadName;
    CountDownLatch cc = new CountDownLatch(1);
    String pathName;

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    //获得锁
    public void tryLock() {
        try {
            //创建节点，并注册StringCallback
            zk.create("/lock", threadName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, this, "abc");
            cc.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //释放锁
    public void unLock() {
        try {
            //-1表示忽略版本判断
            zk.delete(pathName, -1);
            System.out.println(threadName + "干完活了");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent event) {

        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            //如果第一个节点的锁释放了，其实只有第二个节点收到了那个回调事件，getChildren判断自己是不是第一个
            //如果不是第一个节点挂了，而是中间的某一个节点挂了，也能造成他后面的节点收到这个通知，并且getChildren重新watch新的前面的那个节点
            case NodeDeleted:
                zk.getChildren("/", false, this, "abc");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
            case DataWatchRemoved:
                break;
            case ChildWatchRemoved:
                break;
        }
    }

    //StringCallback
    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        //当节点创建成功
        if (name != null) {
            //name是节点名
            System.out.println(threadName + " create node " + name);
            pathName = name;
            //判断自己的序号在这个目录下是不是最小的
            //“/”是"/testLock"
            //父节点上是不需要监控的，只需要监控前面一个顺序节点
            zk.getChildren("/", false, this, "abc");  //当成功创建节点时，注册Children2Callback

        }

    }


    //Children2Callback cb
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        //已经创建了自己的顺序节点，并且可以看到自己前面的节点，但是前面的节点拿到的不是顺序的，需要重新排序
        Collections.sort(children);
        int i = children.indexOf(pathName.substring(1));
        //判断自己是不是一个节点
        if (i == 0) {
            System.out.println(threadName + "获得了锁");
            try {
                zk.setData("/", threadName.getBytes(), -1);  //让线程慢一点，让后面的人可以监控到
                cc.countDown();  // 如果是的话就加锁
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            zk.exists("/" + children.get(i - 1), this, this, "abc"); //如果不是的话监听自己前面的那个节点，注册StatCallback
        }
    }

    //StatCallback cb
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {

    }
}
