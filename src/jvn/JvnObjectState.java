/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jvn;

import jvn.JvnObject.Lock;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sylvain MARION
 */
class JvnObjectState {

    private Lock status;

    private final JvnRemoteServer owner;

    private final List<JvnRemoteServer> listReaders;
    private JvnRemoteServer writer;

    private final String name;
    private final int id;

    public JvnObjectState(JvnRemoteServer owner, String name, int id) {
        this.owner = owner;
        this.listReaders = new ArrayList<JvnRemoteServer>();
        this.name = name;
        this.id = id;
    }

    public Lock getStatus() {
        return status;
    }

    public void setStatus(Lock s) {
        status = s;
    }

    public JvnRemoteServer getOwner() {
        return owner;
    }

    /**
     * @return the listReaders
     */
    public List<JvnRemoteServer> getReaders() {
        return listReaders;
    }

    /**
     * @return the writer
     */
    public JvnRemoteServer getWriter() {
        return writer;
    }

    public void removeWriter() throws JvnException {
        if (writer != null) {
            writer = null;
        } else {
            throw new JvnException("Trying to remove a writer that doesn't exist.");
        }
    }

    public void addWriter(JvnRemoteServer s) throws JvnException {
        if (writer == null) {
            writer = s;
        } else {
            throw new JvnException("Trying to add a writer while it already has one.");
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    public void addReader(JvnRemoteServer s) {
        if (!listReaders.contains(s)) {
            listReaders.add(s);
        }
    }

    public void removeReader(JvnRemoteServer s) {
        if (listReaders.contains(s)) {
            listReaders.remove(s);
        }
    }

}
