package com.orange.ui;

import java.util.HashMap;
import java.util.Map;

import com.orange.base.ParamKeys;
import com.orange.base.Params;
import com.orange.client_manage.ClientInfo;
import com.orange.interfaces.CommandId;
import com.orange.interfaces.ICommandProcessor;
import com.orange.interfaces.IMessageHandler;
import com.orange.interfaces.MessageId;
import com.orange.ui.desktop.FileTransferWidget;
import com.orange.ui.desktop.MainFrame;

public class UIManager implements ICommandProcessor, IMessageHandler {
	private MainFrame mMainFrame;
	private Map<ClientInfo, FileTransferWidget> mClients = new HashMap<ClientInfo, FileTransferWidget>();
	private IMessageHandler mMessageHandler;

	public UIManager(IMessageHandler handler) {
		mMessageHandler = handler;
	}

	public void showMainFrame() {
		if (null == mMainFrame) {
			mMainFrame = new MainFrame(mMessageHandler);
		}

		mMainFrame.setVisible(true);
	}

	public void hideMainFrame() {
		mMainFrame.setVisible(false);
	}

	public void showFileTransferWidget(ClientInfo info) {
		FileTransferWidget widget = mClients.get(info);
		if (null == widget) {
			widget = new FileTransferWidget(mMessageHandler);
			mClients.put(info, widget);
		}
		widget.setClientInfo(info);
		widget.setVisible(true);
	}

	public void hideFileTransferWidget(ClientInfo info) {
		FileTransferWidget widget = mClients.get(info);
		if (null != widget) {
			widget.setVisible(false);
		}
	}

	@Override
	public boolean handleMessage(MessageId id, Params param, Params result) {
		boolean ret = true;

		return ret;
	}

	@Override
	public boolean processCommand(CommandId id, Params param, Params result) {
		boolean ret = true;
		switch (id) {
		case ShowMainFrame:
			showMainFrame();
			break;
		case ShowFileTransferWidget: {
			ClientInfo info = (ClientInfo) param.get(ParamKeys.ClientInfo);
			showFileTransferWidget(info);
		}
			break;
		case HideMainFrame:
			hideMainFrame();
			break;
		case HideFileTransferWidget: {
			ClientInfo info = (ClientInfo) param.get(ParamKeys.ClientInfo);
			hideFileTransferWidget(info);
		}
			break;
		case AddMember:
			mMainFrame.processCommand(id, param, result);
			break;
		case OnFileTransferProgressChanged: {
			ClientInfo info = (ClientInfo) param.get(ParamKeys.ClientInfo);
			FileTransferWidget widget = mClients.get(info);
			if (null != widget) {
				widget.processCommand(id, param, result);
			}

		}
			break;

		default:
			break;
		}
		return ret;
	}

}
