package com.tagsgroup.simulators.fix.helpers;

import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ChronicleMaps implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(ChronicleMap.class);
    private final Closer closer = Closer.create();
    private final File mapRootDirectory;
    private final Map<Class<?>, ChronicleMap<CharSequence, Object>> mapsByDaoType = Maps.newHashMap();


    public ChronicleMaps(File mapRootDirectory) {
        this.mapRootDirectory = mapRootDirectory;
        if(!mapRootDirectory.exists()) {
            mapRootDirectory.mkdirs();
        }
    }

    private ChronicleMap<CharSequence, Object> addMap(Class valueClass) {
        final ChronicleMap<CharSequence, Object> newMap;
        try {
            newMap = ChronicleMapBuilder.of(CharSequence.class, valueClass).averageKey("averageKey").averageValue(valueClass.newInstance()).entries(100000L)
                    .createPersistedTo(new File(mapRootDirectory, valueClass.getSimpleName().toLowerCase()+ ".map"));
            closer.register(newMap);
            logger.info("Created  ({})", newMap.file());
            return newMap;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public <T> ChronicleMap<CharSequence, T> getMap(Class<T> daoClass) {
        logger.info("getMap");
        synchronized (this.getClass()) {
            return (ChronicleMap<CharSequence, T>) mapsByDaoType.computeIfAbsent(daoClass, this::addMap);
        }
    }

    @Override
    public void close() throws IOException {
        closer.close();
        logger.info("Closing ({})", mapRootDirectory.getAbsolutePath());
    }

    public static void main(String[] args) throws Exception{
        ChronicleMaps cm = new ChronicleMaps(new File("./data/messages.test.map"));
        ChronicleMap<CharSequence, Message> mp = cm.getMap(Message.class);

        //https://www.fixsim.com/sample-fix-messages
        String fix = "8=FIX.4.4\u00019=148\u000135=D\u000134=1080\u000149=TESTBUY1\u000152=20180920-18:14:19.508\u000156=TESTSELL1\u000111=636730640278898634\u000115=USD\u000121=2\u000138=7000\u000140=1\u000154=1\u000155=MSFT\u000160=20180920-18:14:19.492\u000110=092\u0001";
        MessageFactory messageFactory = new DefaultMessageFactory();
        DataDictionary dataDict = new DataDictionary("FIX44.xml");
        Message msg = MessageUtils.parse(messageFactory, dataDict, fix);
        mp.put("test", msg);
    }


}
