package frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.doda25.team9.libversion.VersionUtil; 

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        System.out.println("Using lib-version: " + VersionUtil.getVersion());
        SpringApplication.run(Main.class, args);
    }

}