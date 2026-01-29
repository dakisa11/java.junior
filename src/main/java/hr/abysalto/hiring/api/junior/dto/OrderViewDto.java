package hr.abysalto.hiring.api.junior.dto;

import hr.abysalto.hiring.api.junior.model.BuyerAddress;
import hr.abysalto.hiring.api.junior.model.OrderStatus;
import hr.abysalto.hiring.api.junior.model.OrderItem;
import hr.abysalto.hiring.api.junior.model.PaymentOption;
import lombok.Data;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderViewDto {
    @Setter
    private Long orderNr;
    @Setter
    private Long orderId;
    @Setter
    private OrderStatus orderStatus;
    @Setter
    private LocalDateTime orderTime;
    private PaymentOption paymentOption;
    @Setter
    private BigDecimal totalPrice;
    @Setter
    private BuyerAddress deliveryAddress;
    @Setter
    private String contactNumber;
    @Setter
    private String currency;
    private List<OrderItem> orderItems;

}