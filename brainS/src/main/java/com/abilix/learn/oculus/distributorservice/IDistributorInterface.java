/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\oculus2\\DistributorService\\src\\main\\aidl\\com\\abilix\\learn\\oculus\\distributorservice\\IDistributorInterface.aidl
 */
package com.abilix.learn.oculus.distributorservice;
// Declare any non-default types here with import statements

public interface IDistributorInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements IDistributorInterface
{
private static final String DESCRIPTOR = "com.abilix.learn.oculus.distributorservice.IDistributorInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.abilix.learn.oculus.distributorservice.IDistributorInterface interface,
 * generating a proxy if needed.
 */
public static IDistributorInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof IDistributorInterface))) {
return ((IDistributorInterface)iin);
}
return new Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_handAction:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.handAction(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setDisBack:
{
data.enforceInterface(DESCRIPTOR);
DistributorBack _arg0;
_arg0 = DistributorBack.Stub.asInterface(data.readStrongBinder());
this.setDisBack(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements IDistributorInterface
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void handAction(int type) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(type);
mRemote.transact(Stub.TRANSACTION_handAction, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
//执行动作状态

@Override public void setDisBack(DistributorBack discallback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((discallback!=null))?(discallback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_setDisBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_handAction = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_setDisBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public void handAction(int type) throws android.os.RemoteException;
//执行动作状态

public void setDisBack(DistributorBack discallback) throws android.os.RemoteException;
}
