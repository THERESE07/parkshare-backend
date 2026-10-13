package rw.parkshare.backend.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.payment.entity.Refund;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByPayment_Id(Long paymentId);

    List<Refund> findByStatus(String status);

    List<Refund> findByCurrency(String currency);

    List<Refund> findByProviderRefundId(String providerRefundId);

    List<Refund> findByPayment_IdAndStatus(
            Long paymentId,
            String status
    );

    boolean existsByProviderRefundId(String providerRefundId);
}