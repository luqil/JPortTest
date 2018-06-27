package com.luqili.htools.jporttest;

import java.util.regex.Pattern;

public class T2 {
    
    public static void main(String[] args) {
        String a="baidu.com-1500-85.pt";
        if(Pattern.matches("^[0-9a-zA-Z.]{1,100}-[0-9]{1,5}-[0-9]{1,3}.pt$", a)) {
            System.out.println("aaa");
        }
    }
    
}
