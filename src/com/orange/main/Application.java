/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.orange.main;

import com.orange.controller.Controller;
import com.orange.ui.MainFrame;

/**
 *
 * @author niuyunyun
 */
public class Application {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Controller controller = new Controller();
        controller.start();
    }
}
