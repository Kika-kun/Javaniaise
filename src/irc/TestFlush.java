/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jvn.JvnException;
import jvn.JvnProxy;
import jvn.JvnServerImpl;

/**
 *
 * @author Loic
 */
public class TestFlush {
    public static void main(String[] args) throws JvnException {
    List<SentenceItf> ls = new ArrayList<SentenceItf>();
        for (int i = 0; i < 5; i++) {
            try {
                SentenceItf s = (SentenceItf) JvnProxy.createObject(new Sentence(), ((Integer) i).toString());
                ls.add(s);
            } catch (JvnException ex) {
                Logger.getLogger(Burster.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        JvnServerImpl.jvnGetServer().jvnFlush();
        for (int i = 5; i < 10; i++) {
            try {
                SentenceItf s = (SentenceItf) JvnProxy.createObject(new Sentence(), ((Integer) i).toString());
                ls.add(s);
            } catch (JvnException ex) {
                Logger.getLogger(Burster.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
        
    }
}
