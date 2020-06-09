package com.isumi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.util.CollectionUtils;

public class CommonPropertyConfigurer extends PropertyPlaceholderConfigurer implements InitializingBean, BeanNameAware {
    private List<String> locations;
    private List<String> propertiesLocations;
    private Properties[] properties_local;
    private static String poolId;
    private static String mainPoolId = null;
    private boolean envOverride = true;
    private static String commonPro = "common.properties";

    public CommonPropertyConfigurer() {
    }

    public static String getMainPoolId() {
        return mainPoolId;
    }

    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isEmpty(this.locations)) {
            System.out.println("---CommonPropertyConfigurer error: the property 'locations' is required");
            throw new RuntimeException("the property 'locations' is required");
        } else if (StringUtils.isBlank(poolId)) {
            System.out.println("---CommonPropertyConfigurer error: the property 'poolId' is required");
            throw new RuntimeException("the property 'poolId' is required");
        } else {
            if (mainPoolId == null && this.envOverride) {
                mainPoolId = poolId;
            }

            String globalPath = System.getProperty("global.config.path");
            if (StringUtils.isNotBlank(globalPath)) {
                File subEnvFile = new File(globalPath, "env.ini");
                Properties envProperties = null;
                envProperties = loadProperties(subEnvFile);
                if (envProperties != null) {
                    this.propertiesLocations = this.onlyPropertyieLocations(this.locations);
                    this.properties_local = new Properties[this.propertiesLocations.size() + 1];
                    File classPath = new File(this.getClass().getResource("/").getPath());
                    this.setSystemProperty(envProperties);
                    if (!"dev".equals(envProperties.getProperty("env").trim())) {
                        String configFullPath = envProperties.getProperty("config.url") + "/" + poolId + "/" + envProperties.getProperty("env");
                        this.loadLocations(configFullPath, classPath.toString());
                    } else {
                        this.loadPropertiesAndSetSystem(classPath.toString());
                    }

                    if (this.envOverride) {
                        Properties config_path = new Properties();
                        config_path.setProperty("config_path", classPath.toString());
                        this.properties_local[this.propertiesLocations.size()] = config_path;
                    }

                    super.setPropertiesArray(this.properties_local);
                }

            } else {
                System.out.println("---CommonPropertyConfigurer error: global.config.path is null");
                throw new RuntimeException("global.config.path is null");
            }
        }
    }

    private List<String> onlyPropertyieLocations(List<String> locations) {
        List<String> propertiesLocations = new ArrayList();

        for(int i = 0; i < this.locations.size(); ++i) {
            String location = (String)this.locations.get(i);
            if ("properties".equals(location.split("\\.")[1])) {
                propertiesLocations.add(location);
            }
        }

        return propertiesLocations;
    }

    private void setSystemProperty(Properties prop) {
        Iterator i$ = prop.keySet().iterator();

        while(i$.hasNext()) {
            Object okey = i$.next();
            String key = (String)okey;
            String value = prop.getProperty(key);
            System.setProperty(key, value);
        }

    }

    public void loadPropertiesAndSetSystem(String classPath) {
        for(int i = 0; i < this.propertiesLocations.size(); ++i) {
            Properties pro = loadProperties(new File(classPath, (String)this.propertiesLocations.get(i)));
            this.properties_local[i] = pro;
            if (commonPro.equals(this.propertiesLocations.get(i))) {
                this.setSystemProperty(pro);
            }
        }

    }

    public void loadLocations(String urlPath, String classPath) throws Exception {
        try {
            for(int i = 0; i < this.locations.size(); ++i) {
                String location = (String)this.locations.get(i);
                this.downloadNet(urlPath, classPath, location);
            }

            this.loadPropertiesAndSetSystem(classPath);
        } catch (Exception var5) {
            var5.printStackTrace();
            throw new RuntimeException("loadLocations is error !");
        }
    }

    public void downloadNet(String urlPath, String classPath, String fileName) throws Exception {
        File file = new File(classPath + "/" + fileName);
        if (file.exists() && file.isFile()) {
            file.delete();
        }

        FileOutputStream fs = null;

        try {
            URL url = new URL(urlPath + "/" + fileName);
            URLConnection conn = url.openConnection();
            InputStream inStream = conn.getInputStream();
            fs = new FileOutputStream(classPath + "/" + fileName);
            byte[] buffer = new byte[1024];
            int byteread;
            while((byteread = inStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteread);
            }

            fs.flush();
        } catch (Exception var18) {
            throw new RuntimeException("file down is error !");
        } finally {
            try {
                fs.close();
            } catch (IOException var17) {
                throw new RuntimeException("file close is error !");
            }
        }
    }

    public static Properties loadProperties(File propFile) {
        if (!propFile.exists()) {
            System.out.println("---CommonPropertyConfigurer error: Config file <" + propFile.getAbsolutePath() + "> doesn't exists.");
            throw new RuntimeException("Config file <" + propFile.getAbsolutePath() + "> doesn't exists.");
        } else {
            Properties prop = new Properties();

            try {
                InputStream is = new FileInputStream(propFile);
                prop.load(is);
                is.close();
                return prop;
            } catch (IOException var3) {
                var3.printStackTrace();
                throw new RuntimeException(var3);
            }
        }
    }

    public List<String> getLocations() {
        return this.locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public String getPoolId() {
        return poolId;
    }

    public void setPoolId(String poolId) {
        CommonPropertyConfigurer.poolId = poolId;
    }

    public boolean isEnvOverride() {
        return this.envOverride;
    }

    public void setEnvOverride(boolean envOverride) {
        this.envOverride = envOverride;
    }
}
