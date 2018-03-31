package com.barath.reactive.mongodb.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;

@SpringBootApplication
@RestController
public class ReactiveMongoApplication {

	private static final Logger logger= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final UserService userService;

	public ReactiveMongoApplication(UserService userService) {
		this.userService=userService;
	}

	public static void main(String[] args) {
		SpringApplication.run(ReactiveMongoApplication.class, args);
	}

	@GetMapping("/users")
	public Flux<User> fetchUsers(){

		return this.userService.getUsers();
	}

	@PostMapping("/addUser")
	public Mono<User> createUser(@RequestBody Mono<User> userMono){

		return this.userService.addUser(userMono);
	}


	@GetMapping("/user")
	public Flux<User> getUserByName(@RequestParam("name") Mono<String> stringMono){

		return this.userService.getUserByUserName(stringMono);
	}

	@GetMapping("/users/sort")
	public Flux<User> getUserNamesSorted(){

		return this.userService.getUsers();
	}

	@PostConstruct
	public void init(){

		if (logger.isInfoEnabled()) logger.info(" popualting user documents ");
		Flux.just(Mono.just(new User("kapildev"))
				, Mono.just(new User("sachin"))
				, Mono.just(new User("dhoni"))
				, Mono.just(new User("kohli"))
				, Mono.just(new User("ashwin"))
				, Mono.just(new User("ramesh"))
				, Mono.just(new User("dhawan"))
				, Mono.just(new User("rohit"))
				, Mono.just(new User("bumrah"))
				, Mono.just(new User("rahanae")))
				.subscribe(this.userService::addUser);

		this.userService.addUser(Mono.just(new User("barath")));
	}
}

@Configuration
@EnableReactiveMongoRepositories(basePackageClasses = {UserRepository.class})
class MongoConfiguration extends AbstractReactiveMongoConfiguration{

	@Override
	public MongoClient reactiveMongoClient() {
		return MongoClients.create();
	}

	@Override
	protected String getDatabaseName() {
		return "test";
	}
	@Bean
	public ReactiveMongoTemplate reactiveMongoTemplate() {
		return new ReactiveMongoTemplate(reactiveMongoClient(), getDatabaseName());
	}
}




@Service
class UserService{

	private static final Logger logger= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public Mono<User> addUser(final Mono<User> userMono){

		return  userMono.doOnNext( user -> {
			logger.info("saving user with details {} ",user);
			this.userRepository.save(user).subscribe();
		} ).log();
	}

	public Flux<User> getUsers(){

		return this.userRepository.findAll().log();
	}

	public Flux<String> getUsersSortedByNames(){

		return this.userRepository.findAll().map(User::getUserName).sort();
	}

	public Flux<User> getUserByUserName(Mono<String> stringMono){

		return this.userRepository.findByUserName(stringMono).log();
	}






}

@Document
//@JsonIgnoreProperties(ignoreUnknown = true)
class User{

	@Id
	@JsonIgnore
	private String id;

	private String userName;


	public User(String userName) {
		this.userName = userName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public String toString() {
		return "User{" +
				", userName='" + userName + '\'' +
				'}';
	}
}
