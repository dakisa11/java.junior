package hr.abysalto.hiring.api.junior.repository;

import hr.abysalto.hiring.api.junior.model.OrderItem;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderItemRepository extends CrudRepository<OrderItem, Long> {
        List<OrderItem> findByOrderNr(Long orderNr);
        @Modifying
        @Query("DELETE FROM order_item WHERE order_nr = :orderNr")
        void deleteByOrderNr(Long orderNr);
}
