package rw.parkshare.backend.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.payment.entity.Payment;
import rw.parkshare.backend.payment.entity.PaymentStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByReservation_Id(Long reservationId);

    Optional<Payment> findByPaymentReference(String paymentReference);

    Optional<Payment> findByTransactionReference(String transactionReference);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByPaymentMethod(String paymentMethod);

    List<Payment> findByCurrency(String currency);

    List<Payment> findByStatusAndPaymentMethod(
            PaymentStatus status,
            String paymentMethod
    );

    boolean existsByPaymentReference(String paymentReference);

    boolean existsByTransactionReference(String transactionReference);
}
