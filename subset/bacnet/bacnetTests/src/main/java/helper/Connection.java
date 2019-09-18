package helper;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.transport.Transport;

public class Connection {

  private LocalDevice localDevice;
  private IpNetwork network;
  private boolean terminate;

  private final int deviceId = (int) Math.floor(Math.random() * 1000.0);

  public Connection(String broadcastAddress, int port) throws Exception {
    this(broadcastAddress, port, IpNetwork.DEFAULT_BIND_IP);
  }

  public Connection(String broadcastAddress, int port, String localAddress) throws Exception {
      init(broadcastAddress, port, localAddress);
  }

  public Connection(String broadcastAddress, int port, String localAddress, CovNotificationAnalizer
          covNotificationAnalizer ) throws Exception {
      init(broadcastAddress, port, localAddress);
      addListener(covNotificationAnalizer);
  }

  private void init(String broadcastAddress, int port, String localAddress) throws Exception {
      network = new IpNetwork(broadcastAddress, port, IpNetwork.DEFAULT_BIND_IP, 0, localAddress);
      System.out.println("Creating LoopDevice id " + deviceId);
      Transport transport = new Transport(network);
      transport.setTimeout(1000);
      localDevice = new LocalDevice(deviceId, transport);
      localDevice.initialize();
  }

    private void addListener(CovNotificationAnalizer covNotificationAnalizer) {
        try {
            localDevice
                    .getEventHandler()
                    .addListener( new Listener(covNotificationAnalizer));
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
            localDevice.terminate();
            localDevice = null;
            throw e;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

  /** @return the terminate */
  public boolean isTerminate() {
    return terminate;
  }

  /** @param terminate the terminate to set */
  public void doTerminate() {
    terminate = true;
    localDevice.terminate();
//    covNotificationAnalizer.processData();
    synchronized (this) {
      notifyAll();
    }
  }

  /** @return the localDevice */
  public LocalDevice getLocalDevice() {
    return localDevice;
  }
}