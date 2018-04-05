/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.logging.Level;

/**
 *
 * @author millerti
 */
public class Logger extends PrintStream {
    public static final Logger out;
    private static final SnooperStream snooper;
    private static OutputStream systemOut;
    static {
        Class myClass = System.out.getClass();
        Field myField = getField(myClass, "out");
        myField.setAccessible(true);
        try {
            systemOut = (OutputStream)myField.get(System.out);
        } catch (Exception ex) {
            System.err.println("Unable to get OutputStream for System.out");
            System.exit(0);
        }
//        System.err.println("Class of systemOut is " + systemOut.getClass().getCanonicalName());
        snooper = new SnooperStream(systemOut);
        out = new Logger();
    }
    
    static private class SnooperStream extends OutputStream {
        int newline_count = 0;
        OutputStream out;

        @Override
        public void write(int b) throws IOException {
            if (b == '\n') newline_count++;
            out.write(b);
        }
        
        public void write(byte b[], int off, int len) throws IOException {
            int end = off + len;
            for (int i=off; i<end; i++) {
                if (b[i] == '\n') newline_count++;
            }
            out.write(b, off, len);
        }
        
        public void flush() throws IOException {
            out.flush();
        }

        public void close() throws IOException {
            out.close();
        }
        
        public SnooperStream(OutputStream out) {
            this.out = out;
        }
    }
    
    public static Field getField(Class clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class superClass = clazz.getSuperclass();
            if (superClass == null) {
                return null;
            } else {
                return getField(superClass, fieldName);
            }
        }
    }
  
    public Logger() {
        super(snooper, true);
    }
    
    public int getLineCount() {
        return snooper.newline_count;
    }
    
    public void clearLineCount() {
        snooper.newline_count = 0;
    }
    
    public void advanceClock() {
        if (getLineCount() > 0) {
            out.println();
            clearLineCount();
        }
    }
    
}
