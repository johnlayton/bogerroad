package org.bogerroad;

import ch.qos.logback.classic.pattern.ExtendedThrowableProxyConverter;
import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.FieldNamesAware;
import net.logstash.logback.composite.JsonWritingUtils;
import net.logstash.logback.fieldnames.LogstashFieldNames;
import org.bogerroad.logging.ReferenceJsonProvider;
//import org.bogerroad.logging.ReferenceJsonProvider.Logged;
import org.bogerroad.logging.ReferenceJsonProvider.Logged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

import static net.logstash.logback.argument.StructuredArguments.kv;

public class ExceptionScratch {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionScratch.class);


    public static void main(String[] args) {

        try {
            throw new CustomException("1", 2, new RuntimeException(""));
        } catch (final Exception exception) {
            LOGGER.error("Custom Exception 1", exception);
        }

        try {
            throw new OtherException(new Date(), new RuntimeException(""));
        } catch (final Exception exception) {
            LOGGER.error("Other Exception 1", exception);
        }

        try {
            throw new RuntimeException("", new RuntimeException(""));
        } catch (final Exception exception) {
            LOGGER.error("Other Exception 1", exception);
        }

    }

    public static class CustomException extends RuntimeException {
        private final String property1;
        private final int property2;

        public CustomException(String property1, int property2, Throwable cause) {
            super(String.format("Exception for %s - %d", property1, property2), cause);
            this.property1 = property1;
            this.property2 = property2;
        }

        @Logged("propA")
        public String getProperty1() {
            return property1;
        }

        @Logged("propB")
        public int getProperty2() {
            return property2;
        }
    }

    public static class OtherException extends RuntimeException {
        private final Date property1;

        public OtherException(Date property1, Throwable cause) {
            super(String.format("Exception for %s", property1), cause);
            this.property1 = property1;
        }

        @Logged("propA")
        public Date getProperty1() {
            return property1;
        }
    }

}
