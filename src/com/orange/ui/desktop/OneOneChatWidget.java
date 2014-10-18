package com.orange.ui.desktop;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.orange.base.ParamKeys;
import com.orange.base.Params;
import com.orange.client_manage.ClientInfo;
import com.orange.interfaces.IMessageHandler;
import com.orange.interfaces.MessageId;

public class OneOneChatWidget extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IMessageHandler mMessageHandler;
	private JScrollPane mScrollPane;
	private JButton mSelectFileButton;
	private JPanel mSelectArea;
	private JTextField mTextArea;
	private ClientInfo mClientInfo;

	public OneOneChatWidget(IMessageHandler messageHandler) {
		mMessageHandler = messageHandler;

		initComponents();
		setupListeners();
		updateLanguage();
		updateTheme();
	}
	
	public void setClientInfo(ClientInfo info)
	{
		mClientInfo = info;
		setTitle(info.toString());
	}

	private void initComponents() {
		Container container = getContentPane();
		container.setLayout(new BoxLayout(container,  BoxLayout.Y_AXIS));
		mScrollPane = new JScrollPane();
		mScrollPane.setPreferredSize(new Dimension(600,400));
		
		mSelectFileButton = new JButton();
		
		mSelectArea = new JPanel();
		mSelectArea.setLayout(new BoxLayout(mSelectArea, BoxLayout.X_AXIS));
		mSelectArea.add(mSelectFileButton);
		mSelectArea.setSize(600,50);
		
		mTextArea = new JTextField();
		mTextArea.setSize(800,150);
		
		container.add(mScrollPane);
		container.add(mSelectArea);
		container.add(mTextArea);
		
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		pack();
		setSize(800, 600);
		setLocationRelativeTo(null);
	}

	private void setupListeners() {
		mSelectFileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Params param = Params.obtain().put(ParamKeys.ClientInfo,
						mClientInfo);
				mMessageHandler.handleMessage(
						MessageId.ShowFileTransferWidget, param, null);
			}
		});
	}

	private void updateTheme() {
	}

	private void updateLanguage() {
		mSelectFileButton.setText("发送文件");
	}

}
