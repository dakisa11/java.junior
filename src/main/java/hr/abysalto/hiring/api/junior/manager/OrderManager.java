package hr.abysalto.hiring.api.junior.manager;

import hr.abysalto.hiring.api.junior.dto.OrderViewDto;
import hr.abysalto.hiring.api.junior.model.BuyerAddress;
import hr.abysalto.hiring.api.junior.model.Order;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface OrderManager {
    void updateStatus(Long orderNr, String status);
    void deleteById(Long id);

    Iterable<OrderViewDto> getAllOrders();
    Iterable<OrderViewDto> getAllBuyerOrders(Long buyerId);
    List<OrderViewDto> getAllOrdersSortedByTotal(String direction);

    Order save(Order order);
    Order getById(Long id);

    @Transactional
    Order createOrder(Order order, BuyerAddress newAddress, MultiValueMap<String, String> parameters);

    @Transactional
    Order updateOrder(Long orderNr, Order updatedOrder, Long existingAddressId,
                      BuyerAddress maybeNewAddress, MultiValueMap<String, String> parameters);

}
