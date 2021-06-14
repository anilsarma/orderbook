package com.tagsgroup.simulators.fix.fix;

public class ColumnDetail<T, M> {
    String name;
    Class<?> type;
    Caller<M> caller;
    public ColumnDetail(String name, Class<?> type, Caller<M> caller) {
        this.name = name;
        this.type = type;
        this.caller = caller;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public Object getValue(M obj) {
        return caller.invoke(obj);
    }
}
