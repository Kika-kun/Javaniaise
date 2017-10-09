/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marionsy
 */
public class CoordinatorLauncher {
    public static void main(String[] args) {
        try {
            jvn.JvnRemoteCoord coordinator = new jvn.JvnCoordImpl();
            System.out.println("Server RMI started");
        } catch (Exception ex) {
            Logger.getLogger(CoordinatorLauncher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
