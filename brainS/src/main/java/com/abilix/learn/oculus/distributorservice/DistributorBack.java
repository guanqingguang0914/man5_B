/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\oculus2\\DistributorService\\src\\main\\aidl\\com\\abilix\\learn\\oculus\\distributorservice\\DistributorBack.aidl
 */
package com.abilix.learn.oculus.distributorservice;
// Declare any non-default types here with import statements

public interface DistributorBack extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements DistributorBack
{
private static final String DESCRIPTOR = "com.abilix.learn.oculus.distributorservice.DistributorBack";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.abilix.learn.oculus.distributorservice.DistributorBack interface,
 * generating a proxy if needed.
 */
public static DistributorBack asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof DistributorBack))) {
return ((DistributorBack)iin);
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
case TRANSACTION_disCallBack:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.disCallBack(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements DistributorBack
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
@Override public void disCallBack(int status) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(status);
mRemote.transact(Stub.TRANSACTION_disCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_disCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void disCallBack(int status) throws android.os.RemoteException;
}
