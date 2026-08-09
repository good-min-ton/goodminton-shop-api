package com.lezh1n.goodminton_shop_api.events;

import com.lezh1n.goodminton_shop_api.enums.OrderStatus;

/**
 * Raised whenever an order moves to a new status.
 *
 * <p>Carries the id rather than the entity: the listener runs after the
 * transaction commits, where a detached entity's lazy associations are no longer
 * loadable.
 */
public record OrderStatusChangedEvent(Integer orderId, OrderStatus status) {
}
