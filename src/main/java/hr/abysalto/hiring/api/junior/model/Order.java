package hr.abysalto.hiring.api.junior.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("ORDERS")
@AccessType(AccessType.Type.PROPERTY)
public class Order {
	@Id
	private Long orderNr;
	private Long buyerId;
	@Column("ORDER_STATUS")
	private OrderStatus orderStatus;
	private LocalDateTime orderTime;
	@Column("PAYMENT_OPTION")
	private PaymentOption paymentOption;
	private Long deliveryAddressId;
	private String contactNumber;
	private String currency;
	private String notes;
	private BigDecimal totalPrice;

	@Transient
	public String getPaymentOption() {
		return paymentOption != null ? paymentOption.toString() : null;
	}
	public void setPaymentOption(String paymentOptionString) {
		this.paymentOption = PaymentOption.fromString(paymentOptionString);
	}

	@Transient
	public String getOrderStatus() {
		return orderStatus != null ? orderStatus.toString() : null;
	}
	public void setOrderStatus(String orderStatusString) {
		this.orderStatus = OrderStatus.fromString(orderStatusString);
	}
}
