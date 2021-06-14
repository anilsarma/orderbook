package com.tagsgroup.simulators.helpers;

import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class ChronicleMaps implements Cloneable {
    private  static final Logger logger = LoggerFactory.getLogger(ChronicleMaps.class);
    private final Closer closer = Closer.create();
    private final File mapRootDirectory;

    private final Map<Class<?>, ChronicleMap<CharSequence, Object>> mapsByDaoType = Maps.newHashMap();

    public ChronicleMaps(File mapRootDirectory) {
        this.mapRootDirectory = mapRootDirectory;
        mapRootDirectory.mkdir();
    }


    private ChronicleMap<CharSequence, Object> addMap(Class cls) {
        final  ChronicleMap<CharSequence, Object> newMap;
        try {
            newMap = ChronicleMapBuilder.of(CharSequence.class, cls).averageKey("averageKey").averageValue(cls.newInstance()).entries(10000L)
                    .createPersistedTo(new File(mapRootDirectory, cls.getSimpleName().toLowerCase() + ".map"));
            closer.register(newMap);
            logger.info("map file {}", newMap.file());
            return newMap;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> ChronicleMap<CharSequence, T> getMap(Class<T> cls) {
        return (ChronicleMap<CharSequence,T>) mapsByDaoType.computeIfAbsent(cls, this::addMap);
    }


}
