package com.windev.online_banking.controller;

import com.windev.online_banking.entity.User;
import com.windev.online_banking.payload.response.MessageResponse;
import com.windev.online_banking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Endpoint để xem danh sách tất cả người dùng.
     * URL: GET /api/admin/users
     * Yêu cầu: ADMIN
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    /**
     * Endpoint để xóa người dùng.
     * URL: DELETE /api/admin/users/{id}
     * Yêu cầu: ADMIN
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found."));
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully."));
    }

    // Thêm các endpoint quản lý khác nếu cần, ví dụ: cập nhật vai trò, xem giao dịch của người dùng, v.v.
}
