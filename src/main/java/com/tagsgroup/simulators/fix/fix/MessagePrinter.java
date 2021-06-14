package com.tagsgroup.simulators.fix.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;

import quickfix.field.MsgType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MessagePrinter {

    private static Logger logger = LoggerFactory.getLogger(MessagePrinter.class);

    public void print(DataDictionary dd, Message message) throws FieldNotFound {
        String msgType = message.getHeader().getString(MsgType.FIELD);
        StringBuilder builder = new StringBuilder();
        printFieldMap(builder, "\t", dd, msgType, message.getHeader());
        printFieldMap(builder, "\t", dd, msgType, message);
        printFieldMap(builder, "\t", dd, msgType, message.getTrailer());
        System.out.println(builder.toString());

    }

    public List<DecoderEntry> dumpFields(DataDictionary dataDictionary, Message message) throws FieldNotFound {
        if(message==null) {
            return new ArrayList<>();
        }

        String msgType = message. getHeader().getString(MsgType. FIELD) ;
        List<DecoderEntry> builder = new ArrayList<>();

        dumpFieldMap(builder, dataDictionary, msgType, message.getHeader());
        dumpFieldMap(builder, dataDictionary, msgType, message);
        dumpFieldMap(builder, dataDictionary, msgType, message.getTrailer());
        return builder;

    }

    public String dumpField(DataDictionary dataDictionary, Message message, int field) {
        try {
            String value = message.getString(field) ;
            FieldMap fieldMap = message;
            if (message.isSetField(field)) {
                fieldMap = message;
            } else if (message. getHeader().isSetField(field)) {
                fieldMap = message.getHeader();
            } else {
                fieldMap = message.getTrailer();
            }
            if (dataDictionary.hasFieldValue(field)) {
                value = dataDictionary.getValueName(field, fieldMap.getString(field)) + "(" + value + ")";
            }
            return value;
        } catch (Exception e) {
            return "";
        }
    }

    public String getValueName(DataDictionary dataDictionary, int field, String value) {
        if (dataDictionary.hasFieldValue(field)) {
            if (dataDictionary.getValueName(field, value) == null) {
                logger.info("dictionary is null {} {}", field, value);
            }
            return dataDictionary.getValueName(field, value) + "(" + value + ")";
        }
        return value;
    }
    public String getValueShortName(DataDictionary dataDictionary, int field, String value) {
        if (dataDictionary.hasFieldValue(field)) {
            return dataDictionary.getValueName(field, value) + "(" + value + ")";
        }
        return value;

    }

    private void printFieldMap(StringBuilder builder, String prefix, DataDictionary dd, String msgType,  FieldMap fieldMap) throws FieldNotFound {
        Iterator fieldIterator = fieldMap.iterator();
        while (fieldIterator.hasNext()) {
            Field field = (Field) fieldIterator.next();
            if (!isGroupCountField(dd, field)) {
                String value = fieldMap.getString(field.getTag());
                if (dd.hasFieldValue(field.getTag())) {
                    value = dd.getValueName(field.getTag(), fieldMap.getString(field.getTag())) + " (" + value + ")";
                }
                String fname = dd.getFieldName(field.getTag());
                if (fname == null) {
                    fname = "" + field.getTag();
                } else {
                    fname += "(" + field.getTag() + ")" ;
                }
                builder.append(prefix + fname + " = " + value);
                builder.append("\n");
            }
        }

        Iterator groupsKeys = fieldMap.groupKeyIterator();
        while (groupsKeys.hasNext()) {
            int groupCountTag = ((Integer) groupsKeys.next()).intValue();
            builder.append(prefix + dd.getFieldName(groupCountTag) + ": count = " + fieldMap.getInt(groupCountTag));
            builder.append("\n");
            Group g = new Group(groupCountTag, 9);
            int i = 1;
            while (fieldMap.hasGroup(i, groupCountTag)) {
                if (i > 1) {
                    builder.append(prefix + " ----");
                    builder.append("\n");
                }
                fieldMap.getGroup(i, g);
                printFieldMap(builder, prefix + "  ", dd, msgType, g);
                i++;
            }
        }
    }

    private boolean isGroupCountField(DataDictionary dd, Field field) {
        if (dd == null) {
            throw new RuntimeException("field is null " + dd);
        }
        return dd.isHeaderGroup(field.getTag());
    }

    private void dumpFieldMap(List<DecoderEntry> builder, DataDictionary dd, String msgType,  FieldMap fieldMap) throws FieldNotFound {
        Iterator fieldIterator = fieldMap.iterator();
        while (fieldIterator.hasNext()) {
            Field field = (Field) fieldIterator.next();
            if (!isGroupCountField(dd, field)) {
                String value = fieldMap.getString(field.getTag());
                String enumValue = "";
                if (dd.hasFieldValue(field.getTag())) {
//value = dd.getValueName(field.getTag(), fieldMap.getString(field.getTag())) + " (" + value + ")";
                    String tagValue = fieldMap.getString(field.getTag());
                    if (field.getTag() == 18) {
                        String execInstructions[] = tagValue.split("\\s+");
                        for (String ei : execInstructions) {
                            String dvalue = ei;
                            try {
                                dvalue = dd.getValueName(field.getTag(), ei);
                                if (dvalue == null) {
                                    dvalue = ei;
                                } else {
                                    dvalue += "(" + ei + ")";
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (!enumValue.isEmpty()) {
                                enumValue += " ";
                            }
                            enumValue += dvalue;
                        }
                    } else {
                        enumValue = dd.getValueName(field.getTag(), tagValue);
                    }
                }
                int fieldID = field.getTag();
                String fname = dd.getFieldName(field.getTag());
                if (fname == null) {
                    fname = "";
                }
                builder.add(new DecoderEntry(fieldID, fname, value, enumValue));
            }
        }


        Iterator groupsKeys = fieldMap.groupKeyIterator();
        while (groupsKeys.hasNext()) {
            int groupCountTag = ((Integer) groupsKeys.next()).intValue();
            builder.add(new DecoderEntry(groupCountTag, dd.getFieldName(groupCountTag), "" + fieldMap.getInt(groupCountTag), ""));
            Group g = new Group(groupCountTag, 0);

            int i = 1;
            while (fieldMap.hasGroup(i, groupCountTag)) {
                if (i > 1) {
                    //builder.append(prefix + " ----");

                    // builder.append("\n");
                }
                fieldMap.getGroup(i, g);
                dumpFieldMap(builder, dd, msgType, g);
                i++;
            }

        }
    }
}