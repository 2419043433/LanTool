package com.orange.ui.desktop;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class FileTransferWidget extends JFrame {
	private static final long serialVersionUID = 1L;

	private JTextField mFilePath;
	private JButton mSelectFileBtn;
	private JButton mSendFileBtn;
	
	private Delegate mDelegate;
	
	public interface Delegate
	{
		void sendFile(String filePath);
	}

	public FileTransferWidget(Delegate delegate) {
		mDelegate = delegate;
		
		initComponents();
		setupListeners();
		updateLanguage();
		updateTheme();
	}

	private void initComponents() {
		mFilePath = new JTextField();
		mFilePath.setPreferredSize(new Dimension(500, 30));
		mFilePath.setEnabled(false);

		mSelectFileBtn = new JButton();
		mSelectFileBtn.setPreferredSize(new Dimension(100, 30));
		
		mSendFileBtn = new JButton();
		mSendFileBtn.setPreferredSize(new Dimension(100, 30));

		Container container = getContentPane();
		container.setLayout(new FlowLayout());
		container.add(mFilePath);
		container.add(mSelectFileBtn);

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
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
				sendFile(mFilePath.getText());
			}
		});
	}

	private void updateLanguage() {
		mFilePath.setText("文件路径");
		mSelectFileBtn.setText("选择...");
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
	
	private void sendFile(String filePath)
	{
		if(null == mDelegate || null == filePath || filePath.isEmpty())
		{
			return;
		}
		mDelegate.sendFile(filePath);
	}

}
