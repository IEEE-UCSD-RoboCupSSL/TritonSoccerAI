package Triton.Config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class Config {

    private static Properties global_properties = null;

    private Config() {
    }

    public static Properties load() {
        if (global_properties == null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                global_properties = mapper.readValue(new File("config/properties.json"),
                        Properties.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return global_properties;
    }
}
