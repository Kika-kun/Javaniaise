/**
 * *
 * Irc class : simple implementation of a chat using JAVANAISE Contact:
 *
 * Authors:
 */
package irc;

import java.awt.*;
import java.awt.event.*;

import jvn.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Irc2 {

    public TextArea text;
    public TextField data;
    Frame frame;
    SentenceItf sentence;

    /**
     * main method create a JVN object nammed IRC for representing the Chat
     * application
     *
     */
    public static void main(String argv[]) {
        try {
            SentenceItf sentence = (SentenceItf) JvnProxy.createObject(new Sentence(), "IRC");
            new Irc2(sentence);

        } catch (JvnException ex) {
            Logger.getLogger(Irc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * IRC Constructor
     *     *
     * @param s
     */
    public Irc2(SentenceItf s) {
        sentence = s;
        frame = new Frame();
        frame.setLayout(new GridLayout(1, 1));
        text = new TextArea(10, 60);
        text.setEditable(false);
        text.setForeground(Color.red);
        frame.add(text);
        data = new TextField(40);
        frame.add(data);
        Button read_button = new Button("read");
        read_button.addActionListener(rl);
        frame.add(read_button);
        Button write_button = new Button("write");
        write_button.addActionListener(wl);
        frame.add(write_button);
        frame.setSize(545, 201);
        text.setBackground(Color.black);
        frame.setVisible(true);
    }
    
    
    final ActionListener rl = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            // invoke the method
            String s = sentence.read();

            // display the read value
            data.setText(s);
            text.append(s + "\n");
        }
    };
    
    
    final ActionListener wl = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            String s = data.getText();

            sentence.write(s);
        }
    };
}