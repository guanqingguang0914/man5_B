package com.abilix.control.aidl;
import  com.abilix.control.aidl.Control;
import com.abilix.control.aidl.IPushListener;
interface IControl {
   void ControlInterface(in Control mBrain);
   void controlSkillPlayer(int state,String filePath);
   byte[] request(in byte[] data);
   byte[] requestTimeout(in byte[] data,int timeout);
   void cancelRequestTimeout();
   byte[] requestVice(in byte[] data);
   int getRobotType();
   void registerPush(IPushListener mListener,String fullClassName);
   void unregisterPush(IPushListener mListener,String fullClassName);
   void write(in byte[] data);
   void writeVice(in byte[] data);
   void destorySP();
}