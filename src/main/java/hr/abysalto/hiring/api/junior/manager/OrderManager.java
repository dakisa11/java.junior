package hr.abysalto.hiring.api.junior.manager;
import hr.abysalto.hiring.api.junior.dto.OrderViewDto;
import hr.abysalto.hiring.api.junior.model.Order;

public interface OrderManager {
    Iterable<OrderViewDto> getAllOrders();
    void save(Order order);
    Order getById(Long id);
    void deleteById(Long id);
    abstract Iterable<OrderViewDto> getAllBuyerOrders(Long buyerId);
}
