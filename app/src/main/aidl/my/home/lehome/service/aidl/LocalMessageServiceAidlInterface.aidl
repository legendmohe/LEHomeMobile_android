// LocalMessageServiceAidlInterface.aidl
package my.home.lehome.service.aidl;

// Declare any non-default types here with import statements

interface LocalMessageServiceAidlInterface {
    boolean sendLocalMessage(String msg);
    void connectServer(String address);
    void disconnectServer();
}
