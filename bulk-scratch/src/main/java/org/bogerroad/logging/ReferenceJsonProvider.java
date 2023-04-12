package org.bogerroad.logging;

import ch.qos.logback.classic.pattern.ExtendedThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.FieldNamesAware;
import net.logstash.logback.composite.JsonWritingUtils;
import net.logstash.logback.fieldnames.LogstashFieldNames;
import org.bogerroad.ExceptionScratch;
import org.bogerroad.ExceptionScratch.CustomException;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReferenceJsonProvider extends AbstractFieldJsonProvider<ILoggingEvent> implements FieldNamesAware<LogstashFieldNames> {

    public static final String FIELD_STACK_TRACE = "exception";

    public ReferenceJsonProvider() {
        setFieldName(FIELD_STACK_TRACE);
    }

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        Optional.ofNullable(event)
                .map(e -> e.getThrowableProxy() instanceof ThrowableProxy ? (ThrowableProxy) e.getThrowableProxy() : null)
                .map(p -> p.getThrowable() instanceof Exception ? (Exception) p.getThrowable() : null)
                .ifPresent(ex -> {
                    try {
                        generator.writeObjectFieldStart(getFieldName());
                        Arrays.stream(ex.getClass().getDeclaredMethods())
                                .filter(method -> method.canAccess(ex))
                                .filter(method -> method.getParameterCount() == 0)
                                .filter(method -> {
                                    return method.getAnnotation(Logged.class) != null;
                                })
//                                .collect(Collectors.toMap(
//                                    method -> method.getName(),
//                                    method -> {
//                                        try {
//                                            return method.invoke(ex);
//                                        } catch (Exception exception) {
//                                            return null;
//                                        }
//                                    }
//                                ))
                                .forEach(method -> {
                                    try {
                                        generator.writeObjectField(
                                                method.getAnnotation(Logged.class).value(),
                                                method.invoke(ex)
                                        );
//                                        generator.writeStringField(
//                                                method.getAnnotation(Logged.class).value(),
//                                                );
                                    } catch (Exception exception) {
                                    }
                                });
                        generator.writeEndObject();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

//        ThrowableProxy.class.isInstance()
//
//        IThrowableProxy throwableProxy = event.getThrowableProxy();
//        if (throwableProxy instanceof ThrowableProxy) {
//            Throwable throwable = ((ThrowableProxy)event.getThrowableProxy()).getThrowable();
//            if (throwable instanceof CustomException) {
//            }
//        }


//        IThrowableProxy throwableProxy = event.getThrowableProxy();
//        if (throwableProxy != null) {
//            JsonWritingUtils.writeStringField(generator, getFieldName(), "bob");
//        }
    }

    @Override
    public void setFieldNames(LogstashFieldNames fieldNames) {
//        setFieldName(fieldNames.getStackTrace());
    }

//        public ThrowableHandlingConverter getThrowableConverter() {
//            return throwableConverter;
//        }
//
//        public void setThrowableConverter(ThrowableHandlingConverter throwableConverter) {
//            this.throwableConverter = throwableConverter;
//        }

    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Logged {
        String value();
    }
}
