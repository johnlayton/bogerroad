package org.bogerroad;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordScratch {
    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode("password"));
    }
}
