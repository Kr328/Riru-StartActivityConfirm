package android.os;

public final class ServiceManager {
    public static IBinder getService(String name) { throw new IllegalArgumentException("Unsupported"); }
    private static IServiceManager getIServiceManager() {throw new IllegalArgumentException("Unsupported");}

    private static IServiceManager sServiceManager;
}
