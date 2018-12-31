/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: IStartActivityConfirmService.aidl
 */
package com.github.kr328.sac;

public interface IStartActivityConfirmService extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements IStartActivityConfirmService {
        private static final String DESCRIPTOR = "com.github.kr328.sac.IStartActivityConfirmService";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an com.github.kr328.sac.IStartActivityConfirmService interface,
         * generating a proxy if needed.
         */
        public static IStartActivityConfirmService asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof IStartActivityConfirmService))) {
                return ((IStartActivityConfirmService) iin);
            }
            return new Proxy(obj);
        }

        @Override
        public android.os.IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException {
            String descriptor = DESCRIPTOR;
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(descriptor);
                    return true;
                }
                case TRANSACTION_startConfirm: {
                    data.enforceInterface(descriptor);
                    int _arg0;
                    _arg0 = data.readInt();
                    String _arg1;
                    _arg1 = data.readString();
                    String _arg2;
                    _arg2 = data.readString();
                    IConfirmCallback _arg3;
                    _arg3 = IConfirmCallback.Stub.asInterface(data.readStrongBinder());
                    long _result = this.startConfirm(_arg0, _arg1, _arg2, _arg3);
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                }
                default: {
                    return super.onTransact(code, data, reply, flags);
                }
            }
        }

        private static class Proxy implements IStartActivityConfirmService {
            private android.os.IBinder mRemote;

            Proxy(android.os.IBinder remote) {
                mRemote = remote;
            }

            @Override
            public android.os.IBinder asBinder() {
                return mRemote;
            }

            public String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            @Override
            public long startConfirm(int requestId, String source, String target, IConfirmCallback callback) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                long _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(requestId);
                    _data.writeString(source);
                    _data.writeString(target);
                    _data.writeStrongBinder((((callback != null)) ? (callback.asBinder()) : (null)));
                    mRemote.transact(Stub.TRANSACTION_startConfirm, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
        }

        static final int TRANSACTION_startConfirm = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    }

    public long startConfirm(int requestId, String source, String target, IConfirmCallback callback) throws android.os.RemoteException;
}
