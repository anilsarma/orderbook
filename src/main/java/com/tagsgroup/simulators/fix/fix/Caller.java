package com.tagsgroup.simulators.fix.fix;

public interface Caller<T> {
    Object invoke(T obj);
}
