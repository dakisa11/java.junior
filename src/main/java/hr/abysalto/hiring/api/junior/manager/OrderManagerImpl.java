package hr.abysalto.hiring.api.junior.manager;
import java.util.ArrayList;
import java.util.List;

import hr.abysalto.hiring.api.junior.model.Order;
import hr.abysalto.hiring.api.junior.model.OrderItem;
import hr.abysalto.hiring.api.junior.model.OrderStatus;
import hr.abysalto.hiring.api.junior.repository.BuyerAddressRepository;
import hr.abysalto.hiring.api.junior.repository.OrderItemRepository;
import hr.abysalto.hiring.api.junior.repository.OrderRepository;
import hr.abysalto.hiring.api.junior.dto.OrderViewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderManagerImpl implements OrderManager {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private BuyerAddressRepository buyerAddressRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    public OrderManagerImpl(OrderRepository orderRepository,
                            BuyerAddressRepository buyerAddressRepository,
                            OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.buyerAddressRepository = buyerAddressRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public List<OrderViewDto> getAllOrders() {
        List<Order> orders = (List<Order>)orderRepository.findAll();
        List<OrderViewDto> result = new ArrayList<>();

        for (Order order : orders) {
            OrderViewDto orderViewDto = new OrderViewDto();

            orderViewDto.setOrderNr(order.getOrderNr());
            orderViewDto.setOrderStatus(OrderStatus.fromString(order.getOrderStatus()));
            orderViewDto.setOrderTime(order.getOrderTime());
            orderViewDto.setTotalPrice(order.getTotalPrice());

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

    @Override
    public Iterable<OrderViewDto> getAllBuyerOrders() {

    }
}
