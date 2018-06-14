package Util;

import Entity.BaseHealthChecker;
import Entity.CacheHealthChecker;
import Entity.DatabaseHealthChecker;
import Entity.NetworkHealthChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by LiPeng on 17/12/22.
 */
public class ApplicationStartupUtil {
    private static List<BaseHealthChecker> _services;
    //主启动类,初始化闭锁,等待所有服务都检测完
    private static CountDownLatch _latch;
    //This latch will be used to wait on
    public ApplicationStartupUtil() {
    }
    private final static ApplicationStartupUtil INSTANCE = new ApplicationStartupUtil();
    public static ApplicationStartupUtil getInstance(){
        return INSTANCE;
    }
    public static boolean checkExternalService() throws  Exception{
        _latch = new CountDownLatch(3);//初始化闭锁

        _services = new ArrayList<BaseHealthChecker>();
        _services.add(new NetworkHealthChecker("Network",_latch));
        _services.add(new CacheHealthChecker("Cache",_latch));
        _services.add(new DatabaseHealthChecker("Database",_latch));

        Executor executor = Executors.newFixedThreadPool(_services.size());
        for(final BaseHealthChecker v:_services){
            executor.execute(v);
        }
        _latch.await();

        for(final BaseHealthChecker v:_services){
            if(!v.getServiceUp()){
                return false;
            }
        }
        return true;
    }
}
