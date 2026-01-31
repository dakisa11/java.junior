package hr.abysalto.hiring.api.junior.Service;

import hr.abysalto.hiring.api.junior.dto.MenuItemDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MenuService {
    public List<MenuItemDto> getMenuItems() {
        return List.of(
                new MenuItemDto(101, "Pizza Margherita", new BigDecimal("8.50")),
                new MenuItemDto(102, "Pizza Capricciosa", new BigDecimal("10.50")),
                new MenuItemDto(201, "Coca-Cola 0.5L", new BigDecimal("4.50")),
                new MenuItemDto(301, "Chicken Salad", new BigDecimal("9.50")),
                new MenuItemDto(401, "Cheeseburger", new BigDecimal("9.20")),
                new MenuItemDto(501, "French Fries", new BigDecimal("4.50"))
        );
    }
}
