package com.tagsgroup.simulators.renderer;

import com.tagsgroup.simulators.fix.fix.ColorRender;
import com.tagsgroup.simulators.fix.helpers.Utils;
import com.tagsgroup.simulators.models.FixMessagesModel;
import com.tagsgroup.simulators.models.LogMessageModel;
import quickfix.Message;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class LogMessageRenderer extends JLabel implements TableCellRenderer {

    static String colors[] = {
            "ERROR", "WARN", "MsgType", "OrdType"
    };
    LogMessageModel model;

    public LogMessageRenderer(LogMessageModel model) {
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
        if(model.getColumns()[column].getName().compareToIgnoreCase("Category")==0) {
            ColorRender.renderLog(this, (String)value);
        }

        String text = value.toString();
        if(text.contains("8=FIX.4")) {
            Message msg = Utils.get().buildFixMessage(text, true);
            if(msg != null) {
                text = Utils.get().getPrintableFix(msg);
            }
        }
        text = "<html>" + text.replaceAll("\n", "<br>\n") + "</html>";
        this.setToolTipText(text);
        return this;
    }
}
