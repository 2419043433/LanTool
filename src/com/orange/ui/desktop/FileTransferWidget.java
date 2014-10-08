package com.orange.ui.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import com.orange.base.ParamKeys;
import com.orange.base.Params;
import com.orange.client_manage.ClientInfo;
import com.orange.interfaces.CommandId;
import com.orange.interfaces.ICommandProcessor;
import com.orange.interfaces.IMessageHandler;
import com.orange.interfaces.MessageId;

public class FileTransferWidget extends JFrame implements ICommandProcessor {
	private static final long serialVersionUID = 1L;

	private JTextField mFilePath;
	private JButton mSelectFileBtn;
	private JButton mSendFileBtn;
	private JProgressBar mProgressBar;

	private ClientInfo mClientInfo;

	IMessageHandler mMessageHandler;

	public FileTransferWidget(IMessageHandler handler) {
		mMessageHandler = handler;

		initComponents();
		setupListeners();
		updateLanguage();
		updateTheme();
	}

	public void setClientInfo(ClientInfo info) {
		mClientInfo = info;
	}

	private void initComponents() {
		mFilePath = new JTextField();
		mFilePath.setPreferredSize(new Dimension(500, 30));
		mFilePath.setEnabled(false);

		mSelectFileBtn = new JButton();
		mSelectFileBtn.setPreferredSize(new Dimension(100, 30));

		mSendFileBtn = new JButton();
		mSendFileBtn.setPreferredSize(new Dimension(100, 30));

		mProgressBar = new JProgressBar();
		mProgressBar.setOrientation(JProgressBar.HORIZONTAL);

		mProgressBar.setMinimum(0);

		mProgressBar.setMaximum(100);

		mProgressBar.setValue(0);

		mProgressBar.setStringPainted(true);

		mProgressBar.setPreferredSize(new Dimension(500, 30));

		mProgressBar.setBorderPainted(true);

		mProgressBar.setBackground(Color.PINK);
		mProgressBar.setForeground(Color.BLUE);

		Container container = getContentPane();
		container.add(mFilePath, BorderLayout.WEST);
		container.add(mSelectFileBtn, BorderLayout.CENTER);
		container.add(mSendFileBtn, BorderLayout.EAST);
		container.add(mProgressBar, BorderLayout.SOUTH);

		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		setLocationRelativeTo(null);
		pack();
	}

	private void setupListeners() {
		mSelectFileBtn.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				showFileSelectDialog();
			}
		});

		mSendFileBtn.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Params param = Params.obtain()
						.put(ParamKeys.Path, mFilePath.getText())
						.put(ParamKeys.ClientInfo, mClientInfo);
				mMessageHandler.handleMessage(MessageId.StartFileTransfer,
						param, null);
			}
		});
	}

	private void updateLanguage() {
		mFilePath.setText("文件路径");
		mSelectFileBtn.setText("选择...");
		mSendFileBtn.setText("发送");
	}

	private void updateTheme() {

	}

	private void showFileSelectDialog() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.showDialog(this, "选择");
		File file = fileChooser.getSelectedFile();
		if (null != file && null != file.getName()) {
			mFilePath.setText(file.getAbsolutePath());
		}
	}

	@Override
	public boolean processCommand(CommandId id, Params param, Params result) {
		boolean ret = true;
		switch (id) {
		case OnFileTransferProgressChanged: {
			int progress = param.getInt(ParamKeys.Value);
			mProgressBar.setValue(progress);
		}
			break;

		default:
			break;
		}
		return ret;
	}
}
