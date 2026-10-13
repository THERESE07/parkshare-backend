package rw.parkshare.backend.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.payment.entity.PaymentTransaction;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByPayment_Id(Long paymentId);

    Optional<PaymentTransaction> findByTransactionReference(
            String transactionReference
    );

    Optional<PaymentTransaction> findByProviderTransactionId(
            String providerTransactionId
    );

    List<PaymentTransaction> findByProvider(String provider);

    List<PaymentTransaction> findByStatus(String status);

    List<PaymentTransaction> findByCurrency(String currency);

    List<PaymentTransaction> findByPayment_IdAndStatus(
            Long paymentId,
            String status
    );

    boolean existsByTransactionReference(String transactionReference);

    boolean existsByProviderTransactionId(String providerTransactionId);
}
