package com.msb.zookeeper;

import java.lang.String;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * zookeeper创建节点，修改节点，注册监听
 * @author 程治玮
 * @since 2021/1/16 9:30 下午
 */

public class App {
    //zookeeper是有session概念的，没有线程池的概念
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        //参数：zookeeper地址:端口号，连接超时时间，watcher
        //watch观察，回调
        //watch的注册只发生在读类型调用(get,exists)
        //第一类，new zookeeper的时候，传入的watch，这个watcher是session级别的，和node(path)没有关系
        //第二类，和node关联的

        //线程安全的，解决异步的同步问题
        //CountDownLatch允许一个或者多个线程一直等待，直到一组其它操作执行完成。
        CountDownLatch cd = new CountDownLatch(1);

        ZooKeeper zk = new ZooKeeper("192.168.1.82:2181,192.168.1.82:2182,192.168.1.82:2183,192.168.1.82:2184",
                3000, //连接断开临时节点就会删除
                new Watcher() {  //这个watcher和session有关系
                    @Override
                    //Watch的回调方法
                    public void process(WatchedEvent event) {
                        Event.KeeperState state = event.getState();
                        Event.EventType type = event.getType();
                        String path = event.getPath();
                        System.out.println("new zk watcher:" + event.toString());

                        switch (state) {
                            case Unknown:
                                break;
                            case Disconnected:
                                break;
                            case NoSyncConnected:
                                break;
                            case SyncConnected:
                                System.out.println("connected");
                                cd.countDown();
                                break;
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

                        switch (type) {
                            case None:
                                break;
                            case NodeCreated:
                                break;
                            case NodeDeleted:
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
                });

        //阻塞
        //当zookeeper集群回调了事件后，才会结束阻塞状态
        cd.await();
        ZooKeeper.States state = zk.getState();
        switch (state) {
            case CONNECTING:
                System.out.println("ing.......");
                break;
            case ASSOCIATING:
                break;
            case CONNECTED:
                System.out.println("ed.......");
                break;
            case CONNECTEDREADONLY:
                break;
            case CLOSED:
                break;
            case AUTH_FAILED:
                break;
            case NOT_CONNECTED:
                break;
        }

        //创建节点
        //参数：节点路径，节点数据（二进制安全的，给的是字节），ACL（这里没有权限限制），节点类型
        String pathName = zk.create("/node1", "olddata".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        Stat stat = new Stat();

        //获取节点数据并添加监听
        //数据分为：元数据和Data(1M)
        //参数：节点路径，watcher，节点元数据
        byte[] node = zk.getData("/node1", new Watcher() {  //这个watcher和node有关系
            @Override
            public void process(WatchedEvent event) {
                System.out.println("getData watcher:" + event.toString());
                try {
                    //true  default watcher 重新注册 之前new zookeeper的watcher
                    //this  现在的这个watcher
                    zk.getData("/node1",this,stat); //重复注册监听
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, stat);
        System.out.println(new String(node));

        //修改节点数据
        //参数：节点路径，节点数据，版本号
        Stat stat1 = zk.setData("/node1","newData1".getBytes(),0);  //触发回调
        Stat stat2 = zk.setData("/node1","newData2".getBytes(),stat1.getVersion());  //因为重复注册了监听，所以回再次触发回调


        //异步回调
        System.out.println("--------async start---------");
        zk.getData("/node1", false, new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                System.out.println("--------async call back---------");
                System.out.println(new String(data));
                System.out.println(ctx.toString() );
            }
        },"abc");
        System.out.println("--------async over---------");

        Thread.sleep(99999999);
    }
}
