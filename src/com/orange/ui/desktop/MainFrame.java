/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.orange.ui.desktop;

import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * 
 * @author niuyunyun
 */
public class MainFrame extends javax.swing.JFrame {
	private static final long serialVersionUID = 1L;

	private Delegate mDelegate;
	private javax.swing.JTree mTree;
	private JScrollPane mScrollPane;
	private DefaultMutableTreeNode mRoot;
	private DefaultTreeModel mTreeModel;

	public static interface Delegate {

	}

	public MainFrame(Delegate delegate) {
		mDelegate = delegate;

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
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void setupListeners() {

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

	public void addMember(String member) {

		if (contains(member)) {
			return;
		}

		DefaultMutableTreeNode item = new DefaultMutableTreeNode(member);
		mRoot.add(item);
		mTreeModel.nodeStructureChanged(mRoot);
		mTree.expandPath(new TreePath(mRoot));
		Logger.getLogger("MainFrame").log(Level.INFO, "add member " + member);
	}

}
