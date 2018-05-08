package org.dyndns.fules.ck;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

class FileHelper {
    private static ArrayList<ArrayList> ans = new ArrayList<ArrayList>();
    private static ArrayList<String> name = new ArrayList<String>();
    private static ArrayList<String> frq = new ArrayList<String>();
    private static ArrayList<String> res = new ArrayList<String>();


    private static Context context;

    public FileHelper(Context cont) {
        this.context = cont;

        ans.add(name);
        ans.add(frq);
        ans.add(res);
    }

    public static void readFile() {
        String line;

        try {
            BufferedInputStream bs = new BufferedInputStream(context.getResources().openRawResource(R.raw.dictionary00));
            BufferedReader br = new BufferedReader(new InputStreamReader(bs));

            while((line = br.readLine()) != null) {
                tokenString(line);
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public static void tokenString(String str) {
        String data = null;
        StringTokenizer st = new StringTokenizer(str, "\n\t\r\f");

        name.add(st.nextToken());
        frq.add(st.nextToken());

        while (st.hasMoreTokens()) {
            data = st.nextToken();
        }

        res.add(data);
    }

    public ArrayList<ArrayList> getAns() { return ans; }
}