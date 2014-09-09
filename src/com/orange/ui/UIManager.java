/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.orange.ui;

import com.orange.controller.IController;

/**
 *
 * @author niuyunyun
 */
public class UIManager {
    //TODO: replace with IObserver
    private IController mController;
    private MainFrame mMainFrame;
    public UIManager(IController controller) {
        mController = controller;
    }

    public void start() {
        mMainFrame = new MainFrame();
        mMainFrame.init();
        //set show location
        mMainFrame.setVisible(true);
    }
    
        public void onMulticastMsg(String msg) {
        mMainFrame.addMember(msg);
    }
}
