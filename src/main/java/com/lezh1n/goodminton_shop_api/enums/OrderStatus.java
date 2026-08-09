package com.lezh1n.goodminton_shop_api.enums;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    SHIPPING,
    DELIVERED,
    COMPLETED,
    CANCELLED
    // RETURN_REQUESTED was here. Nothing set it and no transition reached it, so
    // no row can hold it - but the admin UI offered it as a filter, which could
    // only ever come back empty. The Postgres enum keeps the value: removing one
    // means recreating the type and rewriting every dependent column, which is a
    // real outage for something that costs nothing to leave.
}
