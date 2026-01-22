package com.tencent.tdesign.controller;

import com.tencent.tdesign.annotation.RepeatSubmit;
import com.tencent.tdesign.entity.UserParameterEntity;
import com.tencent.tdesign.service.UserParameterService;
import com.tencent.tdesign.vo.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/parameters")
public class UserParameterController {

    private final UserParameterService service;

    public UserParameterController(UserParameterService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<UserParameterEntity>> list() {
        return ApiResponse.success(service.getCurrentUserParameters());
    }

    @PostMapping
    @RepeatSubmit
    public ApiResponse<UserParameterEntity> create(@RequestBody UserParameterEntity parameter) {
        return ApiResponse.success(service.saveParameter(parameter));
    }

    @PutMapping("/{id}")
    @RepeatSubmit
    public ApiResponse<UserParameterEntity> update(@PathVariable Long id, @RequestBody UserParameterEntity parameter) {
        return ApiResponse.success(service.updateParameter(id, parameter));
    }

    @DeleteMapping("/{id}")
    @RepeatSubmit
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.deleteParameter(id);
        return ApiResponse.success(null);
    }
}
