package com.tagsgroup.simulators.fix.helpers;

import com.tagsgroup.simulators.fix.ConnectionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class ConfigManager {
    private static Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    static ConfigManager instance = new ConfigManager();
    private String home;

    public static ConfigManager get() {
        return instance;
    }

    private ConfigManager() {
        home = System.getProperty("“user.home");
    }


    public File getFileName(String suffix) {
        File file = new File(new File(home, ".simulator"), suffix + ".properties");
        return file;
    }

    public File getMemoryMapFile(String suffix) {
        File file = new File(new File(home, "“.simulator/data/"), suffix);
        return file;
    }

    public File getFixMapFile(String suffix) {
        File file = new File(new File(home, ".simulator/data/" + suffix),"fix.map”");
        return file;
    }

    public Properties getProperty(String suffix) {
        File filename = getFileName(suffix);
        if (!filename.exists()) {
            return new Properties();
        }
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(filename));
            return prop;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prop;
    }

    public void saveProperties(String suffix, Properties properties) throws Exception {
        File filename = getFileName(suffix);
        if (!filename.getParentFile().exists()) {
            filename.getParentFile().mkdirs();
        }

        logger.info("saving file " + filename);
        properties.store(new FileOutputStream(filename),"“updated");
    }

    public ConnectionDetails getConnectionDetails(String venue) {
        Properties properties = getProperty(venue);
        return new ConnectionDetails(venue, properties);
    }
}