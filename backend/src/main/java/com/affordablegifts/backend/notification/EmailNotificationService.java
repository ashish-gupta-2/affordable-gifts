package com.affordablegifts.backend.notification;

import com.affordablegifts.backend.order.OrderDtos.OrderItemResponse;
import com.affordablegifts.backend.order.OrderDtos.OrderResponse;
import com.affordablegifts.backend.order.OrderPlacedEvent;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sends the order confirmation email. It runs after the order is committed and
 * off the request thread, and it never throws: a mail problem must not affect
 * an order that has already been saved.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    /** Sender address; blank means email is not configured. */
    @Value("${app.mail.from:}")
    private String from;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedEvent event) {
        String to = event.customerEmail();
        if (to == null || to.isBlank()) {
            return; // the customer gave no email
        }
        if (from.isBlank()) {
            log.info("Email not configured (GMAIL_USERNAME unset); skipping confirmation for {}",
                    event.order().orderNumber());
            return;
        }
        try {
            sendOrderConfirmation(to, event.order());
            log.info("Sent confirmation email for {}", event.order().orderNumber());
        } catch (Exception e) {
            log.warn("Could not send confirmation email for {}: {}",
                    event.order().orderNumber(), e.getMessage());
        }
    }

    void sendOrderConfirmation(String to, OrderResponse order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Order Confirmation - " + order.orderNumber());
        message.setText(body(order));
        mailSender.send(message);
    }

    static String body(OrderResponse o) {
        StringBuilder text = new StringBuilder();
        text.append("Hi ").append(o.customerName()).append(",\n\n")
                .append("Thank you for shopping with Affordable Gifts!\n")
                .append("Your order ").append(o.orderNumber()).append(" has been placed.\n\n")
                .append(o.quantity()).append(" x ").append(o.packagingName())
                .append(" (every set is identical). Each set contains:\n");
        for (OrderItemResponse item : o.items()) {
            text.append("  - ").append(item.productName()).append(", ").append(item.weight())
                    .append("g  ").append(money(item.unitPrice())).append('\n');
        }
        text.append('\n')
                .append("Products:  ").append(money(o.productsSubtotal())).append('\n')
                .append("Packaging: ").append(money(o.packagingTotal())).append('\n')
                .append("Delivery:  ").append(money(o.deliveryCharge())).append('\n')
                .append("Total:     ").append(money(o.total())).append("\n\n")
                .append("Delivering to: ").append(o.addressLine()).append("\n\n")
                .append("Payment is pending. We will contact you on ").append(o.customerPhone())
                .append(" to confirm.\n");
        return text.toString();
    }

    /** 590.00 -> "Rs 590", 160.50 -> "Rs 160.50". */
    private static String money(BigDecimal amount) {
        boolean whole = amount.stripTrailingZeros().scale() <= 0;
        return "Rs " + (whole ? amount.toBigInteger().toString() : amount.setScale(2).toPlainString());
    }
}
