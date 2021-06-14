package com.tagsgroup.simulators.renderer;

import com.tagsgroup.simulators.fix.fix.ColorRender;
import com.tagsgroup.simulators.models.FixMessagesModel;
import com.tagsgroup.simulators.models.OrderBlotterModel;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class MessageRenderer extends JLabel implements TableCellRenderer {

    static String colors[] = {
            "Ordstatus", "Symbol", "MsgType", "OrdType"
    };
    FixMessagesModel model;

    public MessageRenderer(FixMessagesModel model) {
        super.setOpaque(true);
        this.model  = model;
    }
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.setBackground(Color.WHITE);
        this.setForeground(Color.BLACK);
        if(isSelected) {
            this.setBackground(com.tagsgroup.simulators.fix.helpers.Color.SELECTION_COLOR);
            this.setForeground(Color.WHITE);
        }
        this.setText(value.toString());
        if(model.getColumns()[column].getName().compareToIgnoreCase("OrdStatus")==0) {
            ColorRender.renderOrderStatus(this, (String)value);
        }

        this.setToolTipText("Tool tip message");
        return this;
    }
}
