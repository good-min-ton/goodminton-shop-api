package com.lezh1n.goodminton_shop_api.enums;

/**
 * Why a notification was raised.
 *
 * <p>Stored as text rather than a Postgres enum: these churn faster than the
 * order lifecycle does, and a Postgres enum cannot drop a value once added.
 */
public enum NotificationType {
    /** A new order is waiting for a SUPER_ADMIN to confirm it. */
    ORDER_AWAITING_CONFIRMATION,
    /** Confirmed - the store now has to prepare it. */
    ORDER_CONFIRMED,
    /** The store is preparing the customer's order. */
    ORDER_PREPARING,
    /** Handed to the carrier; carries the tracking code. */
    ORDER_SHIPPING,
    /** Delivered - the customer still has to confirm receipt. */
    ORDER_DELIVERED,
    /** The customer confirmed receipt, or the scheduler closed it out. */
    ORDER_COMPLETED,
    /** Cancelled, by the customer or by the payment-timeout scheduler. */
    ORDER_CANCELLED
}
