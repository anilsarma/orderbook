package com.tagsgroup.simulators.models;

import com.tagsgroup.simulators.fix.MessageHolder;
import com.tagsgroup.simulators.fix.fix.ColumnDetail;
import com.tagsgroup.simulators.fix.helpers.Utils;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.field.TransactTime;

import javax.swing.table.AbstractTableModel;
import java.util.LinkedList;

public class LogMessageModel extends AbstractTableModel {
    private static Logger logger = LoggerFactory.getLogger(LogMessageModel.class);

    LinkedList<Pair<TransactTime, Pair<String, String>>> data = new LinkedList<>();
    ColumnDetail columns[] = {
            new ColumnDetail<String, MessageHolder>( "Time", String.class, (obj)->  Utils.get().getTimeField(TransactTime.FIELD, obj.getNewOrderSingle())),
            new ColumnDetail<String, MessageHolder>("Category", String.class, (obj) -> obj.getKeyClOrdId()),
            new ColumnDetail<String, MessageHolder>("Message", String.class, (obj) -> obj.getKeyClOrdId()),

    };

    String value = null;
    public ColumnDetail[] getColumns() {
        return columns;

    }

    public LogMessageModel() {

    }

    public int getRowCount() {
        return data.size();

    }

    public int getColumnCount() {
        return columns.length;
    }

    public String getColumnName(int columnIndex) {
        return columns[columnIndex].getName();
    }

    public Class<?> getColumnClass(int columnIndex) {
        return columns[columnIndex].getType();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(value!=null) {
            return value;
        }
        Pair<TransactTime, Pair<String, String>> entry = data.get(rowIndex);
        if(columnIndex==0) {
            return entry.getKey().getValue().toString();
        } if( columnIndex ==1) {
            return entry.getValue().getKey();
        }
        return entry.getValue().getValue();
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        ///
    }

    public void updateData(String category, String message) {
        data.add(new Pair<>( new TransactTime(), new Pair<>(category, message)));
        while (data.size()>1000) {
            data.remove(0);
        }
        fireTableDataChanged();
    }

    public  void clear() {
        data.clear();
        fireTableDataChanged();
    }

}