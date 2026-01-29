package hr.abysalto.hiring.api.junior.dto;

import hr.abysalto.hiring.api.junior.model.BuyerAddress;
import hr.abysalto.hiring.api.junior.model.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderViewDto {
    private Long orderNr;
    private OrderStatus orderStatus;
    private LocalDateTime orderTime;
    private BigDecimal totalPrice;

    private BuyerAddress deliveryAddress;

    public void setOrderNr(Long orderNr) {
        this.orderNr = orderNr;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setDeliveryAddress(BuyerAddress deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
}