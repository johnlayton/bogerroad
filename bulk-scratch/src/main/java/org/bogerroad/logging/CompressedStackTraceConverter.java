package org.bogerroad.logging;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.IThrowableProxy;

public class CompressedStackTraceConverter extends ThrowableProxyConverter {
    public CompressedStackTraceConverter() {
        System.out.println("here");
    }

    protected String throwableProxyToString(IThrowableProxy tp) {
        String original = super.throwableProxyToString(tp);
//            return original.replaceAll("\\R\\t?", " ~~ ");
        return "hello world";
    }
}
