/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc;

import java.io.Serializable;
import jvn.LockType;
import static jvn.LockType.Type.READ;
import static jvn.LockType.Type.WRITE;

/**
 *
 * @author Kikaha
 */
public interface SentenceItf extends Serializable {

    @LockType(type = READ)
    String read();

    @LockType(type = WRITE)
    void write(String text);
    
}
