package com.tagsgroup.simulators.models;

import javax.swing.*;
import java.util.ArrayList;

public class ExecTypeModel extends DefaultComboBoxModel<String> {
    public ExecTypeModel() {
        addElement("TRADE");
        addElement("PARTIAL_FILL");
        addElement("NEW");
        addElement("FILL");
        addElement("CANCELED");
        addElement("PENDING_CANCEL");
        addElement("REPLACED");
        addElement("REJECTED");
        addElement("TRADE_CONNECT");
        addElement("TRADE_CANCEL");
        addElement("DONE_FOR_DAY");

        ArrayList<String> values = new ArrayList<>();

        for (int i=0; i < getSize();i++) {
            values.add(getElementAt(i));
        }

        values.sort(String::compareTo);
        removeAllElements();

        for(String v:values) {
            addElement(v);
        }

    }
}
