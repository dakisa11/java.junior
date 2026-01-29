package hr.abysalto.hiring.api.junior.repository;

import hr.abysalto.hiring.api.junior.model.OrderItem;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderItemRepository extends CrudRepository<OrderItem, Long> {
        List<OrderItem> findByOrderNr(Long orderNr);
}
