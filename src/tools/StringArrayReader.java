/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.io.BufferedReader;
import java.util.List;

/**
 * Make an array of strings look like a BufferedReader.  Only readLine 
 * actually works.
 * 
 * @author millerti
 */
public class StringArrayReader extends BufferedReader {
    String[] arr;
    int line_num;

    public StringArrayReader(String[] arr) {
        super(null);
        this.arr = arr;
    }
    public StringArrayReader(List<String> arr) {
        super(null);
        this.arr = arr.toArray(new String[0]);
    }

    @Override
    public void close() {}

    @Override
    public void mark(int x) {}

    @Override
    public boolean markSupported() { return false; }

    @Override
    public int read() { return -1; }

    @Override
    public String readLine() {
        if (arr==null || line_num >= arr.length) return null;
        return arr[line_num++];
    }

    @Override
    public boolean ready() { return true; }

    @Override
    public long skip(long n) { return 0; }
}
