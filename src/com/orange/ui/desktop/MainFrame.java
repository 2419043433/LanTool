/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.orange.ui.desktop;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.orange.base.ParamKeys;
import com.orange.base.Params;
import com.orange.client_manage.ClientInfo;
import com.orange.interfaces.CommandId;
import com.orange.interfaces.ICommandProcessor;
import com.orange.interfaces.IMessageHandler;
import com.orange.interfaces.MessageId;
import com.orange.system.SystemInfo;
import com.orange.system.SystemInfo.Keys;

/**
 * 
 * @author niuyunyun
 */
public class MainFrame extends javax.swing.JFrame implements ICommandProcessor {
	private static final long serialVersionUID = 1L;

	private javax.swing.JTree mTree;
	private JScrollPane mScrollPane;
	private DefaultMutableTreeNode mRoot;
	private DefaultTreeModel mTreeModel;
	private IMessageHandler mMessageHandler;

	public MainFrame(IMessageHandler handler) {
		mMessageHandler = handler;

		initComponents();
		setupListeners();
		updateLanguage();
		updateTheme();
	}

	private void initComponents() {
		mRoot = new DefaultMutableTreeNode();
		mTreeModel = new DefaultTreeModel(mRoot);

		mTree = new javax.swing.JTree();
		mTree.setModel(mTreeModel);
		mTree.setPreferredSize(new Dimension(600, 400));
		mTree.expandPath(new TreePath(mRoot));

		mScrollPane = new JScrollPane();
		mScrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		mScrollPane.setViewportView(mTree);

		getContentPane().add(mScrollPane);
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		pack();
		setSize(800, 600);
		setLocationRelativeTo(null);
		setupMenu();
	}

	private void setupListeners() {
		mTree.addMouseListener(mTreeMouseAdapter);
	}

	public void updateLanguage() {
		mRoot.setUserObject("局域网在线成员");
		mTreeModel.nodeChanged(mRoot);
		setTitle("局域网通信助手");
	}

	public void updateTheme() {

	}

	boolean contains(String member) {
		boolean ret = false;
		for (int i = 0; i < mRoot.getChildCount(); ++i) {
			TreeNode child = mRoot.getChildAt(i);
			if (child.toString().equals(member)) {
				ret = true;
				break;
			}
		}

		return ret;
	}

	public void addMember(ClientInfo info) {

		if (contains(info.toString())) {
			return;
		}

		DefaultMutableTreeNode item = new DefaultMutableTreeNode(info);
		mRoot.add(item);
		mTreeModel.nodeStructureChanged(mRoot);
		mTree.expandPath(new TreePath(mRoot));
		Logger.getLogger("MainFrame").log(Level.INFO, "add member " + info);
	}

	private JPopupMenu mTreeMenu = new JPopupMenu();
	private JMenuItem mTreeMenu_TransferFile;

	private void setupMenu() {
		mTreeMenu_TransferFile = new JMenuItem("发送文件");
		mTreeMenu_TransferFile.setActionCommand("send_file");
		mTreeMenu_TransferFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("send_file")) {
					TreePath path = mTree.getSelectionPath();
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
							.getLastPathComponent();
					ClientInfo info = (ClientInfo) node.getUserObject();
					Params param = Params.obtain().put(ParamKeys.ClientInfo,
							info);
					mMessageHandler.handleMessage(
							MessageId.ShowFileTransferWidget, param, null);
				}
			}
		});
		mTreeMenu.add(mTreeMenu_TransferFile);
	}

	private MouseAdapter mTreeMouseAdapter = new MouseAdapter() {
		public void mouseReleased(MouseEvent e) {
			switch (e.getButton()) {
			case MouseEvent.BUTTON3: {
				TreePath treePath = mTree
						.getPathForLocation(e.getX(), e.getY());
				mTree.setSelectionPath(treePath);

				mTreeMenu.add(mTreeMenu_TransferFile);
				mTreeMenu.show(e.getComponent(), e.getX(), e.getY());
			}
				break;
			case MouseEvent.BUTTON1: {
				//double click
				if (e.getClickCount() == 2) {
					TreePath path = mTree.getSelectionPath();
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
							.getLastPathComponent();
					ClientInfo info = (ClientInfo) node.getUserObject();
					Params param = Params.obtain().put(ParamKeys.ClientInfo,
							info);
					mMessageHandler.handleMessage(MessageId.ShowOneOneChatWidget, param, null);
				}
			}
				break;
			default:
				break;
			}
		}
	};

	@Override
	public boolean processCommand(CommandId id, Params param, Params result) {
		boolean ret = true;
		switch (id) {
		case AddMember: {
			ClientInfo info = (ClientInfo) param.get(ParamKeys.ClientInfo);
			addMember(info);
		}
			break;

		default:
			break;
		}
		return ret;
	}

}
