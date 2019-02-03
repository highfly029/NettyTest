
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * @ClassName ConfigPropUtil
 * @Description TODO
 * @Author liyunpeng
 * @Date 2018/11/15 10:03
 **/
public class ConfigPropUtil {
    private static String NULLSTR = "";
    private static Properties prop = null;
    static {
        String CONFIG_PATH = "/conf/";
        String CONFIG_NAME = "config.properties";
        prop = new Properties();
        try {
            boolean isLoad = false;
            String classPath = ConfigPropUtil.class.getResource("/").getPath();
            File file = new File(classPath);
            for(String name : file.list()) {
                if (name.equalsIgnoreCase(CONFIG_NAME)) {
                    InputStream in = new BufferedInputStream(new FileInputStream(classPath + CONFIG_NAME));
                    prop.load(in);
                    isLoad = true;
                    break;
                }
            }
            if (!isLoad) {
                String basePath = file.getParent();
                InputStream in = new BufferedInputStream(new FileInputStream(basePath + CONFIG_PATH + CONFIG_NAME));
                prop.load(in);
            }
            if (prop.stringPropertyNames().size() != 0) {
                System.out.println("load config success");
            } else {
                System.out.println("load config fail");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getValue(String key) {
        return prop.getProperty(key, NULLSTR);
    }

    public static int getIntValue(String key) {
        return Integer.valueOf(prop.getProperty(key, "-1"));
    }

}
