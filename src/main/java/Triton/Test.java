package Triton;

import Triton.Config.Properties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class Test {

    public static void main(String[] args) {

        ObjectMapper mapper = new ObjectMapper();

        try {
            Properties property = mapper.readValue(new File("config/properties.json"),
                    Properties.class);
            System.out.println(property);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
