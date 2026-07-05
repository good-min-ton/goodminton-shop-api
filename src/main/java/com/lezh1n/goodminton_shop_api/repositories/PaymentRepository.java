package com.lezh1n.goodminton_shop_api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Payment;
import com.lezh1n.goodminton_shop_api.enums.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByOrder_Id(Integer orderId);

    List<Payment> findByOrder_IdAndStatus(Integer orderId, PaymentStatus status);

    Optional<Payment> findByVnpayTxnRef(String vnpayTxnRef);

    Optional<Payment> findByPayosOrderCode(Long payosOrderCode);
}
