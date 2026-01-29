package hr.abysalto.hiring.api.junior.controller;

import hr.abysalto.hiring.api.junior.components.DatabaseInitializer;
import hr.abysalto.hiring.api.junior.manager.OrderManager;
import hr.abysalto.hiring.api.junior.model.Order;
import hr.abysalto.hiring.api.junior.manager.BuyerManager;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Orders", description = "for handling orders")
@RequestMapping("order")
@Controller
public class OrderController {

    @Autowired
    private OrderManager orderManager;
    //@Autowired
    //private BuyerManager buyerManager;
    @Autowired
    private DatabaseInitializer databaseInitializer;

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
    public String viewHomePage(Model model) {
        model.addAttribute("orderList", this.orderManager.getAllOrders());
        return "order/index";
    }

    @GetMapping("/addnew")
    public String addNewEmployee(Model model) {
        Order order = new Order();
        model.addAttribute("order", order);
        return "order/neworder";
    }

    @PostMapping("/save")
    public String saveOrder(@ModelAttribute("order") Order order) {
        this.orderManager.save(order);
        return "redirect:/order/";
    }

    @GetMapping("/showFormForUpdate/{id}")
    public String updateForm(@PathVariable(value = "id") long id, Model model) {
        Order order = this.orderManager.getById(id);
        model.addAttribute("order", order);
        return "order/updateorder";
    }

    @GetMapping("/deleteOrder/{id}")
    public String deleteById(@PathVariable(value = "id") long id) {
        this.orderManager.deleteById(id);
        return "redirect:/order/";
    }

}
