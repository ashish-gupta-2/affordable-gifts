package com.affordablegifts.backend.order;

import com.affordablegifts.backend.order.OrderDtos.OrderResponse;

/** Published once for each newly created order (not for idempotent retries). */
public record OrderPlacedEvent(OrderResponse order, String customerEmail) {
}
