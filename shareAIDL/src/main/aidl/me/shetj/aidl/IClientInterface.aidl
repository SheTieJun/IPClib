// IClientInterface.aidl
package me.shetj.aidl;

// Declare any non-default types here with import statements

interface IClientInterface {

   String getName();
   void readFromServerMsg(String msg);
}