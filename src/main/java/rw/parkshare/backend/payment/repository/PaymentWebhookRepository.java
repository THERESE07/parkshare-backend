package rw.parkshare.backend.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.payment.entity.PaymentWebhook;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentWebhookRepository extends JpaRepository<PaymentWebhook, Long> {

    Optional<PaymentWebhook> findByProviderAndEventId(
            String provider,
            String eventId
    );

    List<PaymentWebhook> findByProvider(String provider);

    List<PaymentWebhook> findByEventType(String eventType);

    List<PaymentWebhook> findByProcessed(boolean processed);

    List<PaymentWebhook> findByProviderAndProcessed(
            String provider,
            boolean processed
    );

    boolean existsByProviderAndEventId(
            String provider,
            String eventId
    );
}
