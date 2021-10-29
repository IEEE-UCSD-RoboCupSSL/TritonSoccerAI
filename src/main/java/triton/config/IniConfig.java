package triton.config;

import java.io.File;
import java.io.IOException;

public interface IniConfig {
    public void processFromParsingIni(File iniFIle) throws IOException;
}
