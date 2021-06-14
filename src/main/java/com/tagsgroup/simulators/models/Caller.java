package com.tagsgroup.simulators.models;

public interface Caller<T> {
    Object invoke(T obj);
}
