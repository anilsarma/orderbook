package com.tagsgroup.simulators;

import java.io.*;

public class TestIEX {
    public static int sumFile(String file) throws Exception{
        //
        // 1
        // 2
        // 3
        int total = 0;
        BufferedReader is = new BufferedReader(new FileReader(file));
        String str;

        while( true )  {
            try {
                str = is.readLine();
            } catch(IOException e) {
                break;
            }
            try {
                total += Integer.parseInt(str);
            } catch(NumberFormatException e) {
                //
            }
        }
        return total;
    }
}



 