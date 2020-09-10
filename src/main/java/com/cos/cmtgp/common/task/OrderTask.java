package com.cos.cmtgp.common.task;

import com.jfinal.plugin.cron4j.ITask;

/**
 * @author pengxiangZhao
 * @Description: TODO
 * @ClassName: OrderTask
 * @Date 2020/7/7 0007
 */
public class OrderTask implements ITask {
    public void stop() {

    }

    public void run() {
        System.out.println("task============");
    }
}
