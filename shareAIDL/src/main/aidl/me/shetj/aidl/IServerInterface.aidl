// IServerInterface.aidl
package me.shetj.aidl;

// Declare any non-default types here with import statements

import me.shetj.aidl.IClientInterface;

interface IServerInterface {

     void readFromClientMsg(String msg);

     void registerClientInterface(IClientInterface client);

     void unregisterClientInterface(IClientInterface client);
}