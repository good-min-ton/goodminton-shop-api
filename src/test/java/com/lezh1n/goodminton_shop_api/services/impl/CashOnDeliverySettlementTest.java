package com.lezh1n.goodminton_shop_api.services.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.Test;

import com.lezh1n.goodminton_shop_api.entities.Order;
import com.lezh1n.goodminton_shop_api.entities.Payment;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.enums.PaymentMethod;
import com.lezh1n.goodminton_shop_api.enums.PaymentStatus;

/**
 * A cash-on-delivery order used to finish as COMPLETED while its payment still
 * read PENDING: card and PayOS settle from their webhooks and in-store sales are
 * marked paid at the counter, but COD had no path to PAID at all.
 *
 * <p>The private helpers are exercised directly. Driving them through the
 * service would need the whole security and persistence stack for logic that is
 * a few lines of state, and this keeps the assertions about the rules rather
 * than about the wiring.
 */
class CashOnDeliverySettlementTest {

    private final OrderServiceImpl service = newService();

    /**
     * Built reflectively so adding a collaborator to OrderServiceImpl does not
     * break this file - counting nulls by hand already did once. Everything is
     * null except the event publisher, which moveTo actually calls.
     */
    private static OrderServiceImpl newService() {
        try {
            Constructor<?> ctor = OrderServiceImpl.class.getDeclaredConstructors()[0];
            Class<?>[] types = ctor.getParameterTypes();
            Object[] args = new Object[types.length];
            for (int i = 0; i < types.length; i++) {
                if (types[i] == ApplicationEventPublisher.class) {
                    args[i] = (ApplicationEventPublisher) event -> {
                        // Notifications are covered separately; here they are noise.
                    };
                }
            }
            ctor.setAccessible(true);
            return (OrderServiceImpl) ctor.newInstance(args);
        } catch (Exception e) {
            throw new IllegalStateException("could not build OrderServiceImpl", e);
        }
    }

    private void settle(Order order) throws Exception {
        Method m = OrderServiceImpl.class.getDeclaredMethod("settleCashOnDelivery", Order.class);
        m.setAccessible(true);
        m.invoke(service, order);
    }

    private void moveTo(Order order, OrderStatus status) throws Exception {
        Method m = OrderServiceImpl.class.getDeclaredMethod("moveTo", Order.class, OrderStatus.class);
        m.setAccessible(true);
        m.invoke(service, order, status);
    }

    private static Payment payment(PaymentMethod method, PaymentStatus status) {
        return Payment.builder()
                .method(method)
                .status(status)
                .amount(BigDecimal.valueOf(100_000))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private static Order orderWith(Payment... payments) {
        return Order.builder()
                .status(OrderStatus.SHIPPING)
                .totalAmount(BigDecimal.valueOf(100_000))
                .orderDate(LocalDateTime.now())
                .payments(new ArrayList<>(List.of(payments)))
                .orderItems(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("a pending COD payment is settled, with the time it was collected")
    void codIsSettledOnDelivery() throws Exception {
        Order order = orderWith(payment(PaymentMethod.COD, PaymentStatus.PENDING));

        settle(order);

        Payment cod = order.getPayments().get(0);
        assertThat(cod.getStatus()).isEqualTo(PaymentStatus.PAID);
        // paid_at is the other half of the bug: without it there is no record of
        // when the money actually arrived.
        assertThat(cod.getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("a bank transfer is left alone - nobody has verified it")
    void bankTransferIsNotSettled() throws Exception {
        Order order = orderWith(payment(PaymentMethod.BANKING, PaymentStatus.PENDING));

        settle(order);

        assertThat(order.getPayments().get(0).getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(order.getPayments().get(0).getPaidAt()).isNull();
    }

    @Test
    @DisplayName("an already-settled payment keeps its original paid_at")
    void settlementIsIdempotent() throws Exception {
        Payment cod = payment(PaymentMethod.COD, PaymentStatus.PAID);
        LocalDateTime original = LocalDateTime.now().minusDays(1);
        cod.setPaidAt(original);
        Order order = orderWith(cod);

        settle(order);

        // Re-running delivery must not rewrite when the money arrived.
        assertThat(cod.getPaidAt()).isEqualTo(original);
    }

    @Test
    @DisplayName("a failed payment is not resurrected by delivery")
    void failedPaymentStaysFailed() throws Exception {
        Order order = orderWith(payment(PaymentMethod.COD, PaymentStatus.FAILED));

        settle(order);

        assertThat(order.getPayments().get(0).getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("only the COD line of a mixed-payment order is settled")
    void onlyCodIsTouched() throws Exception {
        Order order = orderWith(
                payment(PaymentMethod.COD, PaymentStatus.PENDING),
                payment(PaymentMethod.PAYOS, PaymentStatus.PENDING));

        settle(order);

        assertThat(order.getPayments().get(0).getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(order.getPayments().get(1).getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("an order with no payment row does not blow up")
    void noPaymentsIsSafe() throws Exception {
        Order order = orderWith();

        settle(order);

        assertThat(order.getPayments()).isEmpty();
    }

    @Test
    @DisplayName("every status change stamps the clock the stuck-order queues read")
    void statusChangeStampsTheClock() throws Exception {
        Order order = orderWith();
        LocalDateTime before = order.getStatusChangedAt();
        Thread.sleep(5);

        moveTo(order, OrderStatus.DELIVERED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.getStatusChangedAt()).isAfter(before);
    }
}
