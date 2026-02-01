package hr.abysalto.hiring.api.junior.manager;
import hr.abysalto.hiring.api.junior.dto.OrderViewDto;
import hr.abysalto.hiring.api.junior.model.Order;

import java.math.BigDecimal;
import java.util.List;

public interface OrderManager {
    Iterable<OrderViewDto> getAllOrders();
    Order save(Order order);
    Order getById(Long id);
    void updateStatus(Long orderNr, String status);
    void deleteById(Long id);
    abstract Iterable<OrderViewDto> getAllBuyerOrders(Long buyerId);
    List<OrderViewDto> getAllOrdersSortedByTotal(String direction);
}
