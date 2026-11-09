package rw.parkshare.backend.pricing;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.admin.repository.OperatorVerificationRepository;
import rw.parkshare.backend.parking.repository.ParkingLotRepository;

/**
 * Decides whether VAT is charged on a lot's parking.
 *
 * In Rwanda VAT is collected and paid to RRA by the seller of the service,
 * here the lot operator, and only if that operator is VAT-registered
 * (mandatory above RWF 20M turnover a year or 5M in a quarter; voluntary below).
 * We charge VAT only when an admin has VERIFIED the operator and confirmed
 * VAT registration (TIN + RRA VAT certificate). A self-declaration alone
 * does not count.
 */
@Component
@Transactional(readOnly = true)
public class VatPolicy {

    private final ParkingLotRepository parkingLotRepository;
    private final OperatorVerificationRepository verificationRepository;

    public VatPolicy(ParkingLotRepository parkingLotRepository,
                     OperatorVerificationRepository verificationRepository) {
        this.parkingLotRepository = parkingLotRepository;
        this.verificationRepository = verificationRepository;
    }

    public boolean appliesTo(Long parkingLotId) {
        return parkingLotRepository.findById(parkingLotId)
                .filter(lot -> lot.getOperator() != null)
                .flatMap(lot -> verificationRepository.findByUser_Id(lot.getOperator().getId()))
                .map(v -> "VERIFIED".equals(v.getVerificationStatus()) && v.isVatRegistered())
                .orElse(false);
    }
}
