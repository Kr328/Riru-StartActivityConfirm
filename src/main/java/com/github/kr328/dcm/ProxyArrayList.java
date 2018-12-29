package com.github.kr328.dcm;

import java.util.ArrayList;

public class ProxyArrayList<T> extends ArrayList<T> {
    @Override
    public boolean add(T o) {
        return super.add(o);
    }
}
