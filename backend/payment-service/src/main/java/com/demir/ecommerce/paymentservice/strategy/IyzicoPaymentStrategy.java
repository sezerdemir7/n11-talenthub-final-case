package com.demir.ecommerce.paymentservice.strategy;

import com.demir.ecommerce.paymentservice.dto.PaymentRequest;
import com.demir.ecommerce.paymentservice.dto.PaymentResult;
import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.CreatePaymentRequest;
import com.iyzipay.request.CreateRefundRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class IyzicoPaymentStrategy implements PaymentStrategy {

    @Value("${iyzico.api-key}")
    private String apiKey;

    @Value("${iyzico.secret-key}")
    private String secretKey;

    @Value("${iyzico.base-url}")
    private String baseUrl;

    @Override
    public PaymentResult pay(PaymentRequest request) {

        BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);

        CreatePaymentRequest iyzicoRequest = new CreatePaymentRequest();
        iyzicoRequest.setLocale(Locale.TR.getValue());
        iyzicoRequest.setConversationId(String.valueOf(request.orderId()));
        iyzicoRequest.setPrice(amount);
        iyzicoRequest.setPaidPrice(amount);
        iyzicoRequest.setCurrency(Currency.TRY.name());
        iyzicoRequest.setInstallment(1);
        iyzicoRequest.setBasketId("ORDER-" + request.orderId());
        iyzicoRequest.setPaymentChannel(PaymentChannel.WEB.name());
        iyzicoRequest.setPaymentGroup(PaymentGroup.PRODUCT.name());

        iyzicoRequest.setPaymentCard(createPaymentCard(request));
        iyzicoRequest.setBuyer(createDemoBuyer(request));
        iyzicoRequest.setShippingAddress(createDemoAddress());
        iyzicoRequest.setBillingAddress(createDemoAddress());
        iyzicoRequest.setBasketItems(createDemoBasketItems(request, amount));

        Payment payment = Payment.create(iyzicoRequest, createOptions());

        if ("success".equalsIgnoreCase(payment.getStatus())) {

            String paymentTransactionId = payment.getPaymentItems()
                    .get(0)
                    .getPaymentTransactionId();

            return new PaymentResult(
                    true,
                    paymentTransactionId,
                    null
            );
        }

        return new PaymentResult(
                false,
                null,
                payment.getErrorMessage()
        );
    }

    @Override
    public PaymentResult refund(String paymentTransactionId, BigDecimal amount) {

        CreateRefundRequest refundRequest = new CreateRefundRequest();
        refundRequest.setLocale(Locale.TR.getValue());
        refundRequest.setConversationId(paymentTransactionId);
        refundRequest.setPaymentTransactionId(paymentTransactionId);
        refundRequest.setPrice(amount.setScale(2, RoundingMode.HALF_UP));
        refundRequest.setCurrency(Currency.TRY.name());
        refundRequest.setIp("127.0.0.1");
        refundRequest.setReason(RefundReason.OTHER);
        refundRequest.setDescription("Order cancelled by user");

        Refund refund = Refund.create(refundRequest, createOptions());

        if (Status.SUCCESS.getValue().equalsIgnoreCase(refund.getStatus())) {
            return new PaymentResult(
                    true,
                    refund.getPaymentTransactionId(),
                    null
            );
        }

        return new PaymentResult(
                false,
                null,
                refund.getErrorMessage()
        );
    }

    private Options createOptions() {
        Options options = new Options();
        options.setApiKey(apiKey);
        options.setSecretKey(secretKey);
        options.setBaseUrl(baseUrl);
        return options;
    }

    private PaymentCard createPaymentCard(PaymentRequest request) {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setCardHolderName(request.card().cardHolderName());
        paymentCard.setCardNumber(request.card().cardNumber());
        paymentCard.setExpireMonth(request.card().expireMonth());
        paymentCard.setExpireYear(request.card().expireYear());
        paymentCard.setCvc(request.card().cvc());
        paymentCard.setRegisterCard(0);
        return paymentCard;
    }

    private Buyer createDemoBuyer(PaymentRequest request) {
        Buyer buyer = new Buyer();
        buyer.setId(String.valueOf(request.userId()));
        buyer.setName("Demo");
        buyer.setSurname("User");
        buyer.setGsmNumber("+905350000000");
        buyer.setEmail("demo@example.com");
        buyer.setIdentityNumber("74300864791");
        buyer.setLastLoginDate("2026-04-30 12:00:00");
        buyer.setRegistrationDate("2026-04-30 12:00:00");
        buyer.setRegistrationAddress("Demo Address");
        buyer.setIp("127.0.0.1");
        buyer.setCity("Istanbul");
        buyer.setCountry("Turkey");
        buyer.setZipCode("34000");
        return buyer;
    }

    private Address createDemoAddress() {
        Address address = new Address();
        address.setContactName("Demo User");
        address.setCity("Istanbul");
        address.setCountry("Turkey");
        address.setAddress("Demo Address");
        address.setZipCode("34000");
        return address;
    }

    private List<BasketItem> createDemoBasketItems(PaymentRequest request, BigDecimal amount) {
        BasketItem basketItem = new BasketItem();
        basketItem.setId("ORDER-" + request.orderId());
        basketItem.setName("Ecommerce Order");
        basketItem.setCategory1("Ecommerce");
        basketItem.setItemType(BasketItemType.PHYSICAL.name());
        basketItem.setPrice(amount);

        return List.of(basketItem);
    }
}
