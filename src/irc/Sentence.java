/**
 * *
 * Sentence class : used for representing the text exchanged between users
 * during a chat application Contact:
 *
 * Authors:
 */
package irc;

public class Sentence implements SentenceItf {

    String data;

    public Sentence() {
        data = new String("");
    }

    @Override
    public void write(String text) {
        data = text;
    }

    @Override
    public String read() {
        return data;
    }

    @Override
    public String toString() {
        return data; //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    

}
