package com.barath.app.controller;

import com.barath.app.document.Customer;
import com.barath.app.exception.CustomerNotFoundException;
import com.barath.app.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class CustomerController {

    private static final Logger logger= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService){

        this.customerService=customerService;

    }

    @PostMapping(value="/customer",produces = MediaType.APPLICATION_JSON_UTF8_VALUE,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Customer createSCustomer(@RequestBody Customer customer) throws Exception {

       Optional<Customer>   cust= Optional.ofNullable(Optional.ofNullable(customer).orElseThrow(() -> new Exception("Customer cannot be saved")));
       if(cust.isPresent()){
         return customerService.saveCustomer(customer);
       }
       return cust.get();
    }

    @GetMapping(value="/customer/{customerName}")
    public Customer getCustomerByFirstName(@PathVariable String customerName){

        if(!StringUtils.isEmpty(customerName)){
           return customerService.getCustomer(customerName);
        }
        throw new CustomerNotFoundException("Customer with customer first name "+customerName+" not found");

    }

    @GetMapping(value = "/customers",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Customer> getCustomers(){

        return customerService.getCustomers();
    }
}
