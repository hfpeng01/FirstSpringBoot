package Entity;

import java.util.concurrent.CountDownLatch;

/**
 * Created by LiPeng on 17/12/22.
 */
public abstract class BaseHealthChecker implements Runnable {
    private CountDownLatch _latch;
    private String _serviceName;
    private boolean _serviceUp;
    public BaseHealthChecker(String serviceName, CountDownLatch latch){
        super();
        this._latch = latch;
        this._serviceName = serviceName;
        this._serviceUp = false;
    }
    @Override
    public void run() {
        try {
            verifyService();
            _serviceUp = true;
        }catch (Throwable t){
            t.printStackTrace(System.err);
        }finally{
            if(_latch !=null){
                _latch.countDown();
            }
        }
    }
    public String getServiceName(){
        return _serviceName;
    }
    public boolean getServiceUp(){
        return _serviceUp;
    }
    public abstract  void verifyService();
}
