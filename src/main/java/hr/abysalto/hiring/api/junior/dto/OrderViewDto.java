package hr.abysalto.hiring.api.junior.dto;

import hr.abysalto.hiring.api.junior.model.BuyerAddress;
import hr.abysalto.hiring.api.junior.model.OrderStatus;
import hr.abysalto.hiring.api.junior.model.OrderItem;
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
    private OrderStatus orderStatus;
    @Setter
    private LocalDateTime orderTime;
    @Setter
    private BigDecimal totalPrice;
    @Setter
    private BuyerAddress deliveryAddress;
    private List<OrderItem> orderItems;

}