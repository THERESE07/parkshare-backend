package rw.parkshare.backend.notification.sms;

/**
 * Sends SMS to Rwandan numbers. The default implementation only logs;
 * plug in a local gateway (e.g. Africa's Talking or Pindo) by providing
 * another bean that implements this interface.
 */
public interface SmsSender {

    void send(String phone, String message);
}
