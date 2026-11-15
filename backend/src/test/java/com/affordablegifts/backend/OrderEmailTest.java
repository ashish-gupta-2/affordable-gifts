package com.affordablegifts.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderEmailTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @MockitoBean
    JavaMailSender mailSender;

    @BeforeEach
    void seed() {
        jdbc.update("delete from order_items");
        jdbc.update("delete from orders");
        jdbc.update("delete from product_packs");
        jdbc.update("delete from products");
        jdbc.update("delete from packaging_options");
        jdbc.update("insert into packaging_options (id,name,kind,description,price,slot_count,slot_capacity)"
                + " values ('box-2','2 Partition Box','box','2 x 100g',30.00,2,100)");
        for (String[] p : new String[][] {{"almonds", "Almonds", "110"}, {"cashews", "Cashews", "130.50"}}) {
            jdbc.update("insert into products (id,name,category,emoji) values (?,?,?,?)",
                    p[0], p[1], "Dry Fruits", "X");
            jdbc.update("insert into product_packs (product_id,weight_grams,price) values (?,100,?)",
                    p[0], Double.parseDouble(p[2]));
        }
    }

    private org.springframework.test.web.servlet.ResultActions place(String requestId, String email)
            throws Exception {
        return mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
                {"requestId":"%s","packagingId":"box-2","quantity":2,"productIds":["almonds","cashews"],
                 "customer":{"name":"Asha Rao","phone":"9876543210","email":"%s"},
                 "address":{"house":"12","street":"MG Road","area":"Indiranagar","city":"Bengaluru",
                            "state":"Karnataka","pincode":"560001","instructions":""}}"""
                .formatted(java.util.UUID.nameUUIDFromBytes(requestId.getBytes()), email)));
    }

    @Test
    void sendsConfirmationToTheCustomerWithOrderDetails() throws Exception {
        place("m1", "asha@example.com").andExpect(status().isCreated());

        ArgumentCaptor<SimpleMailMessage> sent = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, timeout(3000)).send(sent.capture());
        SimpleMailMessage message = sent.getValue();

        assertEquals("asha@example.com", message.getTo()[0]);
        assertEquals("shop@test.local", message.getFrom());
        assertTrue(message.getSubject().startsWith("Order Confirmation - AG-"));
        String text = message.getText();
        assertTrue(text.contains("Hi Asha Rao"));
        assertTrue(text.contains("Cashews, 100g  Rs 130.50"), text);
        assertTrue(text.contains("Total:     Rs 591"), text); // 481 + 60 + 50
    }

    @Test
    void refusesAnOrderWithoutAnEmailAndSendsNothing() throws Exception {
        place("m2", "").andExpect(status().isBadRequest());
        Thread.sleep(500);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void aMailFailureNeverBreaksThePlacedOrder() throws Exception {
        doThrow(new MailSendException("smtp down")).when(mailSender).send(any(SimpleMailMessage.class));

        place("m3", "asha@example.com").andExpect(status().isCreated());

        verify(mailSender, timeout(3000)).send(any(SimpleMailMessage.class));
        assertEquals(1, jdbc.queryForObject("select count(*) from orders", Integer.class));
    }

    @Test
    void aRetriedCheckoutDoesNotEmailTwice() throws Exception {
        place("m4", "asha@example.com").andExpect(status().isCreated());
        place("m4", "asha@example.com").andExpect(status().isOk());

        verify(mailSender, timeout(3000)).send(any(SimpleMailMessage.class));
        Thread.sleep(500);
        verify(mailSender, org.mockito.Mockito.times(1)).send(any(SimpleMailMessage.class));
    }
}
