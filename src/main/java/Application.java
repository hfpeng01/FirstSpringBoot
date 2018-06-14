import Util.ApplicationStartupUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by LiPeng on 17/9/18.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args){
        SpringApplication.run(Application.class,args);


        boolean result = false;
        try{
            result = ApplicationStartupUtil.checkExternalService();
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("External services validation completed ! Result was : "+result);
    }
}
