package uk.antiperson.stackmob.config;

import java.util.List;

public class ConfigList {

    private final List<?> list;
    private final String path;
    private final boolean inverted;
    private final ConfigFile configFile;
    public ConfigList(ConfigFile configFile, List<?> list, String path, boolean inverted) {
        this.configFile = configFile;
        this.list = list;
        this.path = path;
        this.inverted = inverted;
    }

    /**
     * List contains method which supports inverting lists.
     * @param tocheck object to check is in the list
     * @return whether this object is in the list.
     */
    public boolean contains(Object tocheck) {
        if (inverted){
            return !list.contains(tocheck);
        }
        return list.contains(tocheck);
    }

    public List<Integer> asIntList() {
        return configFile.getIntegerList(path);
    }

    public static ConfigList getConfigList(ConfigFile configFile, ConfigValue value) {
        List<?> list = (List<?>) value.getValue();
        String path = value.getPath();
        if (list == null) {
            throw new UnsupportedOperationException(path + " list is null!");
        }
        boolean inverted = configFile.getBoolean(value.getPath() + "-invert");
        return new ConfigList(configFile, list, path, inverted);
    }


}
