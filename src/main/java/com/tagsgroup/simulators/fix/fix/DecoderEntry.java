package com.tagsgroup.simulators.fix.fix;

public class DecoderEntry {

    private int fieldID;

    private String fieldName;
    private String fieldValue;
    private String fieldEnum;


    public DecoderEntry(int fieldID, String fieldName, String fieldValue, String fieldEnum) {
        this.fieldID = fieldID;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.fieldEnum = fieldEnum;

    }

    public int getFieldID() {
        return fieldID;
    }

    public void setFieldID(int fieldID) {
        this.fieldID = fieldID;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public String getFieldEnum() {
        return fieldEnum;
    }

    public void setFieldEnum(String fieldEnum) {
        this.fieldEnum = fieldEnum;
    }

    public String getPrintableFixName() {
        return getFieldName() + "(" + getFieldID() + ")";
    }

    public String getPrintableValue() {
        String value = getFieldValue();
        try {
            if (!getFieldEnum().isEmpty()) {
                if (getFieldID() == 18) {
                    value = getFieldEnum();
                } else {
                    value = getFieldEnum() + "(" + getFieldValue() + ")";
                }
            }
        } catch (Exception e) {
            System.err.println("error:" + e.getMessage() + " tag " + getFieldID() + " “ " + getFieldEnum());
        }
        return value;
    }
}
