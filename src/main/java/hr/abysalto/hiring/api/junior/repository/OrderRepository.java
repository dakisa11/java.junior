package hr.abysalto.hiring.api.junior.repository;

import hr.abysalto.hiring.api.junior.model.Order;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderRepository extends CrudRepository<Order, Long> {
    List<Order> findByBuyerId(Long buyerId);
}
