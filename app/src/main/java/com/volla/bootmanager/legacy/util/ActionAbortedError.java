package com.volla.bootmanager.legacy.util;

public class ActionAbortedError extends Exception {
    public ActionAbortedError(Exception e) {
        super(e);
    }
    private static final long serialVersionUID = 1L;
}
