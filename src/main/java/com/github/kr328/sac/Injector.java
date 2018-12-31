package com.github.kr328.sac;

@SuppressWarnings("unused")
public class Injector {
    public static int injectSystem() {
        return SystemServerInjector.inject();
    }

    public static int injectApplication() {
        return ApplicationInjector.inject();
    }
}
