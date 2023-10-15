package com.warrington;

import com.warrington.repl.Repl;

class Main {
    public static void main(String[] args) {
        String user = System.getProperty("user.name");

        if (user == null) {
            user = "anonymous";
        }

        System.out.printf("Hello %s! This is the Monkey programming language!\n", user);

        System.out.println("Feel free to type in commands");

        Repl.start();
    }
}