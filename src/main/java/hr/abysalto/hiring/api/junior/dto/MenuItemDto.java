package hr.abysalto.hiring.api.junior.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MenuItemDto {
    private Integer itemNr;
    private String itemName;
    private BigDecimal price;
}
