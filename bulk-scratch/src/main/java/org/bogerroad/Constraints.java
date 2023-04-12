package org.bogerroad;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Random;

public class Constraints {

    private static final Logger LOGGER = LoggerFactory.getLogger(Constraints.class);

    public static void main(String[] args) {

        final Demo demo = new Demo();

        demo.setA("");
        demo.setA(null);

        LOGGER.info(demo.getA());

        demo.setB("");
        demo.setB(null);

        LOGGER.info(demo.getB());

        demo.setA(callToAnotherMethod(args));
        demo.setB(callToAnotherMethod(args));
    }

//    @org.jetbrains.annotations.NotNull
    private static String callToAnotherMethod(String[] args) {
        return (args.length > 1 && args[0].equals("yes")) ? "a" : "b";
    }

    public static class Demo {

        private String a;
        private String b;

        public void setA(@NotNull String a) {
            this.a = a;
        }

        public String getA() {
            return a;
        }

        public void setB(@NonNull String b) {
            this.b = b;
        }

        public String getB() {
            return b;
        }
    }
}
