package hr.abysalto.hiring.api.junior.repository;

import hr.abysalto.hiring.api.junior.model.Order;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderRepository extends CrudRepository<Order, Long> {
    @Modifying
    @Query("UPDATE orders SET order_status = :status WHERE order_nr = :orderNr")
    void updateStatus(Long orderNr, String status);

    @Query("SELECT * FROM orders ORDER BY total_price ASC")
    List<Order> findAllOrdersByTotalPriceAsc();
    @Query("SELECT * FROM orders ORDER BY total_price DESC")
    List<Order> findAllOrdersByTotalPriceDesc();
    List<Order> findByBuyerId(Long buyerId);


}
