package com.integrationtesting.restcontroller;

import com.integrationtesting.entity.Customer;
import com.integrationtesting.repository.CustomerRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CustomerController {

    private final CustomerRepository repo;

    CustomerController(CustomerRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/api/customers")
    List<Customer> getAll() {
        return repo.findAll();
    }
}
