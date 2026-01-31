package hr.abysalto.hiring.api.junior.manager;
import java.util.ArrayList;
import java.util.List;

import hr.abysalto.hiring.api.junior.model.*;
import hr.abysalto.hiring.api.junior.manager.BuyerManager;
import hr.abysalto.hiring.api.junior.repository.BuyerAddressRepository;
import hr.abysalto.hiring.api.junior.repository.OrderItemRepository;
import hr.abysalto.hiring.api.junior.repository.OrderRepository;
import hr.abysalto.hiring.api.junior.dto.OrderViewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderManagerImpl implements OrderManager {
    private final OrderRepository orderRepository;
    private final BuyerAddressRepository buyerAddressRepository;
    private final OrderItemRepository orderItemRepository;
    private final BuyerManager buyerManager;

    public OrderManagerImpl(OrderRepository orderRepository,
                            BuyerAddressRepository buyerAddressRepository,
                            OrderItemRepository orderItemRepository,
                            BuyerManager buyerManager) {
        this.orderRepository = orderRepository;
        this.buyerAddressRepository = buyerAddressRepository;
        this.orderItemRepository = orderItemRepository;
        this.buyerManager = buyerManager;
    }

    @Override
    public List<OrderViewDto> getAllOrders() {
        List<Order> orders = (List<Order>)orderRepository.findAll();
        return getOrderViewDtos(orders);
    }

    @Override
    public void save(Order order) {
        this.orderRepository.save(order);
    }

    @Override
    public Order getById(Long id) {
        return this.orderRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        this.orderRepository.deleteById(id);
    }

    public List<OrderViewDto> getAllBuyerOrders(Long buyerId) {
        List<Order> orders = orderRepository.findByBuyerId(buyerId);
        return getOrderViewDtos(orders);
    }

    private List<OrderViewDto> getOrderViewDtos(List<Order> orders) {
        List<OrderViewDto> result = new ArrayList<>();

        for (Order order : orders) {
            OrderViewDto orderViewDto = new OrderViewDto();

            Buyer buyer = buyerManager.getById(order.getBuyerId());
            orderViewDto.setBuyerName(buyer.getFirstName() + ' ' + buyer.getLastName());

            orderViewDto.setOrderNr(order.getOrderNr());
            orderViewDto.setOrderId(order.getBuyerId());
            orderViewDto.setOrderStatus(OrderStatus.fromString(order.getOrderStatus()));
            orderViewDto.setOrderTime(order.getOrderTime());
            orderViewDto.setPaymentOption(PaymentOption.fromString(order.getPaymentOption()));
            orderViewDto.setContactNumber(order.getContactNumber());
            orderViewDto.setCurrency(order.getCurrency());
            orderViewDto.setTotalPrice(order.getTotalPrice());
            orderViewDto.setNotes(order.getNotes());

            buyerAddressRepository
                    .findById(order.getDeliveryAddressId())
                    .ifPresent(orderViewDto::setDeliveryAddress);

            List<OrderItem> orderItems =
                    orderItemRepository.findByOrderNr(order.getOrderNr());
            orderViewDto.setOrderItems(orderItems);

            result.add(orderViewDto);
        }
        return result;
    }
}
