package android.os;

public final class ServiceManager {
    public static IBinder getService(String name) { throw new IllegalArgumentException("Unsupported"); }
    private static IServiceManager getIServiceManager() {throw new IllegalArgumentException("Unsupported");}
    public static void addService(String name, IBinder service) { throw new RuntimeException("Stub"); }

    private static IServiceManager sServiceManager;
}
