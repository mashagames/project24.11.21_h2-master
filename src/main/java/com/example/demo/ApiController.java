package com.example.demo;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Map.*;


@RestController
public class ApiController {
    private final UserRepository userRepository;
    public ApiController(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    // 1.   Создать пользователя
    // curl -s -X POST http://localhost:8080/adduser -d 'username=user&password=pwd123&repeatPassword=pwd123&age=25'
    @PostMapping("adduser")
    public void addUser(
            @RequestParam("username") String username, @RequestParam("password") String password,
            @RequestParam("repeatPassword") String repeatPassword, @RequestParam("age") Integer age) {
//        if (!Pattern.matches("[a-zA-Z]+", username)) throw new WrongFormatException();
        if (!userRepository.findByUsername(username).isEmpty()) throw new UserExistsException();
        if (password.equals(repeatPassword)) {
            userRepository.save(new User(username, password, age));
        } else {
            throw new WrongFormatException();
        }
    }

    // 5. Возвращает список пользователей
    // curl -s -X GET http://localhost:8080/users
    // 0. Возвращает список пользователей
    // curl -s -X GET http://localhost:8080/users?sortBy=username&direction=down
    // 0. Возвращает пользователя по username
    // curl -s -X GET http://localhost:8080/users?username=user
    // 0. Возвращает список пользователей по age+-5
    // curl -s -X GET http://localhost:8080/users?age=25

    @GetMapping("/users")
    public List<User> getAllUsers(@RequestParam(name = "username", required = false) String username,
                                  @RequestParam(name = "age", required=false) Integer age,
                                  @RequestParam(name = "sortBy", required=false) String sortBy,
                                  @RequestParam(name = "direction", required=false) String direction) {
        List<User> users = new ArrayList<User>();
        if (username == null && age == null) {
            if (sortBy == null) {userRepository.findAll().forEach(users::add);}
            else {
                if (direction.equals("down"))
                    users = userRepository.findAll(Sort.by(Sort.Direction.DESC, sortBy));
                else
                    users = userRepository.findAll(Sort.by(Sort.Direction.ASC, sortBy));
            }
            }
        if (username != null && age == null) userRepository.findByUsername(username).forEach(users::add);
        if (username == null && age != null) {
            for (Integer i = age - 5; i< age+5; i++)
                userRepository.findByAge(i).forEach(users::add);
        }
        return users;
    }

    // 2. Возвращает пользователя по ID
    // curl -s -X GET http://localhost:8080/users/1

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable("id") long id) {
        Optional<User> userData =
                userRepository.findById(id);
        if (userData.isPresent()) {
            return userData.get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    // 3. Удаляет пользователя по id. Если пользователя нет, то вернуть 404 ошибку.
    /*  curl -s -X DELETE http://localhost:8080/users/1  */

    @DeleteMapping("/users/{id}")
    public void deleteTutorial(@PathVariable("id") long id) {
        if (userRepository.existsById(id))
            userRepository.deleteById(id);
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    // 4. Обновить пользователя по id. Если такого пользователя нет, то вернуть 404 ошибку.
    // curl -s -X PUT http://localhost:8080/users/1  -d 'username=user&password=newpwd&repeatPassword=newpwd&age=25'
    @PutMapping("/users/{id}")
    public void updateUser(@PathVariable("id") long id,
                           @RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("repeatPassword") String repeatPassword,
                           @RequestParam("age") Integer age) {
        if (!password.equals(repeatPassword)) throw new WrongFormatException();
        User user = new User(username, password, age);
        Optional<User> userData = userRepository.findById(id);
        if (userData.isPresent()) {
            User _user = userData.get();
            _user.setUsername(user.getUsername());
            _user.setPassword(user.getPassword());
            _user.setAge(user.getAge());
            userRepository.save(_user);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
