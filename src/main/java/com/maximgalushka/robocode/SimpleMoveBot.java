package com.maximgalushka.robocode;

import robocode.Robot;

/**
 * <p></p>
 *
 * @author Maxim Galushka
 * @since 15.06.12
 */
public class SimpleMoveBot extends Robot {

    @Override
    public void run() {
        while (true) {
            ahead(100);
            back(100);
        }
    }
}
