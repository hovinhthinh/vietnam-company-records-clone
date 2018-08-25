/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class CollaborationQueue<T> {

    private final Queue<T> queue = new LinkedList<>();
    private int numberOfCollaborators;

    private final Object lock = new Object();
    private int currentWaitCount = 0;
    public boolean isEnded = false;

    public CollaborationQueue(int nCollaborators) {
        if (nCollaborators < 1) {
            throw new RuntimeException("nCollaborators must be positive.");
        }
        numberOfCollaborators = nCollaborators;
    }

    public boolean push(T element) {
        synchronized (lock) {
            boolean result = queue.add(element);
            lock.notify();
            return result;
        }
    }

    public T pop() {
        synchronized (lock) {
            try {
                while (true) {
                    if (!queue.isEmpty()) {
                        return queue.poll();
                    }
                    ++currentWaitCount;
                    if (currentWaitCount == numberOfCollaborators) {
                        isEnded = true;
                        lock.notify();
                        return null;
                    }
                    lock.wait();
                    --currentWaitCount;
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(CollaborationQueue.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
    }
}
