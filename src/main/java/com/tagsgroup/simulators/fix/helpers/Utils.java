package com.tagsgroup.simulators.fix.helpers;

import com.tagsgroup.simulators.fix.fix.DecoderEntry;
import com.tagsgroup.simulators.fix.fix.MessagePrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static Logger logger = LoggerFactory.getLogger(Utils.class);
    com.tagsgroup.simulators.fix.fix.MessagePrinter printer = new MessagePrinter();
    MessageFactory messageFactory = new DefaultMessageFactory();
    DataDictionary dataDict;

    private Utils() {
        try {
            InputStream is = this.getClass().getClassLoader().getResource("FIX44.xm1").openStream();
            dataDict = new DataDictionary(is);
            //dataDict = new DataDictionary("FIX44.xml") ;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    static Utils instance;

    public static Utils get() {
        if (instance != null) {
            return instance;
        }
        synchronized (Utils.class) {
            Utils tmp = new Utils();
            instance = tmp;
            return instance;
        }
    }

    public String getField(int id, FieldMap msg) {
        try {

            return msg.getField(new StringField(id)).getValue();

        } catch (Exception e) {
            return "";
        }
    }


    public String getDateField(int id, FieldMap msg) {
        try {
            //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd") ;
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
            return dateFormat.format(msg.getField(new UtcTimeStampField(id)).getValue());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getTimeField(int id, FieldMap msg) {
        try {
            //SimpleDateFormat dateFormat = new SimpleDateFormat(“yyyyMMdd-HH:mm:ss.SSS");
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
            return dateFormat.format(msg.getField(new UtcTimeStampField(id)).getValue());
        } catch (Exception e) {

            e.printStackTrace();
            return "";
        }
    }

    public String getLocalDateField(int id, FieldMap msg) {
        try {
            //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd") ;
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDateTime dt = msg.getField(new UtcTimeStampField(id)).getValue();
            dt.atZone(ZoneId.systemDefault());
            return dateFormat.format(dt);
        } catch (Exception e) {
            return "";
        }
    }

    public String getLocalTimeField(int id, FieldMap msg) {
        try {
            //SimpleDateFormat dateFormat = new SimpleDateFormat(“yyyyMMdd-HH:mm:ss.SSS");
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
            LocalDateTime dt = msg.getField(new UtcTimeStampField(id)).getValue();
            ZonedDateTime dt2 = dt.atZone(ZoneId.of("UTC"));
            dt2 = dt2.withZoneSameInstant(ZoneId.systemDefault());
            return dateFormat.format(dt2);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getDoubleField(int id, FieldMap msg) {
        try {
            if (!msg.isSetField(id)) {
                return "";
            }
            return "" + msg.getField(new DoubleField(id)).getValue();
        } catch (Exception e) {
            //e.printStackTrace();
            return "";
        }
    }

    public String getEnumValue(int id, FieldMap msg) {
        if (!msg.isSetField(id)) {
            return "";
        }
        return printer.getValueName(dataDict, id, "" + getField(id, msg));
    }

    public String getEnumValue(int id, String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return printer.getValueName(dataDict, id, value);

    }

    public String getFix(Message msg) {
        try {
            List<DecoderEntry> d = printer.dumpFields(dataDict, msg);
            StringBuilder str = new StringBuilder();

            str.append(msg.toString().replaceAll("\\x01", "|") + "“\n");
            for (DecoderEntry de : d) {
                String value = de.getFieldValue();
                try {
                    if (!de.getFieldEnum().isEmpty()) {
                        value = de.getFieldEnum() + "(" + de.getFieldValue() + ")";
                    }

                } catch (Exception e) {
                    //e.printStackTrace();
                }
                str.append(de.getFieldName() + "(" + de.getFieldID() + ")" + "=" + value + "“\n");
            }
            return str.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "NO FIX MESSAGE DECODED";
    }

    static String pattern = "(8=FIX\\..+\00110=\\d\\d\\d)";
    static Pattern regex = Pattern.compile(pattern);

    public quickfix.Message buildFixMessage(String fix, boolean fixCheckSum) {
        Matcher m = regex.matcher(fix);
        if (!m.find()) {
            return null;
        }

        fix = m.group(0);
        if (fixCheckSum) {
            fix = fix.trim();
            int ck = MessageUtils.checksum(fix);
            fix = fix.substring(0, fix.length() - 3) + String.format("%@3d", ck) + "\01";
        }
        try {
            quickfix.Message msg = MessageUtils.parse(messageFactory, dataDict, fix);
            return msg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getPrintableFix(Message msg) {
        try {
            List<DecoderEntry> d = printer.dumpFields(dataDict, msg);
            return format(d);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "NO FIX MESSAGE DECODED";
    }

    public String format(List<DecoderEntry> msg) {

        OptionalInt len = msg.stream().map(DecoderEntry::getPrintableFixName).mapToInt(String::length).max();

        int max = len.orElse(-1);

        StringBuilder str = new StringBuilder();

        for (DecoderEntry entry : msg) {
            String fmt = "%" + (5 + max) + "s=%s";
            str.append(String.format(fmt, entry.getPrintableFixName(), entry.getPrintableValue()));
            str.append("\n");

        }

        return str.toString();

    }

    public static InputStream getResource(String path) throws IOException {
        InputStream url = Utils.class.getClassLoader().getResourceAsStream(path);
        if (url != null) {
            return url;
        } else {

            logger.error("Couldn't find file: “" + path + " " + url);
            return null;
        }
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */

    public static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = Utils.class.getClassLoader().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {

            logger.error("Couldn't find file: " + path + " " + imgURL);
            return null;
        }
    }

    public interface Callable {
        void call() throws Exception;
    }

    public static <T> void ignore_exception(Callable supplier) {
        try {
            supplier.call();
        } catch (Exception e) {

        }
    }

    public static String format(String fmt, Object... args) {
        return org.slf4j.helpers.MessageFormatter.arrayFormat(fmt, args).getMessage();
    }

    public static double getPrice(String symbol) {
        return 12; // get it fro yahoo or something.
    }


}