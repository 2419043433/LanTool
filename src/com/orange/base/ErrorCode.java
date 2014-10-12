package com.orange.base;

public enum ErrorCode {
	ErrorUnknown,
	ErrorFile,
	ErrorFileNotFound,
	ErrorFileRead,
	ErrorFileWrite,
	
	ErrorNetwork,
	ErrorNetworkAcceptFailed,
	ErrorNetworkPortAlreadyOccupied,
	ErrorNetworkConnect,
	ErrorNetworkDisconnect,
	ErrorNetworkRead,
	ErrorNetworkWrite,
	
	ErrorState,
	ErrorIllegalState,
}
