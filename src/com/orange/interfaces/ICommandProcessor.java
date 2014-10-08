package com.orange.interfaces;

import com.orange.base.Params;

public interface ICommandProcessor {
	boolean processCommand(CommandId id, Params param, Params result);
}
