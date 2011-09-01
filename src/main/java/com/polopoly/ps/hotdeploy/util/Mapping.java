package com.polopoly.ps.hotdeploy.util;

public interface Mapping<F, T> {
    T map(F from);
}
