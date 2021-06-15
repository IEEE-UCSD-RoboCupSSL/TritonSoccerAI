package Triton.Config;

import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;

public interface IniConfig {
    public void processFromParsingIni(File iniFIle) throws IOException;
}
