package com.tagsgroup.simulators.models;

import javax.swing.*;
import java.util.ArrayList;

public class OrdStatusModel extends DefaultComboBoxModel<String> {
    public OrdStatusModel() {
        addElement("AUTO");
        addElement("NEW");
        addElement("FILLED");
        addElement("PARTIALLY_FILLED");
        addElement("DONE_FOR_DAY");
        addElement("CANCELED");
        addElement("REPLACED");
        addElement("REJECTED");
        addElement("PENDING_REPLACE");

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
