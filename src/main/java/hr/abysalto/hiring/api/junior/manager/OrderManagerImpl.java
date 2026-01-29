package hr.abysalto.hiring.api.junior.manager;
import java.util.List;
import hr.abysalto.hiring.api.junior.model.Order;
import hr.abysalto.hiring.api.junior.model.OrderStatus;
import hr.abysalto.hiring.api.junior.repository.BuyerAddressRepository;
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

    public OrderManagerImpl(OrderRepository orderRepository, BuyerAddressRepository buyerAddressRepository) {
        this.orderRepository = orderRepository;
        this.buyerAddressRepository = buyerAddressRepository;
    }

    @Override
    public Iterable<OrderViewDto> getAllOrders() {
        List<Order> orders = (List<Order>)orderRepository.findAll();

        return orders.stream().map(order ->  {
            OrderViewDto dto = new OrderViewDto();
            dto.setOrderNr(order.getOrderNr());
            dto.setOrderStatus(OrderStatus.fromString(order.getOrderStatus()));
            dto.setOrderTime(order.getOrderTime());
            dto.setTotalPrice(order.getTotalPrice());

            buyerAddressRepository
                    .findById(order.getDeliveryAddressId())
                    .ifPresent(dto::setDeliveryAddress);

            return dto;
        }).toList();
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
}
