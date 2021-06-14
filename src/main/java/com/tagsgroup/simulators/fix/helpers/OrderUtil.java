package com.tagsgroup.simulators.fix.helpers;

import quickfix.field.OrdStatus;

public class OrderUtil {

    public  static  boolean isOut(OrdStatus ordStatus) {
        final char status = ordStatus.getValue();
        if(status ==OrdStatus.FILLED || status ==OrdStatus.CANCELED || status == OrdStatus.DONE_FOR_DAY || status == OrdStatus.REJECTED) {
            return true;
        }
        return false;
    }
}
