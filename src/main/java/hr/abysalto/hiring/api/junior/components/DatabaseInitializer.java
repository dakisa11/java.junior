package hr.abysalto.hiring.api.junior.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private boolean dataInitialized = false;

	public boolean isDataInitialized() {
		return this.dataInitialized;
	}

	public void initialize() {
		initTables();
		initData();
		this.dataInitialized = true;
	}

	private void initTables() {
		this.jdbcTemplate.execute("""
			 CREATE TABLE buyer (
				 buyer_id INT auto_increment PRIMARY KEY,
				 first_name varchar(100) NOT NULL,
				 last_name varchar(100) NOT NULL,
				 title varchar(100) NULL
			 );
 		""");

		this.jdbcTemplate.execute("""
			 CREATE TABLE buyer_address (
				 buyer_address_id INT auto_increment PRIMARY KEY,
				 city varchar(100) NOT NULL,
				 street varchar(100) NOT NULL,
				 home_number varchar(100) NULL
			 );
 		""");

		this.jdbcTemplate.execute("""
			 CREATE TABLE orders (
				 order_nr INT auto_increment PRIMARY KEY,
				 buyer_id int NOT NULL,
				 order_status varchar(32) NOT NULL,
				 order_time datetime NOT NULL,
	 			 payment_option varchar(32) NOT NULL,
				 delivery_address_id INT NOT NULL,
				 contact_number varchar(100) NULL,
				 currency varchar(50) NULL,
				 total_price DECIMAL(10,2),
	 			 notes varchar(500) NULL,
				 CONSTRAINT FK_order_to_buyer FOREIGN KEY (buyer_id) REFERENCES buyer (buyer_id) ON DELETE CASCADE,
				 CONSTRAINT FK_order_to_delivery_address FOREIGN KEY (delivery_address_id) REFERENCES buyer_address (buyer_address_id)
			 );
 		""");

		this.jdbcTemplate.execute("""
			 CREATE TABLE order_item (
				 order_item_id INT auto_increment PRIMARY KEY,
				 order_nr int NOT NULL,
				 item_nr int NOT NULL,
				 name varchar(100) NOT NULL,
				 quantity smallint NOT NULL,
				 price DECIMAL(10,2),
				 CONSTRAINT UC_order_items UNIQUE (order_item_id, order_nr),
				 CONSTRAINT FK_order_item_to_order FOREIGN KEY (order_nr) REFERENCES orders (order_nr) ON DELETE CASCADE
			 );
 		""");
	}

	private void initData() {
		this.jdbcTemplate.execute("INSERT INTO buyer (first_name, last_name, title) VALUES ('Jabba', 'Hutt', 'the')");
		this.jdbcTemplate.execute("INSERT INTO buyer (first_name, last_name, title) VALUES ('Anakin', 'Skywalker', NULL)");
		this.jdbcTemplate.execute("INSERT INTO buyer (first_name, last_name, title) VALUES ('Jar Jar', 'Binks', NULL)");
		this.jdbcTemplate.execute("INSERT INTO buyer (first_name, last_name, title) VALUES ('Han', 'Solo', NULL)");
		this.jdbcTemplate.execute("INSERT INTO buyer (first_name, last_name, title) VALUES ('Leia', 'Organa', 'Princess')");
		this.jdbcTemplate.execute("""
			INSERT INTO buyer_address (city, street, home_number) VALUES
			('Zagreb', 'Ilica', '10'),
			('Split', 'Ulica kralja Tomislava', '5A'),
			('Rijeka', 'Korzo', '22'),
			('Osijek', 'Europska avenija', '3');
			""");

		this.jdbcTemplate.execute(
		"""
				INSERT INTO orders
				(buyer_id, order_status, order_time, payment_option, delivery_address_id,
				 contact_number, currency, total_price, notes)
				VALUES
				(1, 'WAITING_FOR_CONFIRMATION', '2026-01-31 18:45:00',
				 'CASH', 1, '+385991112233', 'EUR', 26.00,
				 'Please ring the doorbell'),
				 
				(2, 'PREPARING', '2026-01-31 19:10:00',
				 'CARD_UPFRONT', 2, '+385981234567', 'EUR', 34.50,
				 'Extra cheese on pizza'),
				 
				(3, 'DONE', '2026-01-31 17:30:00',
				 'CARD_ON_DELIVERY', 3, '+385991998877', 'EUR', 18.20,
				 'Leave at reception'),
				 
				(4, 'DONE', '2026-01-30 13:15:00',
				 'CARD_UPFRONT', 4, '+385951234999', 'EUR', 42.80,
				 'Office lunch order');
			""");
		this.jdbcTemplate.execute("""
			INSERT INTO order_item
			(order_nr, item_nr, name, quantity, price)
			VALUES
			(1, 101, 'Pizza Margherita', 2, 8.50),
			(1, 201, 'Coca-Cola 0.5L', 2, 4.50);
			""");
		this.jdbcTemplate.execute("""
			INSERT INTO order_item
			(order_nr, item_nr, name, quantity, price)
			VALUES
			(2, 102, 'Pizza Capricciosa', 1, 10.50),
			(2, 301, 'Chicken Salad', 1, 9.50),
			(2, 201, 'Coca-Cola 0.5L', 3, 4.83);
			""");
		this.jdbcTemplate.execute("""
			INSERT INTO order_item
			(order_nr, item_nr, name, quantity, price)
			VALUES
			(3, 401, 'Cheeseburger', 1, 9.20),
			(3, 201, 'Coca-Cola 0.5L', 1, 4.50),
			(3, 501, 'French Fries', 1, 4.50);
			""");
		this.jdbcTemplate.execute("""
			INSERT INTO order_item
			(order_nr, item_nr, name, quantity, price)
			VALUES
			(4, 101, 'Pizza Margherita', 3, 8.50),
			(4, 301, 'Chicken Salad', 1, 9.50),
			(4, 201, 'Coca-Cola 0.5L', 2, 4.90);
			""");








	}
}
