package io.sugar6400;

import org.apache.log4j.BasicConfigurator;

public class Main {
    // mainメソッド
    // Robotを起動します
    public static void main(String[] args){
        BasicConfigurator.configure();
        new Robot2(args);
    }
}
