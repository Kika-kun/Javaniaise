/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jvn;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marionsy
 */
public class JvnCoordinatorLauncher {

    public static void main(String[] args) {
        try {
            JvnRemoteCoord coordinator = new jvn.JvnCoordImpl();
            System.out.println("Server RMI started");
            /*Scanner s = new Scanner(System.in);
            boolean quit = false;
            while (!quit) {
                String ss = s.nextLine();
                if ("q".equals(ss) || "quit".equals(ss)) {
                    System.exit(0);
                } else if ("flush".equals(ss)) {
                    coordinator.jvnFlushCache();
                }
            }*/
        } catch (Exception ex) {
            Logger.getLogger(JvnCoordinatorLauncher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
