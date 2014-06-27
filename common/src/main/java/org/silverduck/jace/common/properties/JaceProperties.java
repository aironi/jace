package org.silverduck.jace.common.properties;

import org.silverduck.jace.common.exception.JaceRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Iiro Hietala on 17.5.2014.
 */
public class JaceProperties {

    public static Integer getIntProperty(String key) {
        return Integer.parseInt(getProperty(key)); // we want to throw numberformatexception
    }

    public static String getProperty(String key) {
        return (String) readProperty(key);
    }

    private static Object readProperty(String key) {
        InputStream is = JaceProperties.class.getClassLoader().getResourceAsStream("jace.properties");
        Properties p = new Properties();
        try {
            p.load(is);
            is.close();
        } catch (IOException e) {
            throw new JaceRuntimeException("Couldn't read properties file.", e);
        }
        return p.getProperty(key);
    }
}
