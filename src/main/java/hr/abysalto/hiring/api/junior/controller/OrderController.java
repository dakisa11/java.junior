package hr.abysalto.hiring.api.junior.controller;

import hr.abysalto.hiring.api.junior.service.MenuService;
import hr.abysalto.hiring.api.junior.components.DatabaseInitializer;
import hr.abysalto.hiring.api.junior.manager.OrderManager;
import hr.abysalto.hiring.api.junior.manager.BuyerManager;
import hr.abysalto.hiring.api.junior.model.BuyerAddress;
import hr.abysalto.hiring.api.junior.model.Order;
import hr.abysalto.hiring.api.junior.model.OrderItem;
import hr.abysalto.hiring.api.junior.repository.BuyerAddressRepository;
import hr.abysalto.hiring.api.junior.repository.OrderItemRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@Tag(name = "Orders", description = "for handling orders")
@RequestMapping("order")
@Controller
public class OrderController {

    @Autowired private OrderManager orderManager;
    @Autowired private BuyerManager buyerManager;
    @Autowired private DatabaseInitializer databaseInitializer;
    @Autowired private BuyerAddressRepository buyerAddressRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private MenuService menuService;

    @Operation(summary = "Get all buyers", responses = {
            @ApiResponse(description = "Success", responseCode = "200", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Order.class)))),
            @ApiResponse(description = "Precondition failed", responseCode = "412", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(description = "Error", responseCode = "500", content = @Content(mediaType = "application/json")) })
    @GetMapping("/list")
    public ResponseEntity list() {
        if (!this.databaseInitializer.isDataInitialized()) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).contentType(MediaType.TEXT_PLAIN).body("Data not initialized");
        }
        try {
            return ResponseEntity.ok(this.orderManager.getAllOrders());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(ex);
        }
    }

    @GetMapping("/")
    public String viewHomePage(
            @RequestParam(value = "sort", required = false, defaultValue = "asc") String sort,
            Model model
    ){
        model.addAttribute("orderList", this.orderManager.getAllOrdersSortedByTotal(sort));
        model.addAttribute("sort", sort);

        return "order/index";
    }

    @GetMapping("/{buyerId}/orders")
    public String viewBuyerOrder(@PathVariable Long buyerId, Model model){
        model.addAttribute("orderList", this.orderManager.getAllBuyerOrders(buyerId));
        model.addAttribute("buyerId", buyerId);
        return "order/orders";
    }

    @GetMapping("/addnew")
    public String addNewOrder(Model model) {
        Order order = new Order();
        model.addAttribute("order", order);
        model.addAttribute("buyerList", buyerManager.getAllBuyers());
        model.addAttribute("menuItems", menuService.getMenuItems());
        return "order/neworder";
    }

    @PostMapping("/save")
    public String saveOrder(
            @ModelAttribute("order") Order order,
            @RequestParam String street,
            @RequestParam String homeNumber,
            @RequestParam String city,
            @RequestParam MultiValueMap<String, String> parameters
    ){
        BuyerAddress addr = new BuyerAddress();
        addr.setStreet(street);
        addr.setHomeNumber(homeNumber);
        addr.setCity(city);

        orderManager.createOrder(order, addr, parameters);

        return "redirect:/order/";
    }

    @PostMapping("/update/{id}")
    public String updateOrder(@PathVariable("id") Long orderNr,
                              @ModelAttribute("order") Order order,
                              @RequestParam(required = false) Long existingAddressId,
                              @RequestParam(required = false) String street,
                              @RequestParam(required = false) String homeNumber,
                              @RequestParam(required = false) String city,
                              @RequestParam MultiValueMap<String, String> parameters
    ){
        BuyerAddress newAddr = null;
        if (street != null && !street.isBlank()) {
            newAddr = new BuyerAddress();
            newAddr.setStreet(street);
            newAddr.setHomeNumber(homeNumber);
            newAddr.setCity(city);
        }

        orderManager.updateOrder(orderNr, order, existingAddressId, newAddr, parameters);

        return "redirect:/order/";
    }

    @GetMapping("/showFormForUpdate/{id}")
    public String updateForm(@PathVariable(value = "id") long id, Model model) {
        Order order = this.orderManager.getById(id);
        model.addAttribute("order", order);

        model.addAttribute("buyerList", buyerManager.getAllBuyers());
        model.addAttribute("addressList", buyerAddressRepository.findAll());
        model.addAttribute("menuItems", menuService.getMenuItems());

        Map<Integer, Short> qtyMap = new HashMap<>();
        for (OrderItem orderItem: orderItemRepository.findByOrderNr(order.getOrderNr())) {
            qtyMap.put(orderItem.getItemNr(), orderItem.getQuantity());
        }
        model.addAttribute("qtyMap", qtyMap);

        return "order/updateorder";
    }

    @GetMapping("/deleteOrder/{id}")
    public String deleteById ( @PathVariable(value = "id") long id){
        orderManager.deleteById(id);
        return "redirect:/order/";
    }

    @PostMapping("/{orderNr}/status")
    public String updateOrderStatus(
            @PathVariable Long orderNr,
            @RequestParam("status") String status,
            @RequestParam(value = "redirect", required = false, defaultValue = "/order/") String redirect
    ) {
        orderManager.updateStatus(orderNr, status);
        return "redirect:" + redirect;
    }

}
