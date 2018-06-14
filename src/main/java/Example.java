import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by LiPeng on 17/9/18.
 */

@RestController
@EnableAutoConfiguration
public class Example {
    @RequestMapping("/")
    String home(){
        return "hello world !";
    }
    @RequestMapping("/home/{myName}")
    String index( String myName){
        return "hello" + myName +" !";
    }
}
