/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jvn.JvnException;
import jvn.JvnProxy;
import jvn.JvnServerImpl;

/**
 *
 * @author Kikaha
 */
public class Burster {
    public static void main(String[] args) {        
        // Je crée 5 objets
        List<SentenceItf> ls = new ArrayList<SentenceItf>();
        for (int i = 0; i < 1; i++) {
            try {
                SentenceItf s = (SentenceItf) JvnProxy.createObject(new Sentence(), ((Integer) i).toString());
                ls.add(s);
            } catch (JvnException ex) {
                Logger.getLogger(Burster.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        Random r = new Random();
        for (int i = 0; i < 1000000; i++) {
            if (i%10000 == 0) {
                System.out.println("#" + i);
            }
            SentenceItf s = ls.get(r.nextInt(ls.size()));
            if(r.nextInt()%2 == 0) {
                s.read();
            }
            else {
                s.write(((Double) r.nextDouble()).toString());
            }
        }
    }
}
